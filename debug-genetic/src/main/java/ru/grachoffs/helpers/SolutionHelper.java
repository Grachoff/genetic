package ru.grachoffs.helpers;

import lombok.extern.slf4j.Slf4j;
import ru.grachoffs.traders.Solution;
import ru.grachoffs.traders.State;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
public class SolutionHelper {
    private static Random random = new Random();
    public static AtomicLong atomicLong = new AtomicLong(0L);
    public static Long getBestResult(List<Solution> solutions) {
        return solutions.stream().mapToLong(solution -> solution.getResult()).max().getAsLong();
    }

    public static Long getAverageResult(List<Solution> solutions) {
        return (long) solutions.stream().mapToLong(solution -> solution.getResult()).average().getAsDouble();
    }

    public static List<Solution> pickBest(List<Solution> solutions, int howMuch) {
        return solutions.stream().sorted(Comparator.reverseOrder()).limit(howMuch).collect(Collectors.toList());
    }

    public static List<Solution> mutate(List<Solution> solutions, int howMuch, int probabilityPercentage, int newValueRange) {
//        probabilityPercentage = random.nextInt(37);
        List<Solution> mutated = new ArrayList<>(howMuch);
        for (int i=0; i < howMuch; i++) {
            addMutatedSolution(solutions, probabilityPercentage, newValueRange, mutated);
        }
        return mutated;
    }

    private static void addMutatedSolution(List<Solution> solutions, int probabilityPercentage, int newValueRange, List<Solution> mutated) {
        Solution tmpSolution, randomOldSolution;
        tmpSolution = new Solution();
        randomOldSolution = getRandomSolution(solutions);
        tmpSolution.setPrices(randomOldSolution.getPrices());
        tmpSolution.setInitialState(getMutatedInitialState(probabilityPercentage, randomOldSolution));
        tmpSolution.setPoints(getMutatedPoints(randomOldSolution.getPoints(), probabilityPercentage, newValueRange));
        if (!randomOldSolution.getInitialState().equals(tmpSolution.getInitialState())) {
            tmpSolution.getPoints().add(0, random.nextInt(newValueRange));
        }
        mutated.add(tmpSolution);
    }

    private static List<Integer> getMutatedPoints(List<Integer> points, int probabilityPercentage, int newValueRange) {
        List<Integer> values = new ArrayList<>(points.size()+10);
        for (int i = 0; i< points.size(); i++) {
            int tmpValue = points.get(i);
            if (checkMutationProbability(probabilityPercentage)) {
                tmpValue = random.nextInt(newValueRange);
            } else {
                if (checkMutationProbability(probabilityPercentage)) {
                    tmpValue++;
                }
                if (checkMutationProbability(probabilityPercentage)) {
                    tmpValue--;
                }
                if (tmpValue < 1) tmpValue++;

            }
            if (checkMutationProbability(probabilityPercentage)) {
                values.add(random.nextInt(newValueRange));
            }
            if (checkMutationProbability(probabilityPercentage)) {
                continue;
            }
            values.add(tmpValue);
        }
        return values;
    }

    private static State getMutatedInitialState(int probabilityPercentage, Solution randomOldSolution) {
        return checkMutationProbability(probabilityPercentage) ?
                (State.BOUGHT.equals(randomOldSolution.getInitialState()) ? State.WAITING : State.BOUGHT)
                : randomOldSolution.getInitialState();
    }

    private static boolean checkMutationProbability(int probabilityPercentage) {
        return random.nextInt(100) <= probabilityPercentage;
    }

    private static Solution getRandomSolution(List<Solution> solutions) {
        return solutions.get(random.nextInt(solutions.size()));
    }

    public static boolean checkForCompletion(List<Solution> solutions, Long perfectResult, int targetPercentage) {
        Long percentage = getBestResult(solutions) * 100 / perfectResult;
        atomicLong.set(percentage);
//        log.info("Percentage: {}", percentage);
        return percentage >= targetPercentage;
    }

    public static void eliminate(List<Solution> collection) {
        Collections.sort(collection, Comparator.reverseOrder());
        int i = 1;
        Long res = collection.get(0).getResult();
        while (i<collection.size()) {
            if (collection.get(i).getResult().equals(res)) {
                collection.remove(i);
                continue;
            }
            res = collection.get(i).getResult();
            i++;
        }
    }

    public static List<Solution> breed(List<Solution> collection, int howMuchBreed) {
        List<Solution> solutions = new ArrayList<>(howMuchBreed);
        for (int i = 0; i < howMuchBreed; i++) {
            Solution rnd1 = collection.get(random.nextInt(collection.size()));
            Solution rnd2 = collection.get(random.nextInt(collection.size()));
            Solution result = new Solution();
            result.setPrices(rnd1.getPrices());
            result.setInitialState(random.nextBoolean() ? rnd1.getInitialState() : rnd2.getInitialState());
            int sizeOfNewCollection = Math.min(rnd1.getPoints().size(), rnd2.getPoints().size());
            List<Integer> newPoints = new ArrayList<>(sizeOfNewCollection);
            for (int j=0; j < sizeOfNewCollection; j++) {
                newPoints.add((rnd1.getPoints().get(j) + rnd2.getPoints().get(j))/2);
            }
            result.setPoints(newPoints);
            solutions.add(result);
        }
        return solutions;
    }
}
