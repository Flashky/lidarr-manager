package com.lolomander.manager.lidarr.api.client;

import com.lolomander.manager.lidarr.api.model.album.AlbumResource;
import com.lolomander.manager.lidarr.api.model.artist.ArtistResource;
import com.lolomander.manager.lidarr.api.model.search.SearchResource;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LidarrSearchClientTest {

    private static MockWebServer mockWebServer;
    private LidarrSearchClient searchClient;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    /**
     * Método auxiliar para cargar archivos JSON desde el classpath.
     */
    private String loadJson(String fileName) throws IOException {
        ClassPathResource resource = new ClassPathResource("mocks/search/" + fileName);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        searchClient = new LidarrSearchClient(restClient);
    }

    @Test
    void searchShouldReturnFlattenedList() throws Exception {

        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody(loadJson("search.json"))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        List<SearchResource> results = searchClient.search("Metallica");

        // Assert
        assertNotNull(results);
        assertEquals(10, results.size());

        SearchResource firstResource = results.getFirst();
        assertEquals(1, firstResource.getId());
        assertEquals("65f4f0c5-ef9e-490c-aee3-909e7ae6b2ab", firstResource.getForeignId());
        assertTrue(firstResource.isArtist());
        assertFalse(firstResource.isAlbum());

        ArtistResource artist = firstResource.getArtist();
        assertEquals(172, artist.getId());
        assertEquals("65f4f0c5-ef9e-490c-aee3-909e7ae6b2ab", artist.getForeignArtistId());
        assertEquals("Metallica", artist.getArtistName());

        SearchResource secondResource = results.get(1);
        assertEquals(2, secondResource.getId());
        assertEquals("e8f70201-8899-3f0c-9e07-5d6495bc8046", secondResource.getForeignId());
        assertFalse(secondResource.isArtist());
        assertTrue(secondResource.isAlbum());

        AlbumResource album = secondResource.getAlbum();
        assertEquals(335, album.getId());
        assertEquals("e8f70201-8899-3f0c-9e07-5d6495bc8046", album.getForeignAlbumId());
        assertEquals("Metallica", album.getTitle());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/search?term=Metallica", recordedRequest.getPath());
    }

    @Test
    void searchAlbumsShouldOnlyReturnItemsWithAlbum() throws Exception {

        mockWebServer.enqueue(new MockResponse()
                .setBody(loadJson("search.json"))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        List<SearchResource> results = searchClient.searchAlbums("Metallica");

        // Assert
        assertEquals(9, results.size());
        results.forEach(result -> {
            assertTrue(result.isAlbum());
            assertFalse(result.isArtist());
        });

        AlbumResource album = results.getFirst().getAlbum();
        assertEquals(335, album.getId());
        assertEquals("e8f70201-8899-3f0c-9e07-5d6495bc8046", album.getForeignAlbumId());
        assertEquals("Metallica", album.getTitle());

    }

    @Test
    void searchArtistsShouldOnlyReturnItemsWithArtist() throws Exception {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody(loadJson("search.json"))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        List<SearchResource> results = searchClient.searchArtists("Metallica");

        // Assert
        assertEquals(1, results.size());

        SearchResource firstResource = results.getFirst();
        assertEquals(1, firstResource.getId());
        assertEquals("65f4f0c5-ef9e-490c-aee3-909e7ae6b2ab", firstResource.getForeignId());
        assertTrue(firstResource.isArtist());
        assertFalse(firstResource.isAlbum());

        ArtistResource artist = firstResource.getArtist();
        assertEquals(172, artist.getId());
        assertEquals("65f4f0c5-ef9e-490c-aee3-909e7ae6b2ab", artist.getForeignArtistId());
        assertEquals("Metallica", artist.getArtistName());
    }

    @Test
    void searchShouldReturnEmptyListOnEmptyResponse() throws Exception  {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody(loadJson("search_empty.json"))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        List<SearchResource> results = searchClient.search("3412adas");

        // Assert
        assertTrue(results.isEmpty());
    }
}