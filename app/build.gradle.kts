plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.masu.platochess"
    compileSdk = 35
    buildToolsVersion = "35.0.0"

    defaultConfig {
        applicationId = "com.masu.platochess"
        minSdk = 23
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

android {
    ndkVersion = "27.2.12479018"
    sourceSets.getByName("main") {
        jniLibs.srcDir(layout.buildDirectory.dir("stockfish-libs"))
        assets.srcDir(layout.buildDirectory.dir("stockfish-assets"))
    }
    packaging.jniLibs.useLegacyPackaging = true
}

val buildStockfish by tasks.registering(Exec::class) {
    workingDir(rootProject.projectDir)
    commandLine("bash", "scripts/build-stockfish.sh")
    inputs.file(rootProject.file("scripts/build-stockfish.sh"))
    outputs.dir(layout.buildDirectory.dir("stockfish-libs"))
    outputs.dir(layout.buildDirectory.dir("stockfish-assets"))
}
tasks.named("preBuild").configure { dependsOn(buildStockfish) }
dependencies { testImplementation("junit:junit:4.13.2") }
