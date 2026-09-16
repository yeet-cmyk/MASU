package com.masu.platochess
/** Small offline fallback used only when the exact tracked opening position is known. */
object OpeningAdvisor{fun firstMove(side:PlayerSide):String=if(side==PlayerSide.WHITE)"e2e4" else "e7e5"}
