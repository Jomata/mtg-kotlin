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

    val greasefangInHand = BoardQuery(MTGZone.HAND, CardQuery.parse("Greasefang"))
    val parhelionInYard = BoardQuery(MTGZone.GRAVEYARD, CardQuery.parse("Parhelion"))
    val success = MultiCondition.and(greasefangInHand, parhelionInYard)
    val greasefangInYard = BoardQuery(MTGZone.GRAVEYARD, CardQuery.parse("Greasefang"))
    val noGreasefangInHand = BoardQuery(MTGZone.HAND, CardQuery.parse("Greasefang"), ConditionOperator.EXACTLY, 0)
    val noParhelionInYard = BoardQuery(MTGZone.GRAVEYARD, CardQuery.parse("Parhelion"), ConditionOperator.EXACTLY, 0)
    val wishclawInField = BoardQuery(MTGZone.BATTLEFIELD, CardQuery.parse("Wishclaw"))
    val wishForGreasefang = MultiCondition.and(noGreasefangInHand, wishclawInField)
    val wishForGoblinEng = MultiCondition.and(noParhelionInYard, wishclawInField)
    val faithlessInYard = BoardQuery(MTGZone.GRAVEYARD, CardQuery.parse("Faithless Looting"))
    val canCastFaithless = GameQuery.canPayFor("{R}")
    val canFlashbackFaithless = GameQuery.canPayFor("{1}{R}{R}")
    val canCastGreasefang = GameQuery.canPayFor("{1}{W}{B}")

    val allConditions = listOf(
        success,
        greasefangInYard,
        wishForGreasefang,
        wishForGoblinEng,
        faithlessInYard,
        canCastFaithless,
        canFlashbackFaithless,
        canCastGreasefang,
    )

    val gameStart = MTGBoardState(library = cardList).startGame()
    //We play 10 turns
    val gameEnd = (1..10).fold(gameStart) { state, _ ->
        val afterUntap = state.nextTurn().untapStep()
        val afterDraw = if (afterUntap.turn > 1) afterUntap.drawCard(1) else afterUntap
        val afterLand = afterDraw.playLand()

        println("Turn ${afterLand.turn}")
        println("Lands: ${afterLand.lands.joinToString { it.name }}")
        //allConditions.filter { it.isTrue(afterLand)}.forEach { println("$it is true") }
        val canCastCardsInHand = state.getBothSidesOfCardsInHand().mapNotNull { it.manaCost?.ifEmpty { null } } .map { GameQuery.canPayFor(it) }
        canCastCardsInHand.forEach { println("$it is ${it.isTrue(afterLand)}") }

        afterLand
    }
    //println(gameEnd)
    //Print all the actions in the game log
    gameEnd.gameLog.forEach { println(it.info) }
}