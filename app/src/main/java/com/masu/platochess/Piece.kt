package com.masu.platochess
enum class Piece(val fen:Char){WHITE_KING('K'),WHITE_QUEEN('Q'),WHITE_ROOK('R'),WHITE_BISHOP('B'),WHITE_KNIGHT('N'),WHITE_PAWN('P'),BLACK_KING('k'),BLACK_QUEEN('q'),BLACK_ROOK('r'),BLACK_BISHOP('b'),BLACK_KNIGHT('n'),BLACK_PAWN('p')}
data class RecognizedSquare(val piece:Piece?,val confidence:Float)
