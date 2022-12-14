data class MTGCardAction (val action:MTGCardActionType, val query:CardQuery) : IExecutable<MTGBoardState> {
    constructor(action: MTGCardActionType, query: String) : this(action, CardQuery.parse(query))

    override fun execute(board:MTGBoardState) : MTGBoardState {
        return when (action) {
            MTGCardActionType.TAP -> board.tapPermanent(query)
            MTGCardActionType.CAST -> board.cast(query)
            MTGCardActionType.TRY_CAST -> board.tryCast(query)
            MTGCardActionType.TUTOR -> board.tutor(query)
            MTGCardActionType.DISCARD -> board.discard(query)
            MTGCardActionType.DESTROY -> board.destroy(query)
            MTGCardActionType.RECOVER -> board.recover(query)
            //MTGCardActionType.PLAY_LAND -> TODO()
            MTGCardActionType.FLASHBACK -> board.flashback(query)
            MTGCardActionType.REANIMATE -> board.reanimate(query)
            MTGCardActionType.EXILE_FROM_FIELD -> board.exileFromField(query)
            MTGCardActionType.EXILE_FROM_YARD -> board.exileFromYard(query)
        }
    }
}

enum class MTGCardActionType {
    TAP,
    CAST,
    TRY_CAST,
    TUTOR,
    DISCARD,
    DESTROY,
    RECOVER,
    //PLAY_LAND,
    FLASHBACK,
    REANIMATE,
    EXILE_FROM_FIELD,
    EXILE_FROM_YARD,
}