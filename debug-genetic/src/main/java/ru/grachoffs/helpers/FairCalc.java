package ru.grachoffs.helpers;

import lombok.extern.slf4j.Slf4j;
import ru.grachoffs.traders.Solution;
import ru.grachoffs.traders.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Slf4j
public class FairCalc {
    private Long value = 0L;
    private State state;
    private Long boughtPrice = 0L;
    private List<Long> values;
    private int counter = 0;
    private State initialState;
    private List<Integer> points = new ArrayList<>();

    public FairCalc(List<Long> values) {
        this.values = values;
        if (values.size()<2) {
            initialState = State.WAITING;
            return;
        }
        if (values.get(1)>values.get(0)) {
            boughtPrice = values.get(0);
            initialState = State.BOUGHT;
        } else {
            initialState = State.WAITING;
        }
        counter = 1;
    }

    public Long calculate() {
        state = initialState;
        Integer point = 0;

        log.info("Initial state: {}", state);
        Long current = 0L, next = 0L;
        while (counter < values.size()-1) {
            point++;
            current = values.get(counter);
            next = values.get(counter+1);
//            log.info("Current value: {}, earned: {}", current, value);
            if (Objects.equals(current, next)) {
                counter++;
                continue;
            }
            if (State.BOUGHT.equals(state)) {
                if (next > current) {
                    counter++;
                    continue;
                }
                //time to sell
                value += current - boughtPrice;
//                log.info("selling for {}, value: {}", current, value);
                points.add(point);
                point = 0;
                boughtPrice = 0L;
                state = State.WAITING;
//                log.info("Time to sell: {}", state);
            } else {
                if (next < current) {
                    counter++;
                    continue;
                }
                //time to buy
                boughtPrice = current;
//                log.info("buying for {}", current);
                points.add(point);
                point = 0;
                state = State.BOUGHT;
//                log.info("Time to buy: {}", state);
            }

            counter++;
        }
        if (State.BOUGHT.equals(state)) {
            value += (current > next ? current : next) - boughtPrice;
//            log.info("selling for {}, value: {}", (current > next ? current : next), value);
        }
        return value;
    }

    public Solution getSolution() {
        Solution solution = new Solution();
        solution.setInitialState(initialState);
        solution.setPoints(points);
        solution.setPrices(values);
        return solution;
    }
}
