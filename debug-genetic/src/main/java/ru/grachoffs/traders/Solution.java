package ru.grachoffs.traders;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static ru.grachoffs.traders.State.BOUGHT;

@Data
@ToString
@Slf4j
public class Solution implements Comparable<Solution>{
    @Getter(value = AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Long res;
    private State initialState;
    private List<Integer> points;
    private List<Long> prices;
    @Getter(value = AccessLevel.NONE)
    private static Random random = new Random();

    private Long acquireResult() {
        Long tmpRes = 0L;
        Long buyPrice = 0L;
        Integer counter = 0, pointer = 0;
        State state = initialState;

        if (BOUGHT.equals(initialState)) {
            buyPrice = prices.get(pointer);
        }

        while ((counter < points.size())) {
            pointer += points.get(counter);
            if (pointer>=prices.size()) break;
            if (BOUGHT.equals(state)) {
                tmpRes += prices.get(pointer) - buyPrice;
//                log.info("tmpRes: {}", tmpRes);
                buyPrice = 0L;
                state = State.WAITING;
            } else {
                buyPrice = prices.get(pointer);
                state = BOUGHT;
            }

            counter++;
        }
        if (BOUGHT.equals(state) && buyPrice > 0) {
            tmpRes += prices.get(prices.size()-1) - buyPrice;
//            log.info("tmpRes: {}", tmpRes);
        }
        return tmpRes;
    }

    public Long getResult() {
        if (res == null) res = acquireResult();
        return res;
    }

    public static Solution getRandomSolution(List<Long> prices) {
        Solution solution = new Solution();
        solution.setPrices(prices);
        Integer limit = prices.size()/4, bound = 30;
        List<Integer> points = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            points.add(random.nextInt(bound)+1);
        }
        solution.setPoints(points);
        solution.setInitialState(random.nextBoolean() ? State.BOUGHT : State.WAITING);
        return solution;
    }

    public static List<Solution> getRandomSolutions(List<Long> prices, Integer size) {
        List<Solution> solutions = new ArrayList<>(size);
        for (int i=0; i < size; i++) {
            solutions.add(getRandomSolution(prices));
        }
        return solutions;
    }

    public static List<Solution> getFixedSolutions(List<Long> prices, Integer size, Integer fixedValue) {
        List<Solution> solutions = new ArrayList<>(size);
        for (int i=0; i < size; i++) {
            solutions.add(getFixedSolution(prices, fixedValue));
        }
        return solutions;
    }

    private static Solution getFixedSolution(List<Long> prices, Integer fixedValue) {
        Solution solution = new Solution();
        solution.setPrices(prices);
        solution.setInitialState(random.nextBoolean() ? State.BOUGHT : State.WAITING);
        Integer limit = prices.size()/4;
        List<Integer> points = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            points.add(fixedValue);
        }
        solution.setPoints(points);

        return solution;
    }

    @Override
    public int compareTo(Solution o) {
        return Long.compare(getResult(), o.getResult());
    }
}
