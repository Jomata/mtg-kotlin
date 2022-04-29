data class MTGGameAction (val turn:Int, val action:MTGGameActionType, val info:String = "")

enum class MTGGameActionType {
    GAME_START,
    TURN_START,
    CARD_DRAW,
    PLAY_LAND,
}