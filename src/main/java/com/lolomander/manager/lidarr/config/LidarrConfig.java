package com.lolomander.manager.lidarr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuración centralizada para la comunicación con la API de Lidarr.
 * Instancia un WebClient preconfigurado con la URL base y la API Key.
 */
@Configuration
public class LidarrConfig {

    @Value("${lidarr.url}")
    private String lidarrUrl;

    @Value("${lidarr.api-key}")
    private String apiKey;

    /**
     * Define el bean de WebClient que será inyectado en los clientes del API.
     * Configura la cabecera X-Api-Key necesaria para todas las peticiones a Lidarr.
     */
    @Bean
    public WebClient lidarrWebClient(WebClient.Builder builder) {

        // Configuramos estrategias de intercambio para permitir buffers más grandes (16MB)
        // por si la respuesta de búsqueda o listado de álbumes es muy pesada.
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();

        return builder
                .baseUrl(lidarrUrl + "/api/v1")
                .defaultHeader("X-Api-Key", apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(strategies)
                .build();
    }
}