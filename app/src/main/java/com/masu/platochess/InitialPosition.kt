package com.masu.platochess
object InitialPosition{val whiteView=listOf("rnbqkbnr","pppppppp","........","........","........","........","PPPPPPPP","RNBQKBNR");val blackView=whiteView.reversed().map{it.reversed()};fun asSquares(side:PlayerSide):List<Char?>=(if(side==PlayerSide.WHITE)whiteView else blackView).flatMap{row->row.map{if(it=='.')null else it}}}
