val deckRatCar = """
    4 Haunted Ridge (MID) 263
    4 Greasefang, Okiba Boss (NEO) 220
    4 Goblin Engineer (MH1) 128
    2 Can't Stay Away (MID) 213
    4 Faithless Looting (STA) 38
    4 Parhelion II (WAR) 24
    2 Wishclaw Talisman (ELD) 110
    2 Needleverge Pathway (ZNR) 263
    3 Blightstep Pathway (KHM) 252
    3 Godless Shrine (RNA) 248
    3 Brightclimb Pathway (ZNR) 259
    4 Blood Crypt (RNA) 245
    2 Sacred Foundry (GRN) 254
    4 Bone Shards (MH2) 76
    4 Stitcher's Supplier (M19) 121
    4 Undead Butler (VOW) 133
    2 Revival // Revenge (RNA) 228
    1 Skysovereign, Consul Flagship (KLR) 272
    4 Concealed Courtyard (KLR) 282
""".trimIndent()

fun main(args: Array<String>) {
    println("Test")
    //val searchQuery = readln()
    val deckList = deckRatCar

    val cardList = MTGCard.fromArenaList(deckList)

    val greasefangInHand = BoardQuery(MTGZone.HAND,"Greasefang")
    val greasefangInField = BoardQuery(MTGZone.BATTLEFIELD,"Greasefang")
    val parhelionInYard = BoardQuery(MTGZone.GRAVEYARD, "Parhelion")
    val success = MultiCondition.and(greasefangInField, parhelionInYard)
    val greasefangInYard = BoardQuery(MTGZone.GRAVEYARD, "Greasefang")
    val noGreasefangInHandAndField = MultiCondition.and(
        BoardQuery(MTGZone.HAND, "Greasefang", ConditionOperator.EXACTLY, 0),
        BoardQuery(MTGZone.BATTLEFIELD, "Greasefang", ConditionOperator.EXACTLY, 0),
    )
    val noEngineerInHand = BoardQuery(MTGZone.HAND, "Goblin Engineer", ConditionOperator.EXACTLY, 0)
    val noParhelionInYard = BoardQuery(MTGZone.GRAVEYARD, "Parhelion", ConditionOperator.EXACTLY, 0)
    val wishclawInField = BoardQuery(MTGZone.BATTLEFIELD, "Wishclaw")
    val faithlessInYard = BoardQuery(MTGZone.GRAVEYARD, "Faithless Looting")
    val csaInYard = BoardQuery(MTGZone.GRAVEYARD, "Can't Stay Away")

    val tryCastGreasefang = MTGBoardLogic(
        BoardQuery(MTGZone.BATTLEFIELD, "Greasefang", ConditionOperator.EXACTLY, 0),
        MTGCardAction(MTGCardActionType.TRY_CAST, "Greasefang")
    )
    val tryCastGreasefangIfParhelionInYard = MTGBoardLogic(parhelionInYard, tryCastGreasefang)
    val tryCastFaithless = MTGCardAction(MTGCardActionType.TRY_CAST, "Faithless Looting")

    val tryReanimateGreaseWithCSA = MTGBoardLogic(MultiCondition.and(
        parhelionInYard,
        greasefangInYard,
    ), MTGCardAction(MTGCardActionType.TRY_CAST, "Can't Stay Away"))

    val tryReanimateGreaseWithRR = MTGBoardLogic(MultiCondition.and(
        parhelionInYard,
        greasefangInYard,
    ), MTGCardAction(MTGCardActionType.TRY_CAST, "Revival"))

    val tryReanimateGreaseWithCSAFlashback = MTGBoardLogic(MultiCondition.and(
        parhelionInYard,
        greasefangInYard,
        GameQuery.canPayFor("{3}{W}{B}"),
        csaInYard,
    ), listOf(
        MTGBoardAction(MTGBoardActionType.TAP_LANDS, 5),
        MTGCardAction(MTGCardActionType.FLASHBACK, "Can't Stay Away"),
    ))

    val flashbackFaithless = MTGBoardLogic(MultiCondition.and(
        faithlessInYard,
        GameQuery.canPayFor("{2}{R}"),
    ), listOf(
        MTGBoardAction(MTGBoardActionType.TAP_LANDS, 3),
        MTGCardAction(MTGCardActionType.FLASHBACK, "Faithless Looting")
    ))

    val tryCastFaithlessIfParhelionInHand = MTGBoardLogic(parhelionInYard, tryCastFaithless)

    val tryCastStitcher = MTGBoardLogic(MTGCardAction(MTGCardActionType.TRY_CAST, "Stitcher's Supplier"))
    val tryCastButler = MTGBoardLogic(MTGCardAction(MTGCardActionType.TRY_CAST, "Undead Butler"))

    val tryCastGoblinEngineer = MTGBoardLogic(
        noParhelionInYard,
        MTGCardAction(MTGCardActionType.TRY_CAST, "Goblin Engineer")
    )

    val wishForGreasefang = MTGBoardLogic(MultiCondition.and(
            noGreasefangInHandAndField,
            wishclawInField,
        ),
        listOf(
            //Tap 1 land, exile wishclaw from field, tutor Greasefang
            MTGBoardAction(MTGBoardActionType.TAP_LANDS, 1),
            MTGCardAction(MTGCardActionType.EXILE_FROM_FIELD, "Wishclaw"),
            MTGCardAction(MTGCardActionType.TUTOR, "Greasefang"),
        )
    )

    val wishForGoblinEngineer = MTGBoardLogic(MultiCondition.and(
        noParhelionInYard,
        noEngineerInHand,
        wishclawInField,
    ),
        listOf(
            //Tap 1 land, exile wishclaw from field, tutor Greasefang
            MTGBoardAction(MTGBoardActionType.TAP_LANDS, 1),
            MTGCardAction(MTGCardActionType.EXILE_FROM_FIELD, "Wishclaw"),
            MTGCardAction(MTGCardActionType.TUTOR, "Goblin Engineer"),
        )
    )

    val tryCastWishclaw = MTGBoardLogic(
        MultiCondition.or(
            noEngineerInHand,
            noGreasefangInHandAndField,
        ),
        MTGCardAction(MTGCardActionType.TRY_CAST, "Wishclaw")
    )

    val heuristics = listOf(
        tryCastGreasefangIfParhelionInYard,
        tryReanimateGreaseWithRR,
        tryReanimateGreaseWithCSA,
        tryReanimateGreaseWithCSAFlashback,
        wishForGreasefang,
        wishForGoblinEngineer,
        tryCastFaithlessIfParhelionInHand,
        tryCastGoblinEngineer,
        tryCastStitcher,
        tryCastButler,
        tryCastWishclaw,
        tryCastFaithless,
        tryCastGreasefang,
        flashbackFaithless,
    )

    val onStitcherETB = MTGTriggeredAction.onETB("Stitcher",MTGBoardAction(MTGBoardActionType.MILL, 3))
    val onButlerETB = MTGTriggeredAction.onETB("Undead Butler",MTGBoardAction(MTGBoardActionType.MILL, 3))
    val onFaithlessCast = MTGTriggeredAction.onCast("Faithless Looting", listOf(
        MTGBoardAction(MTGBoardActionType.DRAW, 2),
        //TODO: Better logic for discard
        MTGCardAction(MTGCardActionType.DISCARD, "type:Vehicle"),
        MTGCardAction(MTGCardActionType.DISCARD, "type:Vehicle"),
    ))
    val onGoblinETB = MTGTriggeredAction.onETB("Goblin Engineer", listOf(
        MTGCardAction(MTGCardActionType.TUTOR, "Parhelion"),
        MTGCardAction(MTGCardActionType.DISCARD, "Parhelion"),
    ))
    val triggers = listOf(
        onStitcherETB,
        onButlerETB,
        onGoblinETB,
        onFaithlessCast,
    )

//    val gameStart = MTGBoardState(deck = cardList).startGame()
//    //We play 10 turns
//    val gameEnd = (1..10).fold(gameStart) { state, _ ->
//
//
//        //println("Turn ${afterLand.turn}")
//        //println("Lands: ${afterLand.lands.joinToString { it.name }}")
//        //allConditions.filter { it.isTrue(afterLand)}.forEach { println("$it is true") }
//        //val canCastCardsInHand = state.getBothSidesOfCardsInHand().mapNotNull { it.manaCost?.ifEmpty { null } } .map { GameQuery.canPayFor(it) }
//        //canCastCardsInHand.forEach { println("$it is ${it.matches(afterLand)}") }
//
//        state.playTurn()
//    }
    //println(gameEnd)
    //Print all the actions in the game log
    val gameEnd = MTGBoardState(deck = cardList, triggers = triggers).startGame().playTurns(10, heuristics)
    gameEnd.gameLog.forEach { println(it.info) }
}