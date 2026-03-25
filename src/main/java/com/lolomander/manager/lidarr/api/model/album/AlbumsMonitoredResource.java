package com.lolomander.manager.lidarr.api.model.album;

/**
 * DTO utilizado para la actualización masiva del estado de monitorización
 * de álbumes en Lidarr.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlbumMonitoredResource {

    /**
     * Lista de IDs internos de Lidarr para los álbumes que se desean actualizar.
     */
    private List<Integer> albumIds;

    /**
     * Define si los álbumes especificados deben estar monitoreados o no.
     */
    private Boolean monitored;
}