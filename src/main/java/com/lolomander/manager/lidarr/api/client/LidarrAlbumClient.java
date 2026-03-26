package com.lolomander.manager.lidarr.api.client;

import com.lolomander.manager.lidarr.api.model.album.AlbumResource;
import com.lolomander.manager.lidarr.api.model.album.AlbumsMonitoredResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;


import java.util.List;
import java.util.Optional;

/**
 * Cliente reactivo para interactuar con los endpoints de Álbumes de Lidarr.
 */
@Slf4j
@Component
public class LidarrAlbumClient {

    private static final String ENDPOINT_ALBUM = "/album";
    private static final String ENDPOINT_ALBUM_ID = "/album/{id}";
    private static final String ENDPOINT_ALBUM_MONITOR = "/album/monitor";
    private static final String ENDPOINT_ALBUM_SEARCH = "/album/lookup";

    private final RestClient restClient;

    public LidarrAlbumClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Obtiene todos los álbumes de la biblioteca.
     */
    public List<AlbumResource> getAllAlbums() {
        return restClient.get()
                .uri(ENDPOINT_ALBUM)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * Obtiene un álbum específico por su ID interno de Lidarr.
     * @param id Identificador numérico interno del álbum.
     */
    public Optional<AlbumResource> getAlbumById(Integer id) {
        try {
            AlbumResource album = restClient.get()
                    .uri(ENDPOINT_ALBUM_ID, id)
                    .retrieve()
                    .body(AlbumResource.class);
            return Optional.ofNullable(album);
        } catch (HttpClientErrorException.NotFound e) {
            log(e, id);
            return Optional.empty();
        }
    }

    /**
     * Busca un álbum en la biblioteca local por su ID externo (MusicBrainz).
     * @param foreignAlbumId UUID de MusicBrainz.
     */
    public Optional<AlbumResource> getAlbumByExternalId(String foreignAlbumId) {
        List<AlbumResource> albums = restClient.get()
                                .uri(uriBuilder -> uriBuilder
                                .path(ENDPOINT_ALBUM)
                            .queryParam("foreignAlbumId", foreignAlbumId)
                            .build())
                            .retrieve()
                .body(new ParameterizedTypeReference<>() {});


        return albums == null ? Optional.empty() : albums.stream().findFirst();
    }

    /**
     * Obtiene todos los álbumes asociados a un artista específico.
     */
    public List<AlbumResource> getAlbumsByArtistId(Integer artistId) {
        return restClient.get()
                            .uri(uriBuilder -> uriBuilder
                                    .path(ENDPOINT_ALBUM)
                                    .queryParam("artistId", artistId)
                                    .build())
                            .retrieve()
                            .body(new ParameterizedTypeReference<>() {});
    }


    /**
     * Actualización masiva del estado de monitorización.
     */
    public void updateMonitorStatus(AlbumsMonitoredResource monitorResource) {
        restClient.put()
                .uri(ENDPOINT_ALBUM_MONITOR)
                .body(monitorResource)
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * Busca álbumes en el catálogo global de Lidarr (Search/Lookup).
     */
    public List<AlbumResource> searchAlbum(String term) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_ALBUM_SEARCH)
                        .queryParam("term", term)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }


    private void log(HttpClientErrorException.NotFound e, int id) {
        log.info("Album with id '{}' not found in Lidarr.", id);
        log.debug("HTTP Status  {}: {}", e.getStatusCode(), e.getMessage());
    }
}