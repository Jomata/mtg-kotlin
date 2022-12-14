class RatCarSample {
    companion object {
    
    private val greasefangInHand = BoardQuery(MTGZone.HAND,"Greasefang")
    private val greasefangInField = BoardQuery(MTGZone.BATTLEFIELD,"Greasefang")
    private val parhelionInYard = BoardQuery(MTGZone.GRAVEYARD, "Parhelion")
    
    private val greasefangInYard = BoardQuery(MTGZone.GRAVEYARD, "Greasefang")
    private val noGreasefangInHandAndField = MultiCondition.and(
        BoardQuery(MTGZone.HAND, "Greasefang", ConditionOperator.EXACTLY, 0),
        BoardQuery(MTGZone.BATTLEFIELD, "Greasefang", ConditionOperator.EXACTLY, 0),
    )
    private val noEngineerInHand = BoardQuery(MTGZone.HAND, "Goblin Engineer", ConditionOperator.EXACTLY, 0)
    private val noParhelionInYard = BoardQuery(MTGZone.GRAVEYARD, "Parhelion", ConditionOperator.EXACTLY, 0)
    private val wishclawInField = BoardQuery(MTGZone.BATTLEFIELD, "Wishclaw")
    private val faithlessInYard = BoardQuery(MTGZone.GRAVEYARD, "Faithless Looting")
    private val csaInYard = BoardQuery(MTGZone.GRAVEYARD, "Can't Stay Away")

    private val tryCastGreasefang = MTGBoardLogic(
        BoardQuery(MTGZone.BATTLEFIELD, "Greasefang", ConditionOperator.EXACTLY, 0),
        MTGCardAction(MTGCardActionType.TRY_CAST, "Greasefang")
    )
    private val tryCastGreasefangIfParhelionInYard = MTGBoardLogic(parhelionInYard, tryCastGreasefang)
    private val tryCastFaithless = MTGCardAction(MTGCardActionType.TRY_CAST, "Faithless Looting")

    private val tryReanimateGreaseWithCSA = MTGBoardLogic(MultiCondition.and(
        parhelionInYard,
        greasefangInYard,
    ), MTGCardAction(MTGCardActionType.TRY_CAST, "Can't Stay Away"))

    private val tryReanimateGreaseWithRR = MTGBoardLogic(MultiCondition.and(
        parhelionInYard,
        greasefangInYard,
    ), MTGCardAction(MTGCardActionType.TRY_CAST, "Revival"))

    private val tryReanimateGreaseWithSorin = MTGBoardLogic(MultiCondition.and(
        parhelionInYard,
        greasefangInYard,
    ), MTGCardAction(MTGCardActionType.TRY_CAST, "Sorin, Vengeful Bloodlord"))

    private val tryReanimateGreaseWithCSAFlashback = MTGBoardLogic(MultiCondition.and(
        parhelionInYard,
        greasefangInYard,
        GameQuery.canPayFor("{3}{W}{B}"),
        csaInYard,
    ), listOf(
        MTGBoardAction(MTGBoardActionType.TAP_LANDS, 5),
        MTGCardAction(MTGCardActionType.FLASHBACK, "Can't Stay Away"),
    ))

    private val flashbackFaithless = MTGBoardLogic(MultiCondition.and(
        faithlessInYard,
        GameQuery.canPayFor("{2}{R}"),
    ), listOf(
        MTGBoardAction(MTGBoardActionType.TAP_LANDS, 3),
        MTGCardAction(MTGCardActionType.FLASHBACK, "Faithless Looting")
    ))

    private val tryCastFaithlessIfParhelionInHand = MTGBoardLogic(parhelionInYard, tryCastFaithless)

    private val tryCastStitcher = MTGBoardLogic(MTGCardAction(MTGCardActionType.TRY_CAST, "Stitcher's Supplier"))
    private val tryCastButler = MTGBoardLogic(MTGCardAction(MTGCardActionType.TRY_CAST, "Undead Butler"))
    private val tryCastVampire = MTGBoardLogic(MTGCardAction(MTGCardActionType.TRY_CAST, "type:Vampire"))
    private val tryCastKiki = MTGBoardLogic(MTGCardAction(MTGCardActionType.TRY_CAST, "Fable of the Mirror-Breaker"))

    private val lootWithBloodToken = MTGBoardLogic(
        MultiCondition.and(
            GameQuery.canPayFor(1),
            BoardQuery(MTGZone.BATTLEFIELD, "type:Vampire"),
            BoardQuery(MTGZone.HAND, "type:Vehicle"),
        ),
        listOf(
            MTGBoardAction(MTGBoardActionType.TAP_LANDS, 1),
            MTGCardAction(MTGCardActionType.DESTROY, "type:Vampire"),
            MTGCardAction(MTGCardActionType.DISCARD, "type:Vehicle"),
            MTGBoardAction(MTGBoardActionType.DRAW, 1),
        )
    )

    private val tryCastDeadlyIfZombieInField = MTGBoardLogic(
        MultiCondition.and(
            GameQuery.canPayFor("{1}{B}"),
            BoardQuery(MTGZone.BATTLEFIELD, "type:Zombie"),
            BoardQuery(MTGZone.HAND, "Deadly Dispute"),
        ),
        listOf(
            MTGBoardAction(MTGBoardActionType.TAP_LANDS, 2),
            MTGCardAction(MTGCardActionType.DESTROY, "type:Zombie"),
            MTGCardAction(MTGCardActionType.CAST, "Deadly Dispute"),
        )
    )

    private val tryCastGoblinEngineer = MTGBoardLogic(
        noParhelionInYard,
        MTGCardAction(MTGCardActionType.TRY_CAST, "Goblin Engineer")
    )

    private val wishForGreasefang = MTGBoardLogic(MultiCondition.and(
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

    private val wishForGoblinEngineer = MTGBoardLogic(MultiCondition.and(
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

    private val tryCastWishclaw = MTGBoardLogic(
        MultiCondition.or(
            noEngineerInHand,
            noGreasefangInHandAndField,
        ),
        MTGCardAction(MTGCardActionType.TRY_CAST, "Wishclaw")
    )
        //Kiki should loot on step 2, but since we haven't implemented Sagas, we loot on ETB
        //If we have reanimate in hand, greasefang in hand, and vehicle in yard, we discard greasefang
        //If we have vehicle in hand, we discard vehicle
        //If we have 2 or more greasefangs in hand, we discard one
        private val reanimatorInHand = MultiCondition.or(
            BoardQuery.hasCardInHand("Revival"),
            BoardQuery.hasCardInHand("Can't Stay Away"),
            BoardQuery.hasCardInHand("Sorin, Vengeful Bloodlord"),
        )
        private val vehicleInHand = BoardQuery.hasCardInHand("type:Vehicle")
        private val kikiLootLogic = MTGBoardLogic(
            _if = MultiCondition.and(parhelionInYard, reanimatorInHand, greasefangInHand),
            _then = listOf(
                MTGCardAction(MTGCardActionType.DISCARD, "Greasefang"),
                MTGBoardAction(MTGBoardActionType.DRAW, 1)
            ),
            _else = MTGBoardLogic(
                _if =  vehicleInHand,
                _then = listOf(
                    MTGCardAction(MTGCardActionType.DISCARD, "type:Vehicle"),
                    MTGBoardAction(MTGBoardActionType.DRAW, 1)
                ),
                _else = MTGBoardLogic(
                    _if = BoardQuery(MTGZone.HAND, "type:Greasefang", ConditionOperator.AT_LEAST, 2),
                    _then = listOf(MTGCardAction(MTGCardActionType.DISCARD, "Greasefang"), MTGBoardAction(MTGBoardActionType.DRAW, 1)),
                    _else = emptyList(),
                )
            )
        )
        private val onKikiETB = MTGTriggeredAction.onETB("Fable of the Mirror-Breaker", listOf(kikiLootLogic, kikiLootLogic))

        private val onStitcherETB = MTGTriggeredAction.onETB("Stitcher",MTGBoardAction(MTGBoardActionType.MILL, 3))
        private val onStitcherDestroyed = MTGTriggeredAction.onDestroyed("Stitcher",MTGBoardAction(MTGBoardActionType.MILL, 3))

        private val onButlerETB = MTGTriggeredAction.onETB("Undead Butler",MTGBoardAction(MTGBoardActionType.MILL, 3))
        private val onButlerDestroyed = MTGTriggeredAction.onDestroyed("Undead Butler",MTGCardAction(MTGCardActionType.RECOVER, "Greasefang"))

        private val faithlessDiscardLogic = MTGBoardLogic(
            _if = BoardQuery.hasCardInHand("type:Vehicle"),
            _then = MTGCardAction(MTGCardActionType.DISCARD, "type:Vehicle"),
            _else = MTGBoardLogic(
                _if = MultiCondition.or(
                    MultiCondition.and(
                        BoardQuery(MTGZone.LANDS, "type:Land", ConditionOperator.AT_LEAST, 3),
                        BoardQuery.hasCardInHand( "type:Land"),
                    ),
                    BoardQuery(MTGZone.HAND, "type:Land", ConditionOperator.AT_LEAST, 2),
                ),
                _then = MTGCardAction(MTGCardActionType.DISCARD, "type:Land"),
                _else = MTGBoardLogic(
                    _if = BoardQuery.hasCardInHand("Faithless Looting"),
                    _then = MTGCardAction(MTGCardActionType.DISCARD, "Faithless Looting"),
                    _else = MTGBoardLogic(
                        _if = BoardQuery.hasCardInHand("type:Zombie"),
                        _then = MTGCardAction(MTGCardActionType.DISCARD, "type:Zombie"),
                        _else = MTGBoardLogic(
                            _if = BoardQuery.hasCardInHand("type:Vampire"),
                            _then = MTGCardAction(MTGCardActionType.DISCARD, "type:Vampire"),
                            _else = MTGCardAction(MTGCardActionType.DISCARD, "*"),
                        )
                    )
                )
            )
        )

        private val onDeadlyDisputeCast = MTGTriggeredAction.onCast("Deadly Dispute",MTGBoardAction(MTGBoardActionType.DRAW, 2))

        private val onFaithlessCast = MTGTriggeredAction.onCast("Faithless Looting", listOf(
            MTGBoardAction(MTGBoardActionType.DRAW, 2),
            //For first discard
            //1. If vehicle in hand, discard vehicle
            //2. If 2 lands in hand, or 3 lands in field, discard land
            //3. If a card named Faithless Looting in hand, discard it
            //4. If a card of type zombie in hand, discard it
            //5. Instant or sorcery
            //6. Discard anything
            faithlessDiscardLogic,
            //For second discard
            //1. If reanimate and greasefang in hand, then discard greasefang
            //The rest as above
            MTGBoardLogic(
                _if = MultiCondition.and(
                    BoardQuery.hasCardInHand("Greasefang"),
                    MultiCondition.or(
                        BoardQuery.hasCardInHand("Revival"),
                        BoardQuery.hasCardInHand("Can't Stay Away"),
                    )
                ),
                _then = MTGCardAction(MTGCardActionType.DISCARD, "Greasefang"),
                _else = faithlessDiscardLogic,
            ))
        )
        private val onGoblinETB = MTGTriggeredAction.onETB("Goblin Engineer", listOf(
            MTGCardAction(MTGCardActionType.TUTOR, "Parhelion"),
            MTGCardAction(MTGCardActionType.DISCARD, "Parhelion"),
        ))

        private val onRevivalCast = MTGTriggeredAction.onCast("Revival", MTGCardAction(MTGCardActionType.REANIMATE, "Greasefang"))
        private val onCSACast = MTGTriggeredAction.onCast("Can't Stay Away", MTGCardAction(MTGCardActionType.REANIMATE, "Greasefang"))
        private val onSorinETB = MTGTriggeredAction.onETB("Sorin", MTGCardAction(MTGCardActionType.REANIMATE, "Greasefang"))

        val deck = """
    4 Greasefang, Okiba Boss (NEO) 220
    4 Parhelion II (WAR) 24
    1 Skysovereign, Consul Flagship (KLR) 272
    2-4 Goblin Engineer (MH1) 128
    4-4 Stitcher's Supplier (M19) 121
    0-4 Undead Butler (VOW) 133
    0-4 Can't Stay Away (MID) 213
    0-4 Revival // Revenge (RNA) 228
    0-4 Bone Shards (MH2) 76
    4-4 Faithless Looting (STA) 38
    0-2 Wishclaw Talisman (ELD) 110
    0-4 Blightstep Pathway (KHM) 252
    0-4 Brightclimb Pathway (ZNR) 259
    0-4 Needleverge Pathway (ZNR) 263
    4-4 Blood Crypt (RNA) 245
    4-4 Godless Shrine (RNA) 248
    0-4 Sacred Foundry (GRN) 254
    0-4 Haunted Ridge (MID) 263
    0-4 Shattered Sanctum (VOW) 264
    0-4 Sundown Pass (VOW) 266
    0-4 Concealed Courtyard (KLR) 282
    0-4 Inspiring Vantage (KLR) 246
""".trimIndent()

        val deckExplorer = """
    4 Parhelion II (WAR) 24
    2 Skysovereign, Consul Flagship (KLR) 272
    4 Greasefang, Okiba Boss (NEO) 220
    4 Thoughtseize (THS) 107
    4 Fatal Push (KLR) 84
    0-2 Sorin, Vengeful Bloodlord (WAR) 217
    4 Stitcher's Supplier (M19) 121
    0-4 Voldaren Epicure (VOW) 182
    0-4 Undead Butler (VOW) 133
    0-4 Bloodtithe Harvester (VOW) 232
    0-4 Deadly Dispute (AFR) 94
    0-4 Can't Stay Away (MID) 213
    0-4 Revival // Revenge (RNA) 228
    0-4 Fable of the Mirror-Breaker (NEO) 141
    0-1 Mountain (SNC) 268
    0-1 Takenuma, Abandoned Mine (NEO) 278
    0-4 Blightstep Pathway (KHM) 252
    0-4 Brightclimb Pathway (ZNR) 259
    0-4 Needleverge Pathway (ZNR) 263
    0-4 Blood Crypt (RNA) 245
    0-4 Godless Shrine (RNA) 248
    0-4 Sacred Foundry (GRN) 254
    0-4 Haunted Ridge (MID) 263
    0-4 Shattered Sanctum (VOW) 264
    0-4 Sundown Pass (VOW) 266
    0-4 Concealed Courtyard (KLR) 282
    0-4 Inspiring Vantage (KLR) 246   
        """.trimIndent()

        val actions = listOf(
            tryCastGreasefangIfParhelionInYard,
            tryReanimateGreaseWithSorin,
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
            tryCastDeadlyIfZombieInField,
            tryCastKiki,
            tryCastVampire,
            lootWithBloodToken,
            tryCastGreasefang,
            flashbackFaithless,
        )

        val triggers = listOf(
            onStitcherETB,
            onStitcherDestroyed,
            onButlerETB,
            onButlerDestroyed,
            onGoblinETB,
            onFaithlessCast,
            onRevivalCast,
            onCSACast,
            onDeadlyDisputeCast,
            onKikiETB,
            onSorinETB,
        )

        val winCon = MultiCondition.and(greasefangInField, parhelionInYard)
    }
}