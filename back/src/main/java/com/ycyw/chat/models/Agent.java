package com.ycyw.chat.models;

import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;

@Entity
@Table(name = "agents")
@Data
@Accessors(chain = true)
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(of = { "id" })
@Builder
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
public class Agent {
  @Id
  @GeneratedValue // relies on DB default gen_random_uuid()
  private UUID id;

  @NonNull
  @Size(max = 255)
  private String secret;

  @Column(name = "agency_id")
  private UUID agencyId;

  @Size(max = 50)
  private String role;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "agent_data", columnDefinition = "jsonb")
  private Map<String, Object> agentData;

  public String getName() {
    if (agentData != null && agentData.containsKey("name")) {
      return (String) agentData.get("name");
    }
    return "Unknown Agent";
  }
}
