fun main(args: Array<String>) {
    println("Test")
    //val searchQuery = readln()
    val deckList = """
        Greasefang, Okiba Boss 
        Agadeem's Awakening
        Parhelion II
        Revival // Revenge 
        Can't Stay Away 
        Stitcher's Supplier 
        Undead Butler 
        Faithless Looting
        Seasoned Pyromancer 
        Goblin Engineer 
        Wishclaw Talisman 
        Bone Shards
        Skysovereign, Consul Flagship 
        Hive of the Eye Tyrant 
        Haunted Ridge 
        Savai Triome 
        Blightstep Pathway 
        Godless Shrine 
        Brightclimb Pathway 
        Blood Crypt 
        Sacred Foundry 
    """.trimIndent()

    var cardList = forohfor.scryfall.api.MTGCardQuery.toCardList(deckList.split("\n","\r"), false)
    //val queryResult = forohfor.scryfall.api.MTGCardQuery.search(searchQuery)
    cardList?.map {

        val card = MTGCard.of(it)
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