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
//            if(_if != null) {
//                println("$_if is true")
//                println("Executing $_then")
//            }
            _then.fold(board) { acc, exec -> exec.execute(acc) }
        } else if(_else.isNotEmpty()) {
//            println("$_if is false")
//            println("Executing $_else")
            _else.fold(board) { acc, exec -> exec.execute(acc) }
        } else {
            board
        }
    }
}