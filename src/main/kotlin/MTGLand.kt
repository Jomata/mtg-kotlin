import org.json.simple.JSONObject
import java.util.*

data class MTGLand (
    val uuid: UUID = UUID.randomUUID(),
    val name:String,
    val types:List<String>,
    val oracle:String,
    val backside:MTGCard?,

    val landTag:MTGLandTag,
    val producedMana: List<MTGMana>,
    val json: JSONObject,
    val tapped:Boolean = false,
) {
    //Problema potencial con el mana producido: Los pathways solo tienen un color en el produced_mana
    //Los de any color no tienen WUBRG en su color identity
    //Pero los pathway si, entonces creo que agarro el que mas abarque de entre los 2
    fun entersTapped(boardState:MTGBoardState) = !entersUntapped(boardState)

    fun entersUntapped(boardState:MTGBoardState) = when(landTag.tappedOnETB) {
        TappedOnETB.ALWAYS_UNTAPPED -> true
        TappedOnETB.ALWAYS_TAPPED -> false
        TappedOnETB.CONDITIONALLY_UNTAPPED -> when(landTag) {
            MTGLandTag.SLOW -> boardState.lands.count() >= 2
            MTGLandTag.CREATURE_AFR -> boardState.lands.count() <= 1
            else -> true
        }
    }

    fun canProduce(manaCost:String):Boolean {
        //Possible options:
        // - A color from MTGMana
        // - A number
        // - 2 colors separated by /
        return when {
            manaCost.contains("/") -> manaCost.split("/").any { canProduce(it) }
            manaCost.toIntOrNull() != null -> true
            MTGMana.values().any { it.symbol == manaCost } -> producedMana.contains(MTGMana.fromSymbol(manaCost))
            else -> false
        }
    }

    fun asMTGCard() = MTGCard(
        uuid = uuid,
        name = name,
        manaCost = null,
        types = types,
        oracle = oracle,
        backside = null,
        json = json,
        tapped = tapped,
    )

    companion object {
        fun of(card:MTGCard):MTGLand = MTGLand(
            name = card.name,
            types = card.types,
            oracle = card.oracle,
            landTag = MTGLandTag.of(card),
            producedMana =
            ( card.json.getOrDefault("color_identity", emptyList<String>()) as List<String>
            + card.json.getOrDefault("produced_mana", emptyList<String>()) as List<String>
            ).distinct().mapNotNull { MTGMana.fromSymbol(it) },
            tapped = false,
            json = card.json,
            backside = card.backside,
        )
    }
}

