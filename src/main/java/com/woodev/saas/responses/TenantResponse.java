package com.woodev.saas.responses;

import com.woodev.saas.entities.TenantStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TenantResponse {

    private String tenantId;

    private String companyName;  //Société Alpha, Beta, etc.

    private String companyCode;

    private String email;

    private String adminFullName;

    private String adminEmail;

    private String adminUsername;

    // ↑ JAMAIS RETOURNER LE PASSWORD !!!
    // Même si c'est un hash BCrypt :
    // → Information sensible
    // → Aide les attaquants
    // → Viole les bonnes pratiques
    //    private String adminPassword;

    private LocalDateTime createAt;

    private TenantStatus status;
}
