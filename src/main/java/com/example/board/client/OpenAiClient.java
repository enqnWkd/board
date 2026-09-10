package com.example.board.client;

import com.example.board.exception.ContentInspectionException;
import com.example.board.exception.Errorcode;
import com.example.board.service.ContentInspectionResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OpenAiClient {

    private static final int TIMEOUT_MILLIS = 10_000;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public OpenAiClient(
            ObjectMapper objectMapper,
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.base-url}") String baseUrl,
            @Value("${openai.model}") String model
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(TIMEOUT_MILLIS);
        requestFactory.setReadTimeout(TIMEOUT_MILLIS);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.objectMapper = objectMapper;
        this.model = model;
    }

    public ContentInspectionResult inspect(String title, String content) {
        try {
            JsonNode response = restClient.post()
                    .uri("/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(createRequest(title, content))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, clientResponse) -> {
                        log.warn("OpenAI 콘텐츠 검사 요청 실패 - status: {}", clientResponse.getStatusCode());
                        throw new ContentInspectionException(Errorcode.OPENAI_SERVICE_UNAVAILABLE);
                    })
                    .body(JsonNode.class);

            return parseResult(response);
        } catch (ContentInspectionException e) {
            throw e;
        } catch (RestClientException | IllegalArgumentException e) {
            log.warn("OpenAI 콘텐츠 검사 호출 또는 응답 처리 실패", e);
            throw new ContentInspectionException(Errorcode.OPENAI_SERVICE_UNAVAILABLE);
        }
    }

    private Map<String, Object> createRequest(String title, String content) {
        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "classification", Map.of(
                                "type", "string",
                                "enum", List.of("NORMAL", "ADVERTISEMENT", "INAPPROPRIATE")
                        )
                ),
                "required", List.of("classification"),
                "additionalProperties", false
        );

        return Map.of(
                "model", model,
                "store", false,
                "instructions", "Classify the submitted board post as NORMAL, ADVERTISEMENT, or INAPPROPRIATE. "
                        + "Classify promotional or commercial spam as ADVERTISEMENT and harmful, abusive, or otherwise unsuitable content as INAPPROPRIATE.",
                "input", "Title: " + title + "\n\nContent: " + content,
                "text", Map.of(
                        "format", Map.of(
                                "type", "json_schema",
                                "name", "article_content_inspection",
                                "strict", true,
                                "schema", schema
                        )
                )
        );
    }

    private ContentInspectionResult parseResult(JsonNode response) {
        if (response == null || !"completed".equals(response.path("status").asText())) {
            throw new ContentInspectionException(Errorcode.OPENAI_SERVICE_UNAVAILABLE);
        }

        JsonNode outputText = response.path("output")
                .findValue("content");

        if (outputText == null || !outputText.isArray()) {
            throw new ContentInspectionException(Errorcode.OPENAI_SERVICE_UNAVAILABLE);
        }

        for (JsonNode content : outputText) {
            if ("output_text".equals(content.path("type").asText())) {
                return toResult(content.path("text").asText(null));
            }
        }

        throw new ContentInspectionException(Errorcode.OPENAI_SERVICE_UNAVAILABLE);
    }

    private ContentInspectionResult toResult(String outputText) {
        if (outputText == null) {
            throw new ContentInspectionException(Errorcode.OPENAI_SERVICE_UNAVAILABLE);
        }

        try {
            JsonNode result = objectMapper.readTree(outputText);
            return ContentInspectionResult.valueOf(result.path("classification").asText());
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new ContentInspectionException(Errorcode.OPENAI_SERVICE_UNAVAILABLE);
        }
    }
}
