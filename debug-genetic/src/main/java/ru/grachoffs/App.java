package ru.grachoffs;

import lombok.extern.slf4j.Slf4j;
import ru.grachoffs.helpers.FairCalc;
import ru.grachoffs.helpers.SolutionHelper;
import ru.grachoffs.traders.Solution;

import java.util.*;

import static ru.grachoffs.helpers.RandomHelper.fillRandomValues;
import static ru.grachoffs.helpers.RandomHelper.readRandomValues;
import static ru.grachoffs.helpers.RandomHelper.writeRandomValues;
import static ru.grachoffs.helpers.SolutionHelper.getAverageResult;
import static ru.grachoffs.helpers.SolutionHelper.getBestResult;

@Slf4j
public class App 
{
    private static final String FILE_NAME = "d:\\Heap\\tmp\\values.bin";
    private static final Long INITIAL_VALUE = 1000L;
    private static final Long MAX_ITERATIONS = 1000000L;
    private static int VALUES_COUNT = 1000;
    private static int PROBABILITY_OF_TREND_CHANGINMG = 5;
    private static int VALUE_RANGE = 100;
    private static int INITIAL_POPULATION_SIZE = 100;
    private static final int HOW_MUCH_PICK = 10;
    private static final int HOW_MUCH_BREED = 20;
    private static final int HOW_MUCH_MUTATE = 100;
    private static final int MUTATION_PROBABILITY_PERCENTS = 7;
    private static final int TARGET_PERCENTAGE = 90;

    public static void main( String[] args )
    {
//        writeFileWithRandomValues();
//        List<Long> values = readRandomValues(FILE_NAME);

        List<Long> values = fillRandomValues(VALUES_COUNT, VALUE_RANGE, PROBABILITY_OF_TREND_CHANGINMG, INITIAL_VALUE);
        log.info("{}",values);
        FairCalc fairCalc = new FairCalc(values);
        long result = fairCalc.calculate();
        Solution solution = fairCalc.getSolution();
//        log.info("{}", solution);
        log.info("Precision result: {}", result);
        log.info("Precision result check: {}", solution.getResult());
        List<Solution> solutions1 = Solution.getRandomSolutions(values, 0);
        List<Solution> solutions2 = Solution.getFixedSolutions(values, INITIAL_POPULATION_SIZE, 1);
        List<Solution> solutions = new ArrayList<>(INITIAL_POPULATION_SIZE*2);
        solutions.addAll(solutions2);
        solutions.addAll(solutions1);
        EvolutionLoop evolutionLoop = new EvolutionLoop(
                MAX_ITERATIONS,
                solutions,
                collection -> SolutionHelper.pickBest((List<Solution>) collection, HOW_MUCH_PICK),
                collection -> SolutionHelper.breed((List<Solution>) collection, HOW_MUCH_BREED),
                collection -> SolutionHelper.mutate((List<Solution>) collection, HOW_MUCH_MUTATE, MUTATION_PROBABILITY_PERCENTS, VALUE_RANGE),
                collection -> SolutionHelper.checkForCompletion((List<Solution>) collection, result, TARGET_PERCENTAGE),
                collection -> SolutionHelper.eliminate((List<Solution>) collection)
                );
        log.info("Average: {}", getAverageResult(solutions));
        Long best = getBestResult(solutions);
        log.info("Best: {}", best);
        Boolean completed = Boolean.FALSE;
        Long milliseconds = System.currentTimeMillis();
        while (!completed) {
            completed = evolutionLoop.performStep();
            if (evolutionLoop.getEvolutionState().equals(EvolutionState.PICKED)) {
                Long tmpBest = getBestResult(evolutionLoop.getSolutions());
                if (tmpBest > best) {
                    best = tmpBest;
                    Long iteration = evolutionLoop.getIteration();
                    Long speed = 1000 * iteration / (System.currentTimeMillis() - milliseconds);
                    log.info("Best: {}, percentage: {}, iteration: {}, speed: {} it/sec", best, SolutionHelper.atomicLong.get(), iteration, speed) ;
                }
            }
        }
        log.info("Completed. Best: {}, percentage: {}, iteration: {}", getBestResult(evolutionLoop.getSolutions()), SolutionHelper.atomicLong.get(), evolutionLoop.getIteration());

    }

    private static void writeFileWithRandomValues() {
        List<Long> values = fillRandomValues(VALUES_COUNT, VALUE_RANGE, PROBABILITY_OF_TREND_CHANGINMG, INITIAL_VALUE);
        writeRandomValues(values, FILE_NAME);
    }

}
