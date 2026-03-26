package com.lolomander.manager.lidarr.api.client;

import com.lolomander.manager.lidarr.api.model.artist.ArtistResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

/**
 * Cliente de infraestructura para interactuar con los endpoints de Artistas en Lidarr.
 * Implementa reintentos automáticos y manejo de errores reactivos.
 */
@Slf4j
@Component
public class LidarrArtistClient {

    private static final String ENDPOINT_ARTIST = "/artist";
    private static final String ENDPOINT_ARTIST_ID = "/artist/{id}";
    private static final String ENDPOINT_ARTIST_LOOKUP = "/artist/lookup";

    private final RestClient restClient;

    public LidarrArtistClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Obtiene todos los artistas de la biblioteca de Lidarr.
     */
    public List<ArtistResource> getAllArtists() {
        return restClient.get()
                .uri(ENDPOINT_ARTIST)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * Busca un artista por su ID interno de Lidarr.
     * @param id Identificador numérico interno.
     */
    public Optional<ArtistResource> getArtistById(Integer id) {
        try {
            ArtistResource artist = restClient.get()
                    .uri(ENDPOINT_ARTIST_ID, id)
                    .retrieve()
                    .body(ArtistResource.class);
            return Optional.ofNullable(artist);
        } catch (HttpClientErrorException.NotFound e) {
            log(e, id);
            return Optional.empty();
        }
    }

    /**
     * Busca un artista en la biblioteca local usando su identificador externo (MusicBrainz).
     * Utiliza el query parameter 'mbId' soportado por el endpoint /artist.
     * @param foreignArtistId UUID de MusicBrainz.
     */
    public Optional<ArtistResource> getArtistByExternalId(String foreignArtistId) {
        List<ArtistResource> artists = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_ARTIST)
                        .queryParam("mbId", foreignArtistId)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        return artists == null ? Optional.empty() : artists.stream().findFirst();
    }

    /**
     * Busca artistas en los metadatos globales (MusicBrainz) a través de Lidarr.
     * @param term Término de búsqueda (Nombre del artista).
     */
    public List<ArtistResource> searchArtist(String term) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_ARTIST_LOOKUP)
                        .queryParam("term", term)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * Añade un nuevo artista a la biblioteca de Lidarr.
     * @param artist Datos del artista a crear.
     */
    public Optional<ArtistResource> createArtist(ArtistResource artist) {
        ArtistResource createdArtist = restClient.post()
                .uri(ENDPOINT_ARTIST)
                .body(artist)
                .retrieve()
                .body(ArtistResource.class);

        return Optional.ofNullable(createdArtist);
    }

    private void log(HttpClientErrorException.NotFound e, int id) {
        log.info("Artist with id '{}' not found in Lidarr.", id);
        log.debug("HTTP Status  {}: {}", e.getStatusCode(), e.getMessage());
    }
}