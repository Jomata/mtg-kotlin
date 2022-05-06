data class MTGGameLog (val turn:Int, val action:MTGGameLogType, val info:String = "")

enum class MTGGameLogType {
    GAME_START,
    TURN_START,
    ZONE_HAND,
    ZONE_FIELD,
    ZONE_YARD,
    ZONE_EXILE,
    ZONE_LANDS,
    CARD_DRAW,
    PLAY_LAND,
    MULLIGAN_CHECK,
    TAP_LANDS,
    CARD_MILL,
    TAP_PERMANENT,
    CAST,
    TUTOR,
    DISCARD,
    FLASHBACK,
    REANIMATE,
    EXILE,
}