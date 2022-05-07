import org.json.simple.JSONObject
import java.util.UUID

data class MTGCard(
    val uuid: UUID = UUID.randomUUID(),
    val name:String,
    val types:List<String>,
    val oracle:String,
    val backside:MTGCard?,
    val manaCost:String?,


    val json: JSONObject,
    val tapped:Boolean = false,
    ) {

    override fun toString(): String {
        return name + (backside?.name?.let { " // $it" } ?: "")
    }

    val cmc:Int
        get() = manaCost?.let { MTGUtils.manaCostToCMC(it) } ?: 0

    val totalCmc:Int
        get() = if (backside != null) cmc + backside.totalCmc else cmc

    //For now, we're ignoring colors, and just focusing in cmc vs untapped lands
    //fun canCastWith(lands:List<MTGLand>):Boolean = lands.count { !it.tapped } >= this.cmc
    fun canCastWith(lands:List<MTGLand>):Boolean {
        val testBoard = MTGBoardState(deck = lands.map{it.asMTGCard()}, library=emptyList(), lands = lands)
        return if(manaCost != null)
            testBoard.canPayFor(manaCost)
        else
            testBoard.canPayFor(cmc)
    }

    fun isLand():Boolean = this.types.contains("Land") || this.backside?.types?.contains("Land") ?: false

    fun isPermanent():Boolean = !this.types.contains("Instant") && !this.types.contains("Sorcery")

    fun tap() = this.copy(tapped = true)

    fun flip():MTGCard = if(this.backside != null) {
        this.backside.copy(
            uuid = this.uuid,
            backside = this,
        )
    } else this

    companion object {

    }
}