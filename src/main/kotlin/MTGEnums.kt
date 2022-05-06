enum class MTGMana (val symbol:String) {
    WHITE("W"),
    BLUE("U"),
    BLACK("B"),
    RED("R"),
    GREEN("G"),
    COLORLESS("C"),
    ;
    companion object {
        fun fromSymbol(symbol:String):MTGMana? {
            return values().firstOrNull { it.symbol == symbol }
        }
    }
}

enum class MTGLandTag(val tappedOnETB: TappedOnETB) {
    BASIC(TappedOnETB.ALWAYS_UNTAPPED),
    //TRIOME(TappedOnETB.ALWAYS_TAPPED),
    SNARL(TappedOnETB.CONDITIONALLY_UNTAPPED),
    SLOW(TappedOnETB.CONDITIONALLY_UNTAPPED),
    FAST(TappedOnETB.CONDITIONALLY_UNTAPPED),
    CHECK(TappedOnETB.CONDITIONALLY_UNTAPPED),
    SHOCK(TappedOnETB.ALWAYS_UNTAPPED),
    BOLT(TappedOnETB.ALWAYS_UNTAPPED),
    CREATURE_AFR(TappedOnETB.CONDITIONALLY_UNTAPPED),
    GENERIC_TAPLAND(TappedOnETB.ALWAYS_TAPPED),
    GENERIC_UNTAPPED(TappedOnETB.ALWAYS_UNTAPPED),
    UNKNOWN(TappedOnETB.CONDITIONALLY_UNTAPPED), //Default assumes untapped
    ;

    companion object {
        fun of(card:MTGCard):MTGLandTag {
            return if(card.types.contains("Basic"))
                BASIC
//            else if(card.oracle.contains("Cycling {3}") && card.oracle.contains("${card.name} enters the battlefield tapped."))
//                TRIOME
            else if(card.name.contains("Pathway"))
                GENERIC_UNTAPPED
            else if(card.oracle.contains("${card.name} enters the battlefield tapped unless you control two or more other lands."))
                SLOW
            else if(card.oracle.contains("If you control two or more other lands, ${card.name}"))
                CREATURE_AFR
            else if(card.oracle.contains("As ${card.name} enters the battlefield, you may reveal a"))
                SNARL
            else if(card.oracle.contains("${card.name} enters the battlefield tapped unless you control two or fewer other lands."))
                FAST
            else if(card.oracle.contains("you may pay 3 life. If you don't, it enters the battlefield tapped."))
                BOLT
            else if(card.oracle.contains("you may pay 2 life. If you don't, it enters the battlefield tapped."))
                SHOCK
            else if(card.oracle.contains("${card.name} enters the battlefield tapped unless you control a"))
                CHECK
            else if(card.oracle.contains(""))
                GENERIC_TAPLAND
            else if(card.oracle.contains("${card.name} enters the battlefield tapped."))
                GENERIC_TAPLAND
            else
                UNKNOWN
        }
    }
}

enum class TappedOnETB {
    ALWAYS_TAPPED,
    ALWAYS_UNTAPPED,
    CONDITIONALLY_UNTAPPED,
}

enum class MultiConditionMode {
    AND,
    OR,
}

enum class MTGZone(val of: (MTGBoardState) -> List<MTGCard>) {
    BATTLEFIELD(MTGBoardState::field),
    HAND(MTGBoardState::hand),
    GRAVEYARD(MTGBoardState::yard),
    LIBRARY(MTGBoardState::library),
    EXILE(MTGBoardState::exile),
    LANDS( { board -> board.lands.map { it.asMTGCard() }}),
    UNTAPPED_PERMANENTS( {it.field.filter { !it.tapped }}),
    UNTAPPED_LANDS( { board -> board.lands.filter{ !it.tapped }.map { it.asMTGCard() }}),
}

enum class MTGGameQueryType {
    TURN_NUMBER,
    CAN_PAY_CMC,
    CAN_PAY_MANA_VALUE,
}

enum class ConditionOperator(val operator: String) {
    EXACTLY("="),
    NOT("!="),
    //GREATER_THAN(">"),
    //LESS_THAN("<"),
    AT_LEAST(">="),
    AT_MOST("<="),
}

enum class MTGCardField(val of: (MTGCard) -> String?) {
    NAME( { it.name } ),
    TYPE( { it.types.joinToString(" ") } ),
}

enum class MTGTrigger {
    ETB,
    CAST,
}