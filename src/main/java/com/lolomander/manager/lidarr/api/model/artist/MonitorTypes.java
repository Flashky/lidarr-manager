package com.lolomander.manager.lidarr.api.model.artist;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MonitorTypes {

    ALL("all"),
    FUTURE("future"),
    MISSING("missing"),
    EXISTING("existing"),
    LATEST("latest"),
    FIRST("first"),
    NONE("none"),
    UNKNOWN("unknown");

    @JsonValue
    private final String value;

}