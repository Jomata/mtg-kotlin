data class MTGBoardState(
    val library:List<MTGCard> = emptyList(),
    val turn:Int = 0,
    val hand:List<MTGCard> = emptyList(),
    val lands:List<MTGLand> = emptyList(),
    val field:List<MTGCard> = emptyList(),
    val yard:List<MTGCard> = emptyList(),
    val exile:List<MTGCard> = emptyList(),
    val gameLog: List<MTGGameAction> = emptyList(),
) {
    //We assert for validity in the constructor after values are set
    init {
        //We assert that library, hand, lands, field, yard, and exile sizes added together are equal to the starting deck size
        val startingDeckSize = 60
        val cardsInGame = library.size + hand.size + lands.size + field.size + yard.size + exile.size
        assert(cardsInGame == startingDeckSize)
    }

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

    //Since BoardState is immutable, we can't just add a card to the hand or similar, we need to create a new boardstate and return that
    //So all methods that modify the boardstate should return a new boardstate

    fun startGame(): MTGBoardState {
        //val newLibrary = library.shuffled()
        //val newHand = newLibrary.take(7)
        return MTGBoardState(
            turn = 0,
            library = this.library,
            //hand = newHand,
            //gameLog = listOf(MTGGameAction(0, MTGGameActionType.GAME_START, "Opening hand: ${newHand.joinToString("||") { it.name }}")),
            gameLog = listOf(MTGGameAction(0, MTGGameActionType.GAME_START, "Game start")),
        ).mulliganCheck(0)
    }

    private fun keepOpeningHand(hand:List<MTGCard>):Boolean {
        return hand.count { it.isLand() } >= 2
    }

    fun mulliganCheck(mulligansSoFar:Int):MTGBoardState {
        val shuffledLibrary = library.shuffled()
        val openingHand = shuffledLibrary.take(7)
        val openingLibrary = shuffledLibrary.drop(7)

        //If we already took 5 mulligans, or keepOpeningHand is true, we keep the hand and choose mulligansSoFar cards to put in the bottom of the library
        //Else, we shuffle the library, generate a new opening hand, and check for mulligan again
        if(mulligansSoFar >= 5 || keepOpeningHand(openingHand)) {
            //TODO: Add logic to keep/bottom cards from opening hand
            //For now, we keep 2 lands, and put the rest in the bottom of the library
            val openingHandLands = openingHand.filter { it.isLand() }.take(2).toSet()
            val openingHandNonLands = (openingHand - openingHandLands).take(7 - openingHandLands.size - mulligansSoFar).toSet()
            val cardsToBottom = (openingHand - openingHandLands - openingHandNonLands)
            return this.copy(
                library = openingLibrary + cardsToBottom, //We take from the start, so putting cards to the bottom means putting them at the end
                hand = (openingHandLands + openingHandNonLands).toList(),
                gameLog = gameLog + listOfNotNull(
                    MTGGameAction(turn, MTGGameActionType.MULLIGAN_CHECK, "Keeping ${7 - mulligansSoFar} cards"),
                    if (cardsToBottom.isNotEmpty())
                        MTGGameAction(turn, MTGGameActionType.MULLIGAN_CHECK, "Bottoming ${cardsToBottom.joinToString("||") { it.name }}")
                    else
                        null
                )
            )
        } else {
            return this.copy(
                library = shuffledLibrary,
                hand = emptyList(),
                gameLog = gameLog + MTGGameAction(turn, MTGGameActionType.MULLIGAN_CHECK, "Mulliganing ${openingHand.joinToString("||") { it.name }}")
            ).mulliganCheck(mulligansSoFar + 1)
        }

    }

    fun nextTurn(): MTGBoardState  = this.copy(
        turn = this.turn + 1,
        gameLog = this.gameLog + MTGGameAction(this.turn + 1, MTGGameActionType.TURN_START, "Turn ${this.turn+1} start: ${this.hand.joinToString("||") { it.name }}")
    )

    fun untapStep():MTGBoardState = this.copy(
        lands = this.lands.map { it.copy ( tapped = false)},
        field = this.field.map { it.copy ( tapped = false)},
    )

    fun drawCard(howMany:Int): MTGBoardState {
        val draw = library.take(howMany)
        val newHand = hand.plus(draw)
        val newLibrary = library.drop(howMany)
        return this.copy(
            library = newLibrary,
            hand = newHand,
            gameLog = gameLog + MTGGameAction(turn, MTGGameActionType.CARD_DRAW, "Draw $howMany: ${
                draw.joinToString(
                    "||"
                ) { it.name }
            }")
        )
    }

    fun playLand():MTGBoardState {
        val land = chooseLand()
        //If we have a land to play, we do, if not, we return the same board state
        return land ?.let { this.playLand(land) } ?: this
    }

    fun playLand(land: MTGLand): MTGBoardState {
        val cardFromHand = this.hand.firstOrNull { it.isLand() && (it.name == land.name || it.backside?.name == land.name) }
        return if(cardFromHand != null) {
            val newHand = hand - cardFromHand
            val newLands = lands + land
            this.copy(
                hand = newHand,
                lands = newLands,
                gameLog = gameLog + MTGGameAction(turn, MTGGameActionType.PLAY_LAND, "Play ${land.name}")
            )
        } else this
    }
}