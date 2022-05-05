data class MTGGameLog (val turn:Int, val action:MTGGameLogType, val info:String = "")

enum class MTGGameLogType {
    GAME_START,
    TURN_START,
    CARD_DRAW,
    PLAY_LAND,
    MULLIGAN_CHECK,
}