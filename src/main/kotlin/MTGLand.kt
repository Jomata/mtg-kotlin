data class MTGLand (
    val name:String,
    val landTag:MTGLandTag,
    val producedMana: List<MTGMana>,
    val tapped:Boolean,
) {
    //Problema potencial con el mana producido: Los pathways solo tienen un color en el produced_mana
    //Los de any color no tienen WUBRG en su color identity
    //Pero los pathway si, entonces creo que agarro el que mas abarque de entre los 2
    fun entersTapped(boardState:MTGBoardState) = when(landTag.tappedOnETB) {
        TappedOnETB.ALWAYS_UNTAPPED -> false
        TappedOnETB.ALWAYS_TAPPED -> true
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

data class MTGBoardState(val hand:List<MTGCard>, val lands:List<MTGLand>) {
    fun getBothSidesOfCardsInHand():List<MTGCard> = this.hand.flatMap { listOf(it, it.backside) }.filterNotNull()

    fun chooseLand(): MTGLand? {
        val availableCards = getBothSidesOfCardsInHand()
        val availableLands = availableCards.filter { it.isLand() }.map { MTGLand.of(it) }
        val untappedLands = availableLands.filter { !it.entersTapped(this) }
        val tappedLands = availableLands.filter { it.entersTapped(this) }
        //If all the lands we have enter tapped, we choose one that always enters tapped in case we can play an untapped land later
        if(untappedLands.isEmpty() && tappedLands.isNotEmpty()) {
            return tappedLands.firstOrNull { it.landTag.tappedOnETB == TappedOnETB.ALWAYS_TAPPED } ?: tappedLands.first()
        }
        //If we have lands that enter untapped, we check if we have plays to use that mana
        else if(untappedLands.isNotEmpty()) {

            val currentPossiblePlayCount = hand.count { it.canCastWith(lands) }
            val landsThatUnlockNewPlays = untappedLands.filter {newLand -> hand.count { it.canCastWith( lands.plus(newLand.copy(tapped = false)) ) } > currentPossiblePlayCount }

            if(landsThatUnlockNewPlays.isNotEmpty()) {
                //If we have lands that enter untapped, AND mana plays for current lands + 1, we prioritize lands that enter untapped conditionally
                return landsThatUnlockNewPlays.firstOrNull { it.landTag.tappedOnETB == TappedOnETB.CONDITIONALLY_UNTAPPED } ?: landsThatUnlockNewPlays.first()
            }
            else {
                //If we have lands that enter untapped, but no mana plays for current lands + 1, we prioritize lands that always enter tapped
                return availableLands.firstOrNull { it.landTag.tappedOnETB == TappedOnETB.ALWAYS_TAPPED } ?:
                //Followed by conditionally that would enter tapped this turn
                availableLands.firstOrNull { it.entersTapped(this) } ?:
                //Followed by any other conditional
                availableLands.firstOrNull { it.landTag.tappedOnETB == TappedOnETB.CONDITIONALLY_UNTAPPED } ?:
                //And only always untapped should be left
                availableLands.first()
                //This can be tricky with fastlands or creaturelands, since they would then stop coming in untapped
                //So T1 with no plays you want a tapland, since you can still play a creatureland on 2, unless you have 2 creaturelands you want both in T1 and T2
            }
        } else {
            //No lands of any kind to play, null
            return null
        }
    }
}