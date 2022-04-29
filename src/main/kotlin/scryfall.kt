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
