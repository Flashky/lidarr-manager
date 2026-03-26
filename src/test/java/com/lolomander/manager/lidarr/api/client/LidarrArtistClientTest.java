package com.lolomander.manager.lidarr.api.client;

import com.lolomander.manager.lidarr.api.model.artist.AddArtistOptions;
import com.lolomander.manager.lidarr.api.model.artist.ArtistResource;
import com.lolomander.manager.lidarr.api.model.artist.MonitorTypes;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class LidarrArtistClientTest {

    private static MockWebServer mockWebServer;
    private LidarrArtistClient lidarrArtistClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        ClassPathResource resource = new ClassPathResource("mocks/artist/" + fileName);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        RestClient restClient = RestClient.builder().baseUrl(baseUrl).build();
        lidarrArtistClient = new LidarrArtistClient(restClient);
    }

    @Test
    void getAllArtistsShouldReturnFluxOfArtists() throws IOException, InterruptedException {

        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody(loadJson("all_artists.json"))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        List<ArtistResource> result = lidarrArtistClient.getAllArtists();

        // Assert
        assertEquals(369, result.size());

        ArtistResource artist = result.getFirst();
        assertEquals("(həd) p.e.", artist.getArtistName());
        assertEquals(1, artist.getId());
        assertEquals(2, artist.getQualityProfileId());
        assertEquals(1, artist.getMetadataProfileId());
        assertEquals("19516266-e5d9-4774-b749-812bb76a6559", artist.getForeignArtistId());
        assertEquals("/data/music/", artist.getRootFolderPath());
        assertTrue(artist.getMonitored());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/artist", recordedRequest.getPath());
    }

    @Test
    void getAllArtistsShouldReturnReturnEmptyWhenNoArtists() throws IOException, InterruptedException {

        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody(loadJson("all_artists_empty.json"))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        List<ArtistResource> result = lidarrArtistClient.getAllArtists();

        // Assert
        assertTrue(result.isEmpty());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/artist", recordedRequest.getPath());

    }


    @Test
    void getArtistByIdShouldReturnSingleArtist() throws IOException, InterruptedException {
        // Arrange
        Integer artistId = 1;
        mockWebServer.enqueue(new MockResponse()
                .setBody(loadJson("get_artist_by_id.json"))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        Optional<ArtistResource> result = lidarrArtistClient.getArtistById(artistId);

        // Assert
        assertTrue(result.isPresent());

        ArtistResource artist = result.get();
        assertEquals("(həd) p.e.", artist.getArtistName());
        assertEquals(1, artist.getId());
        assertEquals(2, artist.getQualityProfileId());
        assertEquals(1, artist.getMetadataProfileId());
        assertEquals("19516266-e5d9-4774-b749-812bb76a6559", artist.getForeignArtistId());
        assertEquals("/data/music/", artist.getRootFolderPath());
        assertTrue(artist.getMonitored());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/artist/"+artistId, recordedRequest.getPath());
    }

    @Test
    void getArtistByIdShouldReturnEmptyWhenApiReturns4XX() throws InterruptedException  {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));

        // Act
        Optional<ArtistResource> result = lidarrArtistClient.getArtistById(123456);

        // Assert
        assertTrue(result.isEmpty());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/artist/123456", recordedRequest.getPath());
    }

    @Test
    void getArtistByExternalIdShouldReturnArtist() throws IOException, InterruptedException {

        String externalId = "19516266-e5d9-4774-b749-812bb76a6559";
        mockWebServer.enqueue(new MockResponse()
                .setBody(loadJson("get_artist_by_external_id.json"))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        Optional<ArtistResource> result = lidarrArtistClient.getArtistByExternalId(externalId);

        // Assert
        assertTrue(result.isPresent());
        ArtistResource artist = result.get();

        assertEquals("(həd) p.e.", artist.getArtistName());
        assertEquals(1, artist.getId());
        assertEquals(2, artist.getQualityProfileId());
        assertEquals(1, artist.getMetadataProfileId());
        assertEquals("19516266-e5d9-4774-b749-812bb76a6559", artist.getForeignArtistId());
        assertEquals("/data/music/", artist.getRootFolderPath());
        assertTrue(artist.getMonitored());


        // Verificamos que la URL se construyó correctamente con el query param
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/artist?mbId="+externalId, recordedRequest.getPath());
    }

    @Test
    void getArtistByExternalIdShouldReturnEmptyWhenNotFound() throws IOException, InterruptedException {

        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody(loadJson("all_artists_empty.json"))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        Optional<ArtistResource> result = lidarrArtistClient.getArtistByExternalId("non-existent-uuid");

        // Assert
        assertTrue(result.isEmpty());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/artist?mbId=non-existent-uuid", recordedRequest.getPath());
    }

    @Test
    void searchArtistShouldReturnListOfArtists() throws IOException, InterruptedException {

        // Arrange
        String term = "Hed PE";
        mockWebServer.enqueue(new MockResponse()
                .setBody(loadJson("search_artist.json")) // Un array con resultados de búsqueda
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        List<ArtistResource> result = lidarrArtistClient.searchArtist(term);

        // Assert
        assertEquals(20, result.size());

        ArtistResource artist = result.getFirst();
        assertNotNull(artist);
        assertEquals("(həd) p.e.", artist.getArtistName());
        assertEquals(1, artist.getId());
        assertEquals(2, artist.getQualityProfileId());
        assertEquals(1, artist.getMetadataProfileId());
        assertEquals("19516266-e5d9-4774-b749-812bb76a6559", artist.getForeignArtistId());
        assertTrue(artist.getMonitored());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/artist/lookup?term=Hed%20PE", recordedRequest.getPath());
    }

    @Test
    void searchArtistShouldReturnEmptyListOnNonExistingTerm() throws IOException, InterruptedException{
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody(loadJson("all_artists_empty.json"))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        List<ArtistResource> result = lidarrArtistClient.searchArtist("non-existent-term");

        // Assert
        assertTrue(result.isEmpty());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/artist/lookup?term=non-existent-term", recordedRequest.getPath());
    }

    @Test
    void addArtistShouldReturnCreatedArtist() throws IOException, InterruptedException {
        // Arrange
        AddArtistOptions addArtistOptions = AddArtistOptions.builder()
                .monitor(MonitorTypes.EXISTING)
                .albumsToMonitor(List.of("dff52d87-cb79-4b5d-acf3-85baab481ea0"))
                .monitored(true)
                .searchForMissingAlbums(true)
                .build();

        ArtistResource newArtist = ArtistResource.builder()
                .foreignArtistId("150fde6c-d8d6-4e59-8ff7-52cfe85497d9")
                .monitored(true)
                .artistName("Jeff Goldblum")
                .qualityProfileId(2)
                .metadataProfileId(1)
                .rootFolderPath("/data/music/")
                .addOptions(addArtistOptions)
                .build();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .setBody(loadJson("create_artist_201.json"))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        Optional<ArtistResource> result = lidarrArtistClient.createArtist(newArtist);

        // Assert - Verify result
        assertTrue(result.isPresent());

        ArtistResource artist = result.get();
        assertNotNull(artist.getId());
        assertEquals("150fde6c-d8d6-4e59-8ff7-52cfe85497d9", artist.getForeignArtistId());
        assertTrue(artist.getMonitored());
        assertEquals("Jeff Goldblum", artist.getArtistName());
        assertEquals(2, artist.getQualityProfileId());
        assertEquals(1, artist.getMetadataProfileId());
        assertEquals("/data/music/", artist.getRootFolderPath());

        AddArtistOptions responseAddOptions = artist.getAddOptions();
        assertNotNull(responseAddOptions);
        assertEquals("existing", responseAddOptions.getMonitor().getValue());
        assertFalse(responseAddOptions.getAlbumsToMonitor().isEmpty());
        assertEquals( "dff52d87-cb79-4b5d-acf3-85baab481ea0", responseAddOptions.getAlbumsToMonitor().getFirst());
        assertTrue(responseAddOptions.getMonitored());
        assertTrue(responseAddOptions.getSearchForMissingAlbums());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/artist", recordedRequest.getPath());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE));

        // Verify sent body
        assertTrue(recordedRequest.getBodySize() > 0);
        String requestBody = recordedRequest.getBody().readUtf8();
        ArtistResource sentArtist = objectMapper.readValue(requestBody, ArtistResource.class);

        assertEquals("150fde6c-d8d6-4e59-8ff7-52cfe85497d9", sentArtist.getForeignArtistId());
        assertTrue(sentArtist.getMonitored());
        assertEquals("Jeff Goldblum", sentArtist.getArtistName());
        assertEquals(2, sentArtist.getQualityProfileId());
        assertEquals(1, sentArtist.getMetadataProfileId());
        assertEquals("/data/music/", sentArtist.getRootFolderPath());

        AddArtistOptions sentResponseAddOptions = sentArtist.getAddOptions();
        assertNotNull(sentResponseAddOptions);
        assertEquals("existing", sentResponseAddOptions.getMonitor().getValue());
        assertFalse(sentResponseAddOptions.getAlbumsToMonitor().isEmpty());
        assertEquals( "dff52d87-cb79-4b5d-acf3-85baab481ea0", sentResponseAddOptions.getAlbumsToMonitor().getFirst());
        assertTrue(sentResponseAddOptions.getMonitored());
        assertTrue(sentResponseAddOptions.getSearchForMissingAlbums());
    }
}