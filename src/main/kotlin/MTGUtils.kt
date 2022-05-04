class MTGUtils {
    companion object {
        fun manaCostToCMC(manaCost: String): Int = manaCost.split('{', '}')?.filter { it.isNotEmpty() }?.sumOf {
            when (it) {
                "X" -> 0
                else -> it.toIntOrNull() ?: 1
            }
        }
    }
}