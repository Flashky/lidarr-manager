package com.lolomander.manager.lidarr.api.client;

import com.lolomander.manager.lidarr.api.model.search.SearchResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cliente para interactuar con el endpoint de búsqueda global de Lidarr.
 */
@Slf4j
@Component
public class LidarrSearchClient {

    private static final String ENDPOINT_SEARCH = "/search";

    private final RestClient restClient;

    public LidarrSearchClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Realiza una búsqueda global en Lidarr.
     *
     * @param term Término de búsqueda (Nombre de artista, álbum o combinación).
     * @return Lista con los resultados encontrados.
     */
    public List<SearchResource> search(String term) {

        List<List<SearchResource>> nestedResults = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_SEARCH)
                        .queryParam("term", term)
                        .build())
                .retrieve()
                // La API devuelve List<List<SearchResource>>
                .body(new ParameterizedTypeReference<>() {
                });

        if (nestedResults == null || nestedResults.isEmpty()) {
            return Collections.emptyList();
        }

        // Extract nested result
        return nestedResults.getFirst();

    }

    /**
     * Realiza una búsqueda global de álbumes en Lidarr.
     * @param term Término de búsqueda
     * @return Lista con los álbumes encontrados.
     */
    public List<SearchResource> searchAlbums(String term) {
        return search(term).stream()
                .filter(SearchResource::isAlbum)
                .collect(Collectors.toList());
    }

    /**
     * Realiza una búsqueda global de artistas en Lidarr.
     * @param term Término de búsqueda
     * @return Lista con los artistas encontrados.
     */
    public List<SearchResource> searchArtists(String term) {
        return search(term).stream()
                .filter(SearchResource::isArtist)
                .collect(Collectors.toList());
    }

}