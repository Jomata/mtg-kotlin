data class MTGGameSim(
    val deck:List<MTGCard>,
    val gameLogic: List<MTGBoardLogic>,
    val cardTriggers: List<MTGTriggeredAction>,
) {
    fun runSims(winCon: ICondition<MTGBoardState>, numSims: Int = 1000): List<Int> {
        val game = MTGBoardState(
            deck = deck,
            triggers = cardTriggers,
        )
        val simResults = (1..numSims).map {
            game.startGame().playUntilWinCon(winCon, gameLogic).turn
        }
        return simResults
    }
}