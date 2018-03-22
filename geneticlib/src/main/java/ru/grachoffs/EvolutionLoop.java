package ru.grachoffs;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


@Slf4j
public class EvolutionLoop<T> {
    private Long maxIterations = 1000L;
    private Long iteration;
    EvolutionState evolutionState = EvolutionState.NOT_RUNNING;

    List<T> solutions;
    List<T> picked;
    List<T> breeded;
    List<T> mutated;
    Function<List<T>, List<T>> picker;
    Function<List<T>, List<T>> breeder;
    Function<List<T>, List<T>> mutator;
    Function<List<T>, Boolean> checker;
    Consumer<List<T>> eliminator;

    public EvolutionLoop(Long maxIterations,
                         List<T> solutions,
                         Function<List<T>, List<T>> picker,
                         Function<List<T>, List<T>> breeder,
                         Function<List<T>, List<T>> mutator,
                         Function<List<T>, Boolean> checker,
                         Consumer<List<T>> eliminator
    ) {
        this.maxIterations = maxIterations;
        this.picker = picker;
        this.breeder = breeder;
        this.mutator = mutator;
        this.checker = checker;
        this.eliminator = eliminator;
        iteration = 0L;
        this.solutions = solutions;
        evolutionState = EvolutionState.INITIAL_POPULATION;
    }

    public Boolean performStep() {
        if (EvolutionState.INITIAL_POPULATION.equals(evolutionState)) {
            evolutionState = EvolutionState.PICKING;
            if (makeSelections()) {
                evolutionState = EvolutionState.RESULT_ACHIEVED;
                return true;
            } else {
                evolutionState = EvolutionState.PICKED;
                return false;
            }
        }

        if (EvolutionState.PICKED.equals(evolutionState)) {
            evolutionState = EvolutionState.BREEDING;
            makeBreeding();
            evolutionState = EvolutionState.BREEDED;
            return false;
        }

        if (EvolutionState.BREEDED.equals(evolutionState)) {
            evolutionState = EvolutionState.MUTATION;
            makeMutating();
            evolutionState = EvolutionState.MUTATED;
            return false;
        }

        if (EvolutionState.MUTATED.equals(evolutionState)) {
            evolutionState = EvolutionState.AGGREGATING;
            aggregateResults();
            evolutionState = EvolutionState.PICKING;
            if (makeSelections()) {
                evolutionState = EvolutionState.RESULT_ACHIEVED;
            } else {
                evolutionState = EvolutionState.PICKED;
            }
        }

        iteration++;
        if (iteration >= maxIterations) {
            log.info("Limit achieved: {}", iteration);
            evolutionState = EvolutionState.LIMIT_ACHIEVED;
        }

//        log.info("Iteration: {}", iteration);
        return EvolutionState.LIMIT_ACHIEVED.equals(evolutionState) ||
               EvolutionState.RESULT_ACHIEVED.equals(evolutionState);
    }

    private void aggregateResults() {
        int total = 0;
        total += CollectionUtils.isEmpty(picked) ? 0 : picked.size();
        total += CollectionUtils.isEmpty(breeded) ? 0 : breeded.size();
        total += CollectionUtils.isEmpty(mutated) ? 0 : mutated.size();
        solutions = new ArrayList<>(total);
        if (!CollectionUtils.isEmpty(picked)) solutions.addAll(picked);
        if (!CollectionUtils.isEmpty(breeded)) solutions.addAll(breeded);
        if (!CollectionUtils.isEmpty(mutated)) solutions.addAll(mutated);
        if (eliminator != null) {
            eliminator.accept(solutions);
        }
    }


    private void makeMutating() {
        if (mutator != null) {
            mutated = mutator.apply(picked);
        }
    }

    private void makeBreeding() {
        if (breeder != null) {
            breeded = breeder.apply(picked);
        }
    }

    private boolean makeSelections() {
        if (picker != null) {
            picked = picker.apply(solutions);
            if (checker != null) {
                return checker.apply(picked);
            }
        }
        return false;
    }

    public Long getMaxIterations() {
        return maxIterations;
    }

    public Long getIteration() {
        return iteration;
    }

    public EvolutionState getEvolutionState() {
        return evolutionState;
    }

    public List<T> getSolutions() {
        return solutions;
    }

}
