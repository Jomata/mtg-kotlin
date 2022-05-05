data class MTGBoardLogic (
    val _if:ICondition<MTGBoardState>,
    val _then:List<IExecutable<MTGBoardState>>,
    val _else:List<IExecutable<MTGBoardState>> = emptyList(),
) : IExecutable<MTGBoardState> {

    constructor(
        _if:ICondition<MTGBoardState>,
        _then:IExecutable<MTGBoardState>,
        _else:IExecutable<MTGBoardState>? = null,
    ) : this(
        _if = _if,
        _then = listOf(_then),
        _else = _else?.let { listOf(_else) } ?: emptyList(),
    )

    override fun execute(board: MTGBoardState): MTGBoardState {
        return if (_if.matches(board)) {
            _then.fold(board) { acc, exec -> exec.execute(acc) }
        } else {
            _else.fold(board) { acc, exec -> exec.execute(acc) }
        }
    }
}