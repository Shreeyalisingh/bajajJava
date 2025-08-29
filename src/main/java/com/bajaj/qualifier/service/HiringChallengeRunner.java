package com.bajaj.qualifier.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import com.bajaj.qualifier.config.AppProperties;
import com.bajaj.qualifier.dto.GenerateWebhookRequest;
import com.bajaj.qualifier.dto.GenerateWebhookResponse;
import com.bajaj.qualifier.model.AnswerRecord;
import com.bajaj.qualifier.repo.AnswerRecordRepository;

@Component
public class HiringChallengeRunner {

  private final RestTemplate restTemplate;
  private final AppProperties props;
  private final AnswerRecordRepository repo;

  public HiringChallengeRunner(RestTemplate restTemplate, AppProperties props, AnswerRecordRepository repo) {
    this.restTemplate = restTemplate;
    this.props = props;
    this.repo = repo;
  }

  @Bean
  public ApplicationRunner runOnStartup() {
    return args -> {
      // 1) Generate webhook
      String generateUrl = props.getBaseUrl() + props.getGeneratePath();

      GenerateWebhookRequest genReq = new GenerateWebhookRequest(
          props.getName(), props.getRegNo(), props.getEmail()
      );

      HttpHeaders genHeaders = new HttpHeaders();
      genHeaders.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<GenerateWebhookRequest> genEntity = new HttpEntity<>(genReq, genHeaders);

      ResponseEntity<GenerateWebhookResponse> genResp =
          restTemplate.postForEntity(generateUrl, genEntity, GenerateWebhookResponse.class);

      if (!genResp.getStatusCode().is2xxSuccessful() || genResp.getBody() == null) {
        throw new IllegalStateException("Failed to generate webhook: " + genResp.getStatusCode());
      }

      String webhookUrl = genResp.getBody().getWebhook();
      String accessToken = genResp.getBody().getAccessToken();

      if (webhookUrl == null || webhookUrl.isBlank()) {
        // fall back to fixed path if API returns none
        webhookUrl = props.getBaseUrl() + props.getSubmitFallbackPath();
      }

      // 2) Decide question based on regNo last two digits
      String reg = props.getRegNo().trim();
      String last2 = reg.substring(Math.max(0, reg.length() - 2));
      // SAFE: no backslash escapes
      int lastTwoDigits = Integer.parseInt(last2.replaceAll("[^0-9]", "0"));
      int question = (lastTwoDigits % 2 == 1) ? 1 : 2;

      // 3) Load your final SQL from resources/queries/q{1|2}.sql
      String sqlPath = "queries/q" + question + ".sql";
      String finalSql = loadSqlFromClasspath(sqlPath).trim();

      if (finalSql.isBlank()) {
        throw new IllegalStateException("Final SQL is empty. Put your answer in " + sqlPath);
      }

      // 4) Store the result in DB
      AnswerRecord rec = new AnswerRecord();
      rec.setRegNo(reg);
      rec.setQuestionNumber(question);
      rec.setFinalQuery(finalSql);
      repo.save(rec);

      // 5) Submit answer
      Map<String, String> payload = new HashMap<>();
      payload.put("finalQuery", finalSql);

      HttpHeaders submitHeaders = new HttpHeaders();
      submitHeaders.setContentType(MediaType.APPLICATION_JSON);
      submitHeaders.set("Authorization", accessToken);

      HttpEntity<Map<String, String>> submitEntity = new HttpEntity<>(payload, submitHeaders);
      ResponseEntity<String> submitResp = restTemplate.postForEntity(webhookUrl, submitEntity, String.class);

      if (submitResp.getStatusCode().is2xxSuccessful()) {
        System.out.println("✅ Successfully submitted solution. Response: " + submitResp.getBody());
      } else {
        System.out.println("⚠️ Submission returned status: " + submitResp.getStatusCode() + " body=" + submitResp.getBody());
      }
    };
  }

  private String loadSqlFromClasspath(String path) {
    try {
      ClassPathResource res = new ClassPathResource(path);
      try (InputStream in = res.getInputStream()) {
        return StreamUtils.copyToString(in, StandardCharsets.UTF_8);
      }
    } catch (Exception e) {
      throw new RuntimeException("Cannot read " + path + ": " + e.getMessage(), e);
    }
  }
}
