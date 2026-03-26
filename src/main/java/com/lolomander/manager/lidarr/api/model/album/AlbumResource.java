package com.lolomander.manager.lidarr.api.model.album;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un recurso de Álbum en la API de Lidarr.
 * Se utiliza tanto para la persistencia local como para los resultados de búsqueda (Lookup).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlbumResource {

    private Integer id;
    private String title;
    private Integer artistId;
    private String foreignAlbumId;
    private Boolean monitored;
    private AddAlbumOptions addOptions;

}