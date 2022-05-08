import io.jenetics.*
import io.jenetics.engine.Engine
import io.jenetics.engine.EvolutionResult
import io.jenetics.engine.Limits.bySteadyFitness
import io.jenetics.util.Factory
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

fun main() {
    testJenetics()
    //singleRun()
}

private fun singleRun() {
    val deck = MTGCard.fromArenaList(RatCarSample.deckExplorer)
    println("Sample run")
    val gameEnd = MTGBoardState(deck = deck, triggers = RatCarSample.triggers).startGame().playUntilWinCon(RatCarSample.winCon, RatCarSample.actions)
    gameEnd.gameLog.forEach { println(it.info) }
}

private fun testJenetics() {
    val cardList = MTGCard.fromRangeArenaList(RatCarSample.deckExplorer)
    val geneCount = cardList.size
    val minGene = cardList.minByOrNull { it.second }?.second ?: 0
    //Max is not inclusive, so we add 1
    val maxGene = 1 + (cardList.maxByOrNull { it.third }?.third ?: 4)

    fun genotypeToDeck(genome: Genotype<IntegerGene>): List<MTGCard> {
        //Do we shave the deck to 60/bump it from less than 60?
        return genome.chromosome().flatMapIndexed { index, gene ->
            val cardInfo = cardList[index]
            val card = cardInfo.first
            val min = cardInfo.second
            val max = cardInfo.third
            val howMany = gene.allele().coerceIn(min, max)
            (1..howMany).map { card.copy(
                uuid = UUID.randomUUID(),
            ) }
        }
    }

    val runs = 1000
    val generations = 500L
    val steadyGenerations = 500
    fun fitness(genotype: Genotype<IntegerGene>): Double {
        val deck = genotypeToDeck(genotype)
        if(deck.size != 60) return 100.0

        val turnsToEnd = MTGGameSim(deck, RatCarSample.actions, RatCarSample.triggers).runSims(RatCarSample.winCon, runs)
        //More points the lower the turns to end, so we divide 100 by each turn, and sum them up
        return turnsToEnd.average()
    }

    val integerGenotypeFactory: Factory<Genotype<IntegerGene>> = Genotype.of(IntegerChromosome.of(minGene, maxGene, geneCount))
    val engine = Engine
        .builder(::fitness, integerGenotypeFactory)
        //.populationSize(20)
        //.survivorsSelector(EliteSelector(1))
        .minimizing()
        //.interceptor(EvolutionResult.toUniquePopulation())
        .build()

    val start = LocalDateTime.now()
    //println("Running $generations generations of $runs simulations")
    println("Running until $steadyGenerations steady generations of $runs simulations")
    println("START: $start")
    val result = engine
        .stream()
        .limit(bySteadyFitness(steadyGenerations))
        //.limit(generations)
        .peek { g ->
            //TODO: Print population, such as:
            //[min,max] current card for each card, taking the min and max of the population for each card
            println("Generation ${g.generation()}. Best turn avg: ${g.bestPhenotype().fitness()}")
            //printDeck(genotypeToDeck(g.bestPhenotype().genotype()))
            g.bestPhenotype().genotype().chromosome().forEachIndexed { index, integerGene ->
                val cardInfo = cardList[index]
                //val min = g.population().minOf { it.genotype().chromosome()[index].allele() }.coerceIn(cardInfo.second, cardInfo.third)
                //val max = g.population().maxOf { it.genotype().chromosome()[index].allele() }.coerceIn(cardInfo.second, cardInfo.third)
                val avg = g.population().map { it.genotype().chromosome()[index].allele() }.average().coerceIn(cardInfo.second.toDouble(), cardInfo.third.toDouble())
                val curr = integerGene.allele().coerceIn(cardInfo.second, cardInfo.third)
                //println("[$min-$max] ${"|".repeat(curr).padEnd(max, '_')} ${cardInfo.first}")
                println("[${"%.2f".format(avg)}] ${"|".repeat(curr).padEnd(cardInfo.third, '_')} ${cardInfo.first}")
            }
        }
        .collect(EvolutionResult.toBestGenotype());
    val end = LocalDateTime.now()
    println("END: $end")

    val durationInMillis = Duration.between(start,end).toMillis()
    //val averageDurationInMillis = durationInMillis.toDouble() / generations.toDouble()
    println("TOTAL: $durationInMillis ms")
    //println("AVG: $averageDurationInMillis ms per generation")

    val finalDeck = genotypeToDeck(result)
    printDeck(finalDeck)

    //val finalSimTurns = MTGGameSim(finalDeck, RatCarSample.actions, RatCarSample.triggers).runSims(RatCarSample.winCon, runs)
    //println("Average turns to end: ${finalSimTurns.average()}")

    //println("Sample run")
    //val gameEnd = MTGBoardState(deck = finalDeck, triggers = RatCarSample.triggers).startGame().playUntilWinCon(RatCarSample.winCon, RatCarSample.actions)
    //gameEnd.gameLog.forEach { println(it.info) }
}

private fun printDeck(deck: List<MTGCard>) {
    deck.groupBy { it.toString() }.map { it.key to it.value.size }.forEach { println("${it.second} ${it.first}") }
}