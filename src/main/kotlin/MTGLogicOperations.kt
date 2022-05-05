//Things I want to represent:
//If lands can produce {1}{W}{B}
//If there are 3 or more untapped lands
//If I have a Greasefang in hand
//If there's a vehicle in the graveyard
//CanPayFor: {1}{W}{B}
//in:Hand name:foo
//tryCast: name:foo //Gets the mana cost of foo, checks if we have enough to pay for it, if we do, tap the required lands and cast it
//More advanced:
// after casting faithless looting, draw 2 and then
// if we have a reanimate spell in hand, discard greasefang
// if not, discard anything but greasefang

//CardQuery : IConditional, of(string), field, value (for now, only field contains value) (for example, "name:greasefag" or "type:vehicle") (default is field=name)
//BoardQuery : IConditional, zone, cardquery, operator (atleast, exactly, atmost), amount, default is atleast 1

// in:graveyard | card: name:Greasefang | atLeast: 1
// in:hand | card:greasefang | atLeast: 1 || in:graveyard | card: parhelion || canPayFor: {1}{W}{B}
//   alternatively, use the tryCast action and just check for parhelion in yard
// in:hand | card: CSA | atLeast: 1 || in:graveyard | card: greasefang || in:graveyard | card: parhelion || canPayFor: {W}{B}
//   alternatively, use tryCast: CSA and check for parhelion and greasefang in yard
// in:hand | card: Revival | atLeast: 1 || in:graveyard | card: greasefang || in:graveyard | card: parhelion || canPayFor: {W/B}{W/B}
//  alternatively, use tryCast: Revival and check for parhelion and greasefang in yard
// in:hand | card: wishclaw || in:hand | card: greasefang | exactly: 0 ||

// - Check for Greasefang in hand and Parhelion in yard
// - Check for greasefang in play and parhelion in yard
// - Check for greasefang in yard -> trycast CSA/RR
// - Check for no greasefang in hand -> trycast Wishclaw (for Greasefang)
// - Check for no parhelion in yard -> trycast Wishclaw (for Goblin Engineer)
// - Check for wishclaw in field, no greasefang in hand, 1 untapped land -> tutor greasefang, exile wishclaw, tap 1 land
// - Check for wishclaw in field, no parhelion in yard, 1 untapped land -> tutor goblin engineer, exile wishclaw, tap 1 land
// - Check for goblin in hand, no parhelion in yard, trycast goblin
// - Check for faithless in yard, canPayFor {2}{R} -> tap 3 lands, flashback faithless looting
// - Check for turn 10, tally failure

data class CardQuery(val field:MTGCardField, val value: String) : ICondition<MTGCard> {
    override fun matches(card: MTGCard): Boolean {
        return field.of(card)?.contains(value) ?: false
    }

    companion object {
        fun parse(query:String) : CardQuery {
            return parseOrNull(query) ?: throw IllegalArgumentException("Could not parse query: $query")
        }
        
        fun parseOrNull(query: String): CardQuery? {
            //Valid queries:
            // name:greasefang
            // type:vehicle
            // greasefang
            val regex = Regex("""((?<field>\w+):)?(?<value>.+)""")
            val match = regex.matchEntire(query) ?: return null
            val value = match.groups["value"]?.value ?: return null
            val field = match.groups["field"]?.value?.let { s -> MTGCardField.values().find{e -> s.equals(e.name, true) } } ?: MTGCardField.NAME
            return CardQuery(field, value)
        }
    }
}

data class BoardQuery(
    val zone: MTGZone, 
    val query: CardQuery, 
    val operator: ConditionOperator = ConditionOperator.AT_LEAST, 
    val amount: Int = 1
) : ICondition<MTGBoardState> {
    override fun matches(board: MTGBoardState): Boolean {
        val cards = zone.of(board).count { query.matches(it) }
        return when (operator) {
            ConditionOperator.EXACTLY -> cards == amount
            ConditionOperator.NOT -> cards != amount
            ConditionOperator.AT_LEAST -> cards >= amount
            ConditionOperator.AT_MOST -> cards <= amount
        }
    }
}

//Special cases that can't be directly expressed by card queries
// - turn number
// - whether we can pay for a mana value (e.g. canPayFor {2}{R})
data class GameQuery(val queryType: MTGGameQueryType, val target: String) : ICondition<MTGBoardState> {
    override fun matches(board: MTGBoardState): Boolean = when (queryType) {
        MTGGameQueryType.TURN_NUMBER -> board.turn == target.toInt()
        MTGGameQueryType.CAN_PAY_CMC -> board.canPayFor(target.toInt())
        MTGGameQueryType.CAN_PAY_MANA_VALUE -> board.canPayFor(target)
    }

    companion object {
        fun isTurn(turn: Int) : GameQuery {
            return GameQuery(MTGGameQueryType.TURN_NUMBER, turn.toString())
        }
        fun canPayFor(cmc: Int) : GameQuery {
            return GameQuery(MTGGameQueryType.CAN_PAY_CMC, cmc.toString())
        }
        fun canPayFor(manaValue: String) : GameQuery {
            return GameQuery(MTGGameQueryType.CAN_PAY_MANA_VALUE, manaValue)
        }
    }
}

interface ICondition<T> {
    fun matches(target: T): Boolean
}

data class MultiCondition<T>(
    val conditions: List<ICondition<T>>, 
    val mode: MultiConditionMode = MultiConditionMode.AND
) : ICondition<T> {
    override fun matches(target: T): Boolean = when(mode) {
        MultiConditionMode.AND -> conditions.all { it.matches(target) }
        MultiConditionMode.OR -> conditions.any { it.matches(target) }
    }

    companion object {
        fun <T> and(vararg conditions:ICondition<T>) = MultiCondition(conditions.asList(), MultiConditionMode.AND)
        fun <T> or(vararg conditions:ICondition<T>) = MultiCondition(conditions.asList(), MultiConditionMode.OR)
    }
}