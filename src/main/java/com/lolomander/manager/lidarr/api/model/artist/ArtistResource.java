package com.lolomander.manager.lidarr.api.model.artist;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtistResource {
    private Integer id;
    private String artistName;
    private String foreignArtistId; // MusicBrainz ID
    private Boolean monitored;
    private String rootFolderPath;
    private Integer qualityProfileId;
    private Integer metadataProfileId;
    private AddArtistOptions addOptions;
}
