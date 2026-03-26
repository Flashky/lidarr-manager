package com.lolomander.manager.lidarr.api.client;

import com.lolomander.manager.lidarr.api.model.album.AlbumResource;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LidarrAlbumClientTest {

    private static MockWebServer mockWebServer;
    private LidarrAlbumClient lidarrAlbumClient;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    private String loadJson(String fileName) throws IOException {
        ClassPathResource resource = new ClassPathResource("mocks/album/" + fileName);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        RestClient restClient = RestClient.builder().baseUrl(baseUrl).build();
        lidarrAlbumClient = new LidarrAlbumClient(restClient);
    }

    @Test
    void getAllAlbumsShouldReturnListOfAlbums() throws IOException, InterruptedException {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody(loadJson("all_albums.json"))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        List<AlbumResource> result = lidarrAlbumClient.getAllAlbums();

        // Assert
        assertEquals(6, result.size());

        AlbumResource album = result.getFirst();
        assertNotNull(album);
        assertEquals(29, album.getId());
        assertEquals("Jazz Chill", album.getTitle());
        assertEquals(20, album.getArtistId());
        assertEquals("0cc32b1a-0d86-4b85-aff9-28582bebbc5f", album.getForeignAlbumId());
        assertTrue(album.getMonitored());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/album", recordedRequest.getPath());
    }

    @Test
    void getAlbumsByArtistIdShouldReturnOptional() throws IOException, InterruptedException {
        // Arrange
        Integer artistId = 1;
        mockWebServer.enqueue(new MockResponse()
                .setBody(loadJson("get_albums_by_artistId.json"))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        List<AlbumResource> result = lidarrAlbumClient.getAlbumsByArtistId(artistId);

        // Assert
        assertFalse(result.isEmpty());

        AlbumResource album = result.getFirst();
        assertNotNull(album);
        assertEquals(29, album.getId());
        assertEquals("Jazz Chill", album.getTitle());
        assertEquals(20, album.getArtistId());
        assertEquals("0cc32b1a-0d86-4b85-aff9-28582bebbc5f", album.getForeignAlbumId());
        assertTrue(album.getMonitored());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/album?artistId="+artistId, recordedRequest.getPath());
    }

    @Test
    void getAlbumByIdShouldReturnSingleAlbum() throws IOException, InterruptedException {
        // Arrange
        Integer albumId = 29;
        mockWebServer.enqueue(new MockResponse()
                .setBody(loadJson("get_album_by_id.json"))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        Optional<AlbumResource> result = lidarrAlbumClient.getAlbumById(albumId);

        // Assert
        assertTrue(result.isPresent());

        AlbumResource album = result.get();
        assertNotNull(album);
        assertEquals(29, album.getId());
        assertEquals("Jazz Chill", album.getTitle());
        assertEquals(20, album.getArtistId());
        assertEquals("0cc32b1a-0d86-4b85-aff9-28582bebbc5f", album.getForeignAlbumId());
        assertTrue(album.getMonitored());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/album/" + albumId, recordedRequest.getPath());
    }

    @Test
    void getAlbumByExternalIdShouldReturnAlbum() throws IOException, InterruptedException {

        // Arrange
        String externalId = "0cc32b1a-0d86-4b85-aff9-28582bebbc5f";
        mockWebServer.enqueue(new MockResponse()
                .setBody(loadJson("get_album_by_external_id.json")) // Devuelve array con 1 elemento
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        Optional<AlbumResource> result = lidarrAlbumClient.getAlbumByExternalId(externalId);

        // Assert
        assertTrue(result.isPresent());

        AlbumResource album = result.get();
        assertNotNull(album);
        assertEquals(29, album.getId());
        assertEquals("Jazz Chill", album.getTitle());
        assertEquals(20, album.getArtistId());
        assertEquals("0cc32b1a-0d86-4b85-aff9-28582bebbc5f", album.getForeignAlbumId());
        assertTrue(album.getMonitored());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/album?foreignAlbumId="+externalId, recordedRequest.getPath());
    }

    @Test
    void lookupAlbumShouldReturnResults() throws IOException, InterruptedException {
        // Arrange
        String term = "Black Album";
        mockWebServer.enqueue(new MockResponse()
                .setBody(loadJson("album_lookup.json"))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        List<AlbumResource> result = lidarrAlbumClient.searchAlbum(term);

        // Assert
        assertEquals(19, result.size());
        AlbumResource album = result.getFirst();
        assertNull(album.getId()); // Album is not added to Lidarr
        assertEquals(0, album.getArtistId());
        assertEquals("Black Album", album.getTitle());
        assertEquals("0f180796-2f64-35fb-a62e-bf690dcf7229", album.getForeignAlbumId());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/album/lookup?term=Black%20Album", recordedRequest.getPath());
    }
}