val deckRatCar = """
    4 Haunted Ridge (MID) 263
    4 Greasefang, Okiba Boss (NEO) 220
    4 Goblin Engineer (MH1) 128
    2 Can't Stay Away (MID) 213
    4 Faithless Looting (STA) 38
    4 Parhelion II (WAR) 24
    2 Wishclaw Talisman (ELD) 110
    2 Needleverge Pathway (ZNR) 263
    4 Blightstep Pathway (KHM) 252
    4 Godless Shrine (RNA) 248
    4 Brightclimb Pathway (ZNR) 259
    4 Blood Crypt (RNA) 245
    2 Sacred Foundry (GRN) 254
    4 Bone Shards (MH2) 76
    4 Stitcher's Supplier (M19) 121
    4 Undead Butler (VOW) 133
    2 Revival // Revenge (RNA) 228
    1 Skysovereign, Consul Flagship (KLR) 272
    1 Concealed Courtyard (KLR) 282
""".trimIndent()

fun main(args: Array<String>) {
    println("Test")
    //val searchQuery = readln()
    val deckList = deckRatCar

    val cardList = MTGCard.fromArenaList(deckList)

            //var cardList = forohfor.scryfall.api.MTGCardQuery.toCardList(deckList.split("\n","\r"), false)
    //val queryResult = forohfor.scryfall.api.MTGCardQuery.search(searchQuery)
    cardList.map {card ->

        //val card = MTGCard.of(it)
        println(card.name + " " + card.types)
        println(card.cmc.toString() + " " + card.manaCost)

        card.backside?.let { back ->
            println("  " + back.name + " " + back.types)
            println("  " + back.cmc + " " + back.manaCost)
        }


//        println(it.name)
//        it.cardFaces?.forEach { face ->
//            println("  ${face.name}")
//            println("  ${face.oracleText}")
//            println("  ${face.jsonData?.get("colors")}")
//            println("  ${face.jsonData?.get("color_indicator")}")
//        }
//        println(it.cmc)
//        println(it.manaCost)
//        println(it.jsonData?.get("color_indicator"))
//        println(it.jsonData?.get("colors"))
//        println(it.colorIdentity)
//        println(it.jsonData?.get("produced_mana"))
        println("----")
    }
}