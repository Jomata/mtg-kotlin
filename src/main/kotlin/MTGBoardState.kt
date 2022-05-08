data class MTGBoardState(
    val deck:List<MTGCard>,
    val triggers:List<MTGTriggeredAction> = emptyList(),
    val library:List<MTGCard> = deck,
    val turn:Int = 0,
    val hand:List<MTGCard> = emptyList(),
    val lands:List<MTGLand> = emptyList(),
    val field:List<MTGCard> = emptyList(),
    val yard:List<MTGCard> = emptyList(),
    val exile:List<MTGCard> = emptyList(),
    val stack:List<MTGCard> = emptyList(),
    val gameLog: List<MTGGameLog> = emptyList(),
) {
    //We assert for validity in the constructor after values are set
    init {
        //We assert that library, hand, lands, field, yard, and exile sizes added together are equal to the starting deck size
        val cardsInGame = library.size + hand.size + lands.size + field.size + yard.size + exile.size + stack.size
        require(cardsInGame == deck.size) { gameLog.joinToString { "\n" } }
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
            deck = deck,
            triggers = triggers,
            turn = 0,
            //hand = newHand,
            //gameLog = listOf(MTGGameAction(0, MTGGameActionType.GAME_START, "Opening hand: ${newHand.joinToString("||") { it.name }}")),
            gameLog = listOf(MTGGameLog(0, MTGGameLogType.GAME_START, "Game start")),
        ).mulliganCheck(0)
    }

    private fun keepOpeningHand(hand:List<MTGCard>):Boolean {
        return hand.count { it.isLand() } >= 2 && hand.count { !it.isLand() } >= 2
    }

    private fun mulliganCheck(mulligansSoFar:Int):MTGBoardState {
        val shuffledLibrary = deck.shuffled()
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
                    MTGGameLog(turn, MTGGameLogType.MULLIGAN_CHECK, "Keeping ${7 - mulligansSoFar} cards"),
                    if (cardsToBottom.isNotEmpty())
                        MTGGameLog(turn, MTGGameLogType.MULLIGAN_CHECK, "Bottoming ${cardsToBottom.joinToString("||") { it.name }}")
                    else
                        null
                )
            )
        } else {
            return this.copy(
                library = shuffledLibrary,
                hand = emptyList(),
                gameLog = gameLog + MTGGameLog(turn, MTGGameLogType.MULLIGAN_CHECK, "Mulliganing ${openingHand.joinToString("||") { it.name }}")
            ).mulliganCheck(mulligansSoFar + 1)
        }

    }

    fun nextTurn(): MTGBoardState {
        val newTurn = turn + 1
        return copy(
            turn = newTurn,
            gameLog = gameLog + MTGGameLog(newTurn, MTGGameLogType.TURN_START, "Turn $newTurn start")
                + MTGGameLog(newTurn, MTGGameLogType.ZONE_HAND, "Hand :" + hand.joinToString("||") { it.name })
                + listOfNotNull(
                if (lands.isNotEmpty()) MTGGameLog( newTurn, MTGGameLogType.ZONE_LANDS, "Land :" + lands.joinToString("||") { it.name }) else null,
                if (field.isNotEmpty()) MTGGameLog( newTurn, MTGGameLogType.ZONE_FIELD, "Field:" + field.joinToString("||") { it.name }) else null,
                if (yard.isNotEmpty()) MTGGameLog( newTurn, MTGGameLogType.ZONE_YARD, "Yard :" + yard.joinToString("||") { it.name }) else null,
                if (exile.isNotEmpty()) MTGGameLog( newTurn, MTGGameLogType.ZONE_EXILE, "Exile:" + exile.joinToString("||") { it.name }) else null
            ),
        )
    }
    

    fun untapStep():MTGBoardState = this.copy(
        lands = this.lands.map { it.copy ( tapped = false)},
        field = this.field.map { it.copy ( tapped = false)},
    )

    fun upkeepStep():MTGBoardState = this.copy()

    fun cleanupStep():MTGBoardState = this.copy()

    fun playTurn(actions:List<IExecutable<MTGBoardState>> = emptyList()):MTGBoardState {
        //Advance turn
        //Untap step
        //Upkeep step
        val afterUpkeep = this.nextTurn().untapStep().upkeepStep()
        //Draw a card if turn > 1
        val afterDraw = if (afterUpkeep.turn > 1) afterUpkeep.drawCards(1) else afterUpkeep
        //Play a default land
        val afterLand = afterDraw.playLand()
        //Execute all the actions indicated in the logic
        //TODO: Como estoy usando fold en el .execute, solo estoy ejecutando maximo una vez cada accion que aplica
        val afterActions = afterLand.runActions(actions)
        //End of turn step
        //Cleanup step
        return afterActions.cleanupStep()
    }

    fun playTurns(turns:Int, actions:List<IExecutable<MTGBoardState>> = emptyList()):MTGBoardState {
        require(turns > 0)
        return (1..turns).fold(this) { state, _ -> state.playTurn(actions) }
    }

    fun playUntilWinCon(winCondition: ICondition<MTGBoardState>, actions:List<IExecutable<MTGBoardState>> = emptyList()):MTGBoardState {
        return if(winCondition.matches(this) || turn > 50)
            this
        else
            this.playTurn(actions).playUntilWinCon(winCondition, actions)
    }

    private fun runActions(actions:List<IExecutable<MTGBoardState>>, recursive: Boolean = true):MTGBoardState {
        val after = actions.fold(this) { acc, action -> action.execute(acc) }
        //We keep on running the actions until the board state doesn't change
        return if(after != this && recursive)
            after.runActions(actions)
        else
            after
    }

    fun drawCards(howMany:Int): MTGBoardState {
        val draw = library.take(howMany)
        val newHand = hand.plus(draw)
        val newLibrary = library.drop(howMany)
        return this.copy(
            library = newLibrary,
            hand = newHand,
            gameLog = gameLog + MTGGameLog(turn, MTGGameLogType.CARD_DRAW, "Draw $howMany: ${
                draw.joinToString(
                    "||"
                ) { it.name }
            }")
        )
    }

    fun millCards(howMany:Int): MTGBoardState {
        return copy(
            library = library.drop(howMany),
            yard = yard + library.take(howMany),
            gameLog = gameLog + MTGGameLog(turn, MTGGameLogType.CARD_MILL, "Milling $howMany: ${
                library.take(howMany).joinToString(
                    "||"
                ) { it.name }
            }")
        )
    }

    fun tapLands(howMany: Int): MTGBoardState {
        val landsToTap = lands.filter { !it.tapped }.take(howMany)
        return copy(
            lands = lands - landsToTap.toSet() + landsToTap.map { it.tap() },
            gameLog = gameLog + MTGGameLog(turn, MTGGameLogType.TAP_LANDS, "Tapping $howMany lands")
        )
    }

    fun tapPermanent(query: CardQuery): MTGBoardState {
        val match = field.firstOrNull { query.matches(it) } ?: return this
        return copy(
            field = field.filter { it != match } + match.tap(),
            gameLog = gameLog + MTGGameLog(turn, MTGGameLogType.TAP_PERMANENT, "Tapping ${match.name}")
        )
    }

    fun cast(query: CardQuery) : MTGBoardState {
        val match = hand.firstOrNull { query.matches(it) } ?: return this
        val onStack = copy(
            hand = hand - match,
            stack = stack + match,
            gameLog = gameLog + MTGGameLog(turn, MTGGameLogType.CAST, "Casting ${match.name}"),
        )
        //TODO: Confirmar que no hay problemas por aplanar todas las acciones y ejecutarlas seguidas usando flatmap
        val onCastActions = triggers.filter{ it.trigger == MTGTrigger.CAST && it.forCard.matches(match) }.flatMap{it.action}
        val afterCastTriggers = onStack.runActions(onCastActions, false)
        return if(match.isPermanent()) {
            //Move card from hand to field
            val onETBActions = triggers.filter{ it.trigger == MTGTrigger.ETB && it.forCard.matches(match) }.flatMap{it.action}
            afterCastTriggers.copy(
                //hand = hand.filter { it != match },
                stack = afterCastTriggers.stack - match,
                field = afterCastTriggers.field + match,
            ).runActions(onETBActions, false)
        } else {
            //Move card from hand to graveyard
            afterCastTriggers.copy(
                stack = afterCastTriggers.stack - match,
                yard = afterCastTriggers.yard + match,
            )
        }
    }

    fun tryCast(query: CardQuery) : MTGBoardState {
        val match = deck.firstOrNull { query.matches(it) } ?: return this
        val manaCost = match.manaCost ?: "{0}"
        val checkCardInHand = BoardQuery(MTGZone.HAND, query)
        val checkCanPay = GameQuery.canPayFor(manaCost)
        val actionTapMana = MTGBoardAction(MTGBoardActionType.TAP_LANDS, MTGUtils.manaCostToCMC(manaCost))
        val actionCast = MTGCardAction(MTGCardActionType.CAST, query)
        val logic = MTGBoardLogic(
            MultiCondition.and(
                checkCardInHand,
                checkCanPay,
            ),
            listOf(
                actionTapMana,
                actionCast,
            ),
        )
        return logic.execute(this)
    }

    fun tutor(query: CardQuery) : MTGBoardState {
        val match = library.firstOrNull { query.matches(it) } ?: return this
        //Move match from library to hand, then shuffle library
        return copy(
            hand = hand + match,
            library = library.filter { it != match }.shuffled(),
            gameLog = gameLog + MTGGameLog(turn, MTGGameLogType.TUTOR, "Tutoring ${match.name}")
        )
    }

    fun discard(query: CardQuery) : MTGBoardState {
        val match = hand.firstOrNull { query.matches(it) } ?: return this
        //Move match from hand to yard
        return this.copy(
            hand = hand - match,
            yard = yard + match,
            gameLog = gameLog + MTGGameLog(turn, MTGGameLogType.DISCARD, "Discarding ${match.name}")
        )
    }

    fun destroy(query: CardQuery) : MTGBoardState {
        val match = field.firstOrNull { query.matches(it) } ?: return this
        //Move match from field to graveyard
        return this.copy(
            field = field - match,
            yard = yard + match,
            gameLog = gameLog + MTGGameLog(turn, MTGGameLogType.DESTROY, "Destroying ${match.name}")
        )
    }

    fun recover(query: CardQuery) : MTGBoardState {
        val match = yard.firstOrNull { query.matches(it) } ?: return this
        //Move match from graveyard to hand
        return this.copy(
            yard = yard - match,
            hand = hand + match,
            gameLog = gameLog + MTGGameLog(turn, MTGGameLogType.RECOVER, "Recovering ${match.name}")
        )
    }

    fun flashback(query: CardQuery) : MTGBoardState {
        val match = this.yard.firstOrNull { query.matches(it) } ?: return this
        val onCastTriggers = triggers.filter{ it.trigger == MTGTrigger.CAST && it.forCard.matches(match) }.flatMap{it.action}
        val afterCasting = copy(
            yard = yard - match,
            stack = stack + match,
            gameLog = gameLog + MTGGameLog(turn, MTGGameLogType.FLASHBACK, "Flashback ${match.name}"),
        ).runActions(onCastTriggers, false)
        return if(match.isPermanent()) {
            //Move card from yard to field and trigger ETBs
            val onETBTriggers = triggers.filter{ it.trigger == MTGTrigger.ETB && it.forCard.matches(match) }.flatMap{it.action}
            afterCasting.copy(
                stack = afterCasting.stack - match,
                field = afterCasting.field + match,
            ).runActions(onETBTriggers, false)
        } else {
            //Move card from yard to exile
            afterCasting.copy(
                stack = afterCasting.stack - match,
                exile = afterCasting.exile + match,
            )
        }
    }

    fun reanimate(query: CardQuery) : MTGBoardState {
        val match = this.yard.firstOrNull { query.matches(it) && it.isPermanent() } ?: return this
        val onETBTriggers = triggers.filter{ it.trigger == MTGTrigger.ETB && it.forCard.matches(match) }.flatMap{it.action}
        return this.copy(
            yard = yard - match,
            field = field + match,
            gameLog = gameLog + MTGGameLog(turn, MTGGameLogType.REANIMATE, "Reanimating ${match.name}")
        ).runActions(onETBTriggers, false)
    }

    fun exileFromYard(query: CardQuery) : MTGBoardState {
        val match = this.yard.firstOrNull { query.matches(it) } ?: return this
        //Move match from yard to exile
        return this.copy(
            yard = this.yard - match,
            exile = this.exile + match,
            gameLog = gameLog + MTGGameLog(turn, MTGGameLogType.EXILE, "Exiling ${match.name}")
        )
    }

    fun exileFromField(query: CardQuery) : MTGBoardState {
        val match = this.field.firstOrNull { query.matches(it) } ?: return this
        //Move match from field to exile
        return this.copy(
            field = this.field - match,
            exile = this.exile + match,
            gameLog = gameLog + MTGGameLog(turn, MTGGameLogType.EXILE, "Exiling ${match.name}")
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
                gameLog = gameLog + MTGGameLog(turn, MTGGameLogType.PLAY_LAND, "Play ${land.name}")
            )
        } else this
    }

    fun canPayFor(cmc: Int): Boolean {
        return this.lands.count { !it.tapped } >= cmc
    }

    //TODO: Keep improving this method
    //Right now, we're only validating that:
    // - We have enough untapped lands for the CMC
    // - We can produce mana of all colors needed
    fun canPayFor(manaCost: String): Boolean {
        val cmc = MTGUtils.manaCostToCMC(manaCost)
        if(!canPayFor(cmc)) return false

        val manaSymbols = manaCost.split('{', '}')?.filter { it.isNotEmpty() }
        return manaSymbols.all { mana ->
            this.lands.any { l -> l.canProduce(mana)}
        }
    }
}