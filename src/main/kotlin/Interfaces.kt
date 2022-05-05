interface ICondition<T> {
    fun matches(target: T): Boolean
}

interface IExecutable<T> {
    fun execute(target: T): T
}