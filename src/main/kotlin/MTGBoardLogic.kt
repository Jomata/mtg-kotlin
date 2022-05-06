data class MTGBoardLogic (
    val _if:ICondition<MTGBoardState>? = null,
    val _then:List<IExecutable<MTGBoardState>> = emptyList(),
    val _else:List<IExecutable<MTGBoardState>> = emptyList(),
) : IExecutable<MTGBoardState> {

    constructor(
        _if:ICondition<MTGBoardState>? = null,
        _then:IExecutable<MTGBoardState>,
        _else:IExecutable<MTGBoardState>? = null,
    ) : this(
        _if = _if,
        _then = listOf(_then),
        _else = _else?.let { listOf(_else) } ?: emptyList(),
    )

    constructor(action: IExecutable<MTGBoardState>) : this(null,action,null)

    override fun execute(board: MTGBoardState): MTGBoardState {
        return if (_if?.matches(board) != false) { //If the condition is null, or evaluates to true
            _then.fold(board) { acc, exec -> exec.execute(acc) }
        } else {
            _else.fold(board) { acc, exec -> exec.execute(acc) }
        }
    }
}