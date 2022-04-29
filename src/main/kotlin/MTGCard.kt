import org.json.simple.JSONObject
import java.util.UUID

data class MTGCard(
    val uuid: UUID = UUID.randomUUID(),
    val name:String,
    val types:List<String>,
    val oracle:String,
    val backside:MTGCard?,
    //val cmc: Int?,
    val manaCost:String?,
    val json: JSONObject,
    val tapped:Boolean = false,
    ) {

    val cmc:Int
        get() = manaCost?.split('{', '}')?.filter { it.isNotEmpty() }?.sumOf { when(it) {
            "X" -> 0
            else -> it.toIntOrNull() ?: 1
        } } ?: 0

    val totalCmc:Int
        get() = if (backside != null) cmc + backside.totalCmc else cmc

    //For now, we're ignoring colors, and just focusing in cmc vs untapped lands
    fun canCastWith(lands:List<MTGLand>):Boolean = lands.count { !it.tapped } >= this.cmc

    fun isLand():Boolean = this.types.contains("Land") || this.backside?.types?.contains("Land") ?: false

    fun flip():MTGCard = if(this.backside != null) {
        this.backside.copy(
            uuid = this.uuid,
            backside = this,
        )
    } else this

    companion object {

    }
}