package ru.grachoffs;

public enum EvolutionState {
    NOT_RUNNING,
    INITIAL_POPULATION,
    PICKING,
    PICKED,
    RESULT_ACHIEVED,
    LIMIT_ACHIEVED,
    BREEDING,
    BREEDED,
    MUTATION,
    MUTATED,
    AGGREGATING
}
