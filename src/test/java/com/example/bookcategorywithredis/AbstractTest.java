package com.example.bookcategorywithredis;

import com.example.bookcategorywithredis.mapper.BookMapper;
import com.example.bookcategorywithredis.model.BookResponse;
import com.example.bookcategorywithredis.model.UpsertBookRequest;
import com.example.bookcategorywithredis.repository.BookRepository;
import com.example.bookcategorywithredis.service.BookService;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.redis.core.RedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest
@Sql("classpath:db/init.sql")
@Transactional
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Testcontainers
public class AbstractTest {

    public static final Integer UPDATED_ID = 4;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected BookMapper bookMapper;

    @Autowired
    protected BookService bookService;

    @Autowired
    protected BookRepository bookRepository;

    @RegisterExtension
    protected static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    protected static PostgreSQLContainer postgreSQLContainer;

    @Container
    protected static final RedisContainer REDIS_CONTAINER = new RedisContainer(DockerImageName.parse("redis:7.0.12"))
            .withExposedPorts(6379)
            .withReuse(true);

    static {
        DockerImageName postgresql = DockerImageName.parse("postgres:12.3");

        postgreSQLContainer = (PostgreSQLContainer) new PostgreSQLContainer(postgresql)
                .withReuse(true);
        postgreSQLContainer.start();
    }

    @DynamicPropertySource
    public static void registerProperties(DynamicPropertyRegistry registry) {
        String jdbcUrl = postgreSQLContainer.getJdbcUrl();

        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.url", () -> jdbcUrl);

        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379).toString());

        registry.add("app.integration.base-url", wireMockServer::baseUrl);
    }

    @BeforeEach
    public void before() throws Exception {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();

        stubClient();
    }

    @AfterEach
    public void afterEach() {
        wireMockServer.resetAll();
    }

    private void stubClient() throws Exception {
        List<BookResponse> findAllResponseBody = new ArrayList<>();

        findAllResponseBody.add(new BookResponse(1, "FirstAuthor", "FirstTitleBook", "FirstCategory"));
        findAllResponseBody.add(new BookResponse(2, "SecondAuthor", "SecondTitleBook", "FirstCategory"));

        wireMockServer.stubFor(WireMock.get("/api/book")
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(findAllResponseBody))
                        .withStatus(200)));

        BookResponse findByTitleAndAuthorResponseBody = new BookResponse(1, "FirstAuthor", "FirstTitleBook", "FirstCategory");

        wireMockServer.stubFor(WireMock.get("/api/book/findByTitleAndAuthor/" + findByTitleAndAuthorResponseBody.getTitle() + "/" + findByTitleAndAuthorResponseBody.getAuthor())
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(findByTitleAndAuthorResponseBody))
                        .withStatus(200)));

        BookResponse findByIdResponseBody = new BookResponse(1, "FirstAuthor", "FirstTitleBook", "FirstCategory");

        wireMockServer.stubFor(WireMock.get("/api/book/findById/" + 1)
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(findByIdResponseBody))
                        .withStatus(200)));

        List<BookResponse> findAllByCategoryTitleResponseBody = new ArrayList<>();

        findAllByCategoryTitleResponseBody.add(new BookResponse(1, "FirstAuthor", "FirstTitleBook", "FirstCategory"));
        findAllByCategoryTitleResponseBody.add(new BookResponse(2, "SecondAuthor", "SecondTitleBook", "FirstCategory"));

        wireMockServer.stubFor(WireMock.get("/api/book/findByCategoryTitle/" + findAllByCategoryTitleResponseBody.get(1).getCategoryTitle())
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(findAllByCategoryTitleResponseBody))
                        .withStatus(200)));

        UpsertBookRequest createRequest = new UpsertBookRequest();
        createRequest.setAuthor("newAuthor");
        createRequest.setTitle("newTitleBook");
        createRequest.setCategoryTitle("newCategoryTitle");
        BookResponse createResponseBody = new BookResponse(1, "newAuthor", "newTitleBook", "newCategory");

        wireMockServer.stubFor(WireMock.post("/api/book")
                .withRequestBody(equalToJson(objectMapper.writeValueAsString(createRequest)))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(createResponseBody))
                        .withStatus(201)));

        UpsertBookRequest upsertRequest = new UpsertBookRequest();
        upsertRequest.setAuthor("updateAuthor");
        upsertRequest.setTitle("updateTitleBook");
        upsertRequest.setCategoryTitle("updateCategoryTitle");
        BookResponse updatedResponseBody = new BookResponse(1, "updateAuthor", "updateTitleBook", "updateCategory");

        wireMockServer.stubFor(WireMock.put("/api/book/" + UPDATED_ID)
                .withRequestBody(equalToJson(objectMapper.writeValueAsString(upsertRequest)))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(updatedResponseBody))
                        .withStatus(200)));

        wireMockServer.stubFor(WireMock.delete("/api/book/" + UPDATED_ID)
                .willReturn(aResponse().withStatus(204)));
    }
}
