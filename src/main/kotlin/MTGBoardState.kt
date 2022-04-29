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
        val newLibrary = library.shuffled()
        val newHand = newLibrary.take(7)
        return MTGBoardState(
            turn = 0,
            library = newLibrary.drop(7),
            hand = newHand,
            //gameLog = listOf(MTGGameAction(0, MTGGameActionType.GAME_START, "Opening hand: ${newHand.joinToString("||") { it.name }}")),
            gameLog = listOf(MTGGameAction(0, MTGGameActionType.GAME_START, "Game start")),
        )
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