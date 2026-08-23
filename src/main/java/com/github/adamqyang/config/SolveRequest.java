package com.github.adamqyang.config;

/**
 * Everything needed to write one problems.txt entry. Strategy conditions are
 * kept as raw Stelvio syntax (e.g. "fm:w>=5 AND cc:Sb1>=3") rather than a
 * structured model — that syntax is its own small grammar, and building an
 * editor for it is a separate feature from getting the core solve loop working.
 */
public record SolveRequest(String fen, MoveCount moveCount, String strategyConditions, StelvioSettings settings) {

    public boolean hasStrategyConditions() {
        return strategyConditions != null && !strategyConditions.isBlank();
    }
}
