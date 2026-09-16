package com.masu.platochess

data class BoardState(val squares:List<Char?>,val sideToMove:PlayerSide){init{require(squares.size==64)}
 fun fenPlacement():String{val out=StringBuilder();for(r in 0..7){var empty=0;for(c in 0..7){val p=squares[r*8+c];if(p==null)empty++ else{if(empty>0){out.append(empty);empty=0};out.append(p)}};if(empty>0)out.append(empty);if(r<7)out.append('/')};return out.toString()}
 fun fen():String="${fenPlacement()} ${if(sideToMove==PlayerSide.WHITE) "w" else "b"} - - 0 1"
}
