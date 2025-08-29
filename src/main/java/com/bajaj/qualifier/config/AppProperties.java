package com.bajaj.qualifier.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "challenge")
public class AppProperties {
  private String baseUrl;
  private String generatePath;
  private String submitFallbackPath;
  private String name;
  private String regNo;
  private String email;
  public String getBaseUrl() { return baseUrl; }
  public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
  public String getGeneratePath() { return generatePath; }
  public void setGeneratePath(String generatePath) { this.generatePath = generatePath; }
  public String getSubmitFallbackPath() { return submitFallbackPath; }
  public void setSubmitFallbackPath(String submitFallbackPath) { this.submitFallbackPath = submitFallbackPath; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getRegNo() { return regNo; }
  public void setRegNo(String regNo) { this.regNo = regNo; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
}
