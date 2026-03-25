package com.lolomander.manager.lidarr.api.client;

import com.lolomander.manager.lidarr.api.model.artist.ArtistResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Cliente de infraestructura para interactuar con los endpoints de Artistas en Lidarr.
 * Implementa reintentos automáticos y manejo de errores reactivos.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LidarrArtistClient {

    private static final String ENDPOINT_ARTIST = "/artist";
    private static final String ENDPOINT_ARTIST_ID = "/artist/{id}";
    private static final String ENDPOINT_ARTIST_LOOKUP = "/artist/lookup";

    private final WebClient lidarrWebClient;

    /**
     * Obtiene todos los artistas de la biblioteca de Lidarr.
     */
    public Flux<ArtistResource> getAllArtists() {
        return lidarrWebClient.get()
                .uri(ENDPOINT_ARTIST)
                .retrieve()
                .bodyToFlux(ArtistResource.class)
                .retryWhen(createRetrySpec("getAllArtists"))
                .onErrorResume(e -> {
                    log.error("Error al obtener artistas de Lidarr: {}", e.getMessage());
                    return Flux.empty();
                });
    }

    /**
     * Busca un artista por su ID interno de Lidarr.
     * @param id Identificador numérico interno.
     */
    public Mono<ArtistResource> getArtistById(Integer id) {
        return lidarrWebClient.get()
                .uri(ENDPOINT_ARTIST_ID, id)
                .retrieve()
                .bodyToMono(ArtistResource.class)
                .retryWhen(createRetrySpec("getArtistById"))
                .onErrorResume(e -> {
                    log.error("Error al obtener artista con ID {}: {}", id, e.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Busca un artista en la biblioteca local usando su identificador externo (MusicBrainz).
     * Utiliza el query parameter 'mbId' soportado por el endpoint /artist.
     * @param musicBrainzId UUID de MusicBrainz (foreignArtistId).
     */
    public Mono<ArtistResource> getArtistByExternalId(String musicBrainzId) {
        return lidarrWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_ARTIST)
                        .queryParam("mbId", musicBrainzId)
                        .build())
                .retrieve()
                .bodyToFlux(ArtistResource.class)
                .next() // Tomamos el primer resultado del array filtrado
                .retryWhen(createRetrySpec("getArtistByExternalId"))
                .doOnNext(a -> log.debug("Artista encontrado por MusicBrainzId {}: {}", musicBrainzId, a.getArtistName()))
                .onErrorResume(e -> {
                    log.error("Error buscando artista por MBID {}: {}", musicBrainzId, e.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Busca artistas en los metadatos globales (MusicBrainz) a través de Lidarr.
     * @param term Término de búsqueda (Nombre del artista).
     */
    public Flux<ArtistResource> searchArtist(String term) {
        return lidarrWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ENDPOINT_ARTIST_LOOKUP)
                        .queryParam("term", term)
                        .build())
                .retrieve()
                .bodyToFlux(ArtistResource.class)
                .retryWhen(createRetrySpec("searchArtist"))
                .onErrorResume(e -> {
                    log.error("Error buscando artista '{}': {}", term, e.getMessage());
                    return Flux.empty();
                });
    }

    /**
     * Añade un nuevo artista a la biblioteca de Lidarr.
     * @param artist Datos del artista a crear.
     */
    public Mono<ArtistResource> createArtist(ArtistResource artist) {
        return lidarrWebClient.post()
                .uri(ENDPOINT_ARTIST)
                .bodyValue(artist)
                .retrieve()
                .bodyToMono(ArtistResource.class)
                .retryWhen(createRetrySpec("createArtist"))
                .doOnSuccess(a -> log.info("Artista añadido con éxito: {}", a.getArtistName()))
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode().is4xxClientError()) {
                        log.error("Error de cliente al crear artista (Posible duplicado): {}", e.getResponseBodyAsString());
                    }
                    return Mono.empty();
                });
    }

    /**
     * Configura la estrategia de reintento:
     * Solo reintenta en Timeouts o errores 5xx del servidor.
     */
    private Retry createRetrySpec(String operationName) {
        return Retry.backoff(3, Duration.ofSeconds(1))
                .filter(this::isRetryableException)
                .doBeforeRetry(retrySignal ->
                        log.warn("Reintentando {}... Intento: {}", operationName, retrySignal.totalRetries() + 1)
                );
    }

    /**
     * Determina si la excepción es candidata a reintento (problemas transitorios).
     */
    private boolean isRetryableException(Throwable error) {
        return (error instanceof WebClientResponseException webclientresponseexception &&
                webclientresponseexception.getStatusCode().is5xxServerError());
    }
}