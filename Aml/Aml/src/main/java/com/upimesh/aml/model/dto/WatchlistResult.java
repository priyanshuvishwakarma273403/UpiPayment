package com.upimesh.aml.model.dto;

public record WatchlistResult(
        boolean matched,
        String matchedEntity,
        String matchType,
        String source
) {}
