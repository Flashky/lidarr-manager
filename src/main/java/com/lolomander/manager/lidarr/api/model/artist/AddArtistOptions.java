package com.lolomander.manager.lidarr.api.model.artist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.lolomander.manager.lidarr.api.model.common.MonitorTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddArtistOptions {
    private MonitorTypes monitor;
    private List<String> albumsToMonitor;
    private Boolean monitored;
    private Boolean searchForMissingAlbums;
}