package com.bajaj.qualifier.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class AnswerRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String regNo;
  private int questionNumber;
  @Lob
  @Column(length = 65535)
  private String finalQuery;
  private Instant createdAt = Instant.now();
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getRegNo() { return regNo; }
  public void setRegNo(String regNo) { this.regNo = regNo; }
  public int getQuestionNumber() { return questionNumber; }
  public void setQuestionNumber(int questionNumber) { this.questionNumber = questionNumber; }
  public String getFinalQuery() { return finalQuery; }
  public void setFinalQuery(String finalQuery) { this.finalQuery = finalQuery; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
