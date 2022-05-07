import forohfor.scryfall.api.MTGCardQuery
import java.util.*

fun MTGCard.Companion.of(scryfallCard:forohfor.scryfall.api.Card):MTGCard = if(scryfallCard.cardFaces == null) {
    MTGCard(
        name = scryfallCard.name,
        types = scryfallCard.typeLine?.split(' ','—')?.filter { it.isNotEmpty() } ?: emptyList(),
        oracle = scryfallCard.oracleText,
        manaCost = scryfallCard.manaCost ?: null,
        backside = null,
        json = scryfallCard.jsonData,
    )
} else {
    MTGCard.of(scryfallCard.cardFaces[0]).copy(
        backside = MTGCard.of(scryfallCard.cardFaces[1]),
        json = scryfallCard.jsonData,
    )
}

fun MTGCard.Companion.of(scryfallFace:forohfor.scryfall.api.CardFace):MTGCard = MTGCard(
    name = scryfallFace.name,
    types = scryfallFace.typeLine?.split(' ','—')?.filter { it.isNotEmpty() } ?: emptyList(),
    oracle = scryfallFace.oracleText,
    manaCost = scryfallFace.manaCost ?: null,
    backside = null,
    json = scryfallFace.jsonData,
)

fun MTGCard.Companion.scryfallSearch(query:String):List<MTGCard> = MTGCardQuery.search(query)?.map { MTGCard.of(it) } ?: emptyList()

//Receive a list of card names, one per line, for example:
// Island
// Mountain
// Expressive Iteration
//Split them all, trim them, and call forohfor.scryfall.api.MTGCardQuery.toCardList with the trimmed list
//For each result, call MTGCard.Companion.of(scryfallCard) and return thant
fun MTGCard.Companion.fromNameList(cardNames:String):List<MTGCard> {
    val cardList = cardNames.split('\n','\r').map { it.trim() }.filter { it.isNotEmpty() }
    val cards = MTGCardQuery.toCardList(cardList, false)
    return cards.map { MTGCard.of(it) }
}

//Receive a list of Arena-style card names and their count, one for each line, for example:
// 10 Island (NEO) 123
// 10 Mountain (SNC) 234
//Split them into individual lines
//For each line, use a regular expression to extract the count, the card name, the set name between parentheses, and the card number
//Use the card names to call MTGCard.of(cardNames)
//For each returned card, copy it the amount of times specified in the count
//Return the list of cards
fun MTGCard.Companion.fromArenaList(arenaDeck:String):List<MTGCard> {
    val cardList = arenaDeck.split('\n','\r').map { it.trim() }.filter { it.isNotEmpty() }
    val cardNamesToAmount = cardList.map {
        val regex = Regex("(\\d+) ([^\\(]+) \\(([^\\)]+)\\) (\\d+)")
        val match = regex.find(it)
        if(match != null) {
            val count = match.groupValues[1].toInt()
            val name = match.groupValues[2]
            //val set = match.groupValues[3]
            //val number = match.groupValues[4].toInt()
            return@map name to count
        } else {
            return@map null
        }
    }.filterNotNull()
    val cards = MTGCard.fromNameList(cardNamesToAmount.joinToString("\n") { it.first })
    val deck = cards.flatMap { card ->
        //Because of double-faced cards (like Revival // Revenge) we can't use an exact name match
        //However, some double faced cards only have the front face as the name, so we can't use // for all of them
        //So instead we do a partial match with the beginning of the name
        val count = cardNamesToAmount.firstOrNull { it.first.startsWith(card.name) }?.second ?: 0
        (1..count).map { card.copy(
            uuid = UUID.randomUUID(),
        ) }
    }
    assert(deck.size == cardNamesToAmount.sumOf { it.second })
    return deck
}

fun MTGCard.Companion.fromRangeArenaList(arenaDeck:String):List<Triple<MTGCard,Int,Int>> {
    val regex = Regex("""(?<min>\d+)(\-(?<max>\d+))? (?<name>[^\(]+) \((?<set>[^\)]+\)) (?<number>\d+)""")
    val cardList = arenaDeck.split('\n','\r').map { it.trim() }.filter { it.isNotEmpty() }
    val cardNamesToAmount = cardList.map {
        val match = regex.find(it)
        if(match != null) {
            val min = match.groups["min"]?.value?.toInt() ?: 4
            val max = match.groups["max"]?.value?.toInt() ?: min
            val name = match.groups["name"]?.value ?: ""
            //val set = match.groupValues[3]
            //val number = match.groupValues[4].toInt()
            return@map Triple(name,min,max)
        } else {
            return@map null
        }
    }.filterNotNull()
    val cards = MTGCard.fromNameList(cardNamesToAmount.joinToString("\n") { it.first })
    val deck = cards.map { card ->
        //Because of double-faced cards (like Revival // Revenge) we can't use an exact name match
        //However, some double faced cards only have the front face as the name, so we can't use // for all of them
        //So instead we do a partial match with the beginning of the name
        val amounts = cardNamesToAmount.first { it.first.startsWith(card.name) }
        return@map Triple(card,amounts.second,amounts.third)
    }
    return deck
}

