data class MTGTriggeredAction(
    val trigger:MTGTrigger,
    val forCard:CardQuery,
    val action:List<IExecutable<MTGBoardState>>,
    val condition:ICondition<MTGBoardState>? = null
) {
    companion object {
        fun onDestroyed(forCard:String, action:IExecutable<MTGBoardState>,condition:ICondition<MTGBoardState>? = null) = onDestroyed(CardQuery.parse(forCard),listOf(action),condition)
        fun onDestroyed(forCard:String, action:List<IExecutable<MTGBoardState>>,condition:ICondition<MTGBoardState>? = null) = onDestroyed(CardQuery.parse(forCard),action,condition)
        fun onDestroyed(forCard:CardQuery, action:List<IExecutable<MTGBoardState>>,condition:ICondition<MTGBoardState>? = null) = MTGTriggeredAction(
            trigger = MTGTrigger.DESTROYED,
            forCard = forCard,
            action = action,
            condition = condition,
        )
        
        fun onETB(forCard:String, action:IExecutable<MTGBoardState>,condition:ICondition<MTGBoardState>? = null) = onETB(CardQuery.parse(forCard),listOf(action),condition)
        fun onETB(forCard:String, action:List<IExecutable<MTGBoardState>>,condition:ICondition<MTGBoardState>? = null) = onETB(CardQuery.parse(forCard),action,condition)
        fun onETB(forCard:CardQuery, action:List<IExecutable<MTGBoardState>>,condition:ICondition<MTGBoardState>? = null) = MTGTriggeredAction(
            trigger = MTGTrigger.ETB,
            forCard = forCard,
            action = action,
            condition = condition,
        )

        fun onCast(forCard:String, action:IExecutable<MTGBoardState>,condition:ICondition<MTGBoardState>? = null) = onCast(CardQuery.parse(forCard),listOf(action),condition)
        fun onCast(forCard:String, action:List<IExecutable<MTGBoardState>>,condition:ICondition<MTGBoardState>? = null) = onCast(CardQuery.parse(forCard),action,condition)
        fun onCast(forCard:CardQuery, action:List<IExecutable<MTGBoardState>>,condition:ICondition<MTGBoardState>? = null) = MTGTriggeredAction(
            trigger = MTGTrigger.CAST,
            forCard = forCard,
            action = action,
            condition = condition,
        )
    }
}