data class MTGLand (
    val name:String,
    val landTag:MTGLandTag,
    val producedMana: List<MTGMana>,
    val tapped:Boolean,
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

    companion object {
        fun of(card:MTGCard):MTGLand = MTGLand(
            name = card.name,
            landTag = MTGLandTag.of(card),
            producedMana = emptyList(), //TODO: Grab the highest of color identity or produced_mana in the json objet
            tapped = false,
        )
    }
}

