data class MTGBoardAction(val action:MTGBoardActionType, val howMany:Int) {
    fun execute(board:MTGBoardState):MTGBoardState {
        return when (action) {
            MTGBoardActionType.TAP_LANDS -> board.tapLands(howMany)
            MTGBoardActionType.DRAW -> board.drawCards(howMany)
            MTGBoardActionType.MILL -> board.millCards(howMany)
        }
    }
}

enum class MTGBoardActionType {
    TAP_LANDS,
    DRAW,
    MILL,
}