package com.masu.platochess

import android.app.*
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.util.DisplayMetrics
import android.view.WindowManager
import java.io.File

class CaptureService : Service() {
    private var projection: MediaProjection? = null
    private var reader: ImageReader? = null
    private var display: VirtualDisplay? = null
    private val worker = HandlerThread("Board analysis")
    private lateinit var handler: Handler
    private val recognizer = BoardRecognizer()
    private val game = GameTracker()
    private val throttle = FrameThrottle(500)
    private var engine: StockfishAdvisor? = null
    private var suggestionFen: String? = null
    private var suggestion: String? = null
    @Volatile private var stopped = false
    private var width = 0; private var height = 0
    private var savedHistory = -1
    private val sessionName = "game-${System.currentTimeMillis()}.txt"
    private val callback = object : MediaProjection.Callback() {
        override fun onStop() { OverlayBus.message = "توقف التقاط الشاشة"; stopSelf() }
        override fun onCapturedContentResize(w: Int, h: Int) {
            if (width > 0 && (w != width || h != height)) {
                OverlayBus.message = "تغيّر حجم الشاشة • أعد بدء التحليل"
                stopSelf()
            }
        }
    }
    override fun onCreate() {
        super.onCreate()
        worker.start(); handler = Handler(worker.looper)
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) nm.createNotificationChannel(NotificationChannel("capture", "Screen analysis", NotificationManager.IMPORTANCE_LOW))
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this,"capture") else Notification.Builder(this)
        val stop = PendingIntent.getService(this, 1, Intent(this, CaptureService::class.java).setAction("STOP"), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        startForeground(7, builder.setContentTitle("Plato Chess Assistant").setContentText("التقاط الشاشة يعمل • أوقفه بعد المباراة")
            .setSmallIcon(android.R.drawable.ic_menu_view).setOngoing(true)
            .addAction(android.R.drawable.ic_media_pause, "إيقاف", stop).build())
        engine = StockfishAdvisor(File(applicationInfo.nativeLibraryDir, "libstockfish.so"))
        OverlayBus.message = "افتح رقعة جديدة قبل أول نقلة"
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            stopService(Intent(this, OverlayService::class.java)); stopSelf(); return START_NOT_STICKY
        }
        if (projection != null) return START_NOT_STICKY
        val data = if (Build.VERSION.SDK_INT >= 33) intent?.getParcelableExtra("data", Intent::class.java)
            else @Suppress("DEPRECATION") intent?.getParcelableExtra<Intent>("data")
        if (data == null || intent.getIntExtra("resultCode", 0) != Activity.RESULT_OK) { stopSelf(); return START_NOT_STICKY }
        try {
            val wm = getSystemService(WINDOW_SERVICE) as WindowManager
            if (Build.VERSION.SDK_INT >= 30) {
                val bounds = wm.maximumWindowMetrics.bounds
                width = bounds.width(); height = bounds.height()
            } else {
                val dm = DisplayMetrics()
                @Suppress("DEPRECATION") wm.defaultDisplay.getRealMetrics(dm)
                width = dm.widthPixels; height = dm.heightPixels
            }
            projection = (getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager).getMediaProjection(Activity.RESULT_OK, data)
            projection!!.registerCallback(callback, Handler(Looper.getMainLooper()))
            reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
            reader!!.setOnImageAvailableListener({ source -> consume(source) }, handler)
            display = projection!!.createVirtualDisplay("PlatoBoard", width, height, resources.displayMetrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader!!.surface, null, handler)
        } catch (e: Exception) {
            OverlayBus.message = "تعذر بدء الالتقاط • أعد منح إذن الشاشة"
            stopSelf()
        }
        return START_NOT_STICKY
    }
    private fun consume(source: ImageReader) {
        if (stopped) return
        val image = try { source.acquireLatestImage() } catch (_: IllegalStateException) { null } ?: return
        var frame: Bitmap? = null
        try {
            if (!throttle.allow()) return
            val plane = image.planes[0]
            val paddedWidth = plane.rowStride / plane.pixelStride
            val bitmap = Bitmap.createBitmap(paddedWidth, image.height, Bitmap.Config.ARGB_8888)
            try {
                bitmap.copyPixelsFromBuffer(plane.buffer)
                frame = Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)
            } finally { if (bitmap !== frame) bitmap.recycle() }
        } catch (_: Exception) { OverlayBus.message = "تعذر قراءة الإطار" }
        finally { image.close() }
        val bitmap = frame ?: return
        try { if (!stopped) analyze(bitmap) }
        catch (_: Exception) { game.uncertain(); OverlayBus.message = "تعذر التحليل • أبقِ الرقعة ظاهرة" }
        finally { bitmap.recycle() }
    }
    private fun analyze(frame: Bitmap) {
        if (recognizer.playerSide == null) {
            OverlayBus.message = "انتظار وضع البداية • أبقِ الفقاعة خارج الرقعة"
            if (!recognizer.initialize(frame)) return
        }
        val cells = recognizer.read(frame)
        if (cells == null) {
            game.uncertain(); OverlayBus.message = "الرقعة غير واضحة • أبعد الفقاعة عنها"; return
        }
        if (!game.observe(cells)) {
            OverlayBus.message = "جارٍ تثبيت الرقعة أو انتظار وضع قانوني"; return
        }
        if (savedHistory != game.history.size) {
            val dir = File(filesDir,"games").apply { mkdirs() }
            File(dir, sessionName).writeText("Player: ${recognizer.playerSide}\nMoves: ${game.history.joinToString(" ")}\nFEN: ${game.board.fen()}\n")
            savedHistory = game.history.size
        }
        val legal = Rules.legalMoves(game.board)
        if (legal.isEmpty()) { OverlayBus.message = if (Rules.inCheck(game.board, game.board.sideToMove)) "كش مات" else "تعادل • لا نقلات"; return }
        val side = if (recognizer.playerSide == PlayerSide.WHITE) "أبيض" else "أسود"
        if (game.board.sideToMove != recognizer.playerSide) { OverlayBus.message = "$side • دور الخصم"; return }
        val fen = game.board.fen()
        if (fen == suggestionFen && suggestion != null) {
            OverlayBus.message = "$side • ${ChessRules.prettyMove(suggestion!!)}"; return
        }
        OverlayBus.message = "$side • يحسب Stockfish…"
        try {
            val best = engine?.bestMove(fen)
            if (stopped) return
            if (best != null && legal.any { it.uci() == best }) {
                suggestionFen = fen; suggestion = best
                // Show only after another screen frame confirms the position remains unchanged.
            } else OverlayBus.message = "لم يُرجع المحرك نقلة قانونية"
        } catch (_: Exception) { OverlayBus.message = "تعذر تشغيل Stockfish المحلي" }
    }
    override fun onDestroy() {
        stopped = true
        reader?.setOnImageAvailableListener(null, null)
        display?.release(); display = null
        projection?.unregisterCallback(callback); projection?.stop(); projection = null
        engine?.close()
        handler.post { reader?.close(); reader = null; worker.quitSafely() }
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
