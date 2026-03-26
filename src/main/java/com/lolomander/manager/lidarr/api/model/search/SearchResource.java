package com.lolomander.manager.lidarr.api.model.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.lolomander.manager.lidarr.api.model.artist.ArtistResource;
import com.lolomander.manager.lidarr.api.model.album.AlbumResource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para el recurso de búsqueda de Lidarr (/api/v1/search).
 * Este recurso es un contenedor que puede representar tanto a un artista como a un álbum.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResource {

    private Integer id;
    private String foreignId;
    private ArtistResource artist;
    private AlbumResource album;

    public boolean isAlbum() {
        return album != null;
    }

    public boolean isArtist() {
        return artist != null;
    }
}