package com.woodev.saas.exceptions.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//Inclure dans la réponse que les valeurs ou les objets qui ont une valeur différent de nul
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String code; //code d'erreur'
    private String message; //message d'erreur'
    private String path; //path de l'url
    private List<ValidationError> validationErrors;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValidationError{
        private String field;
        private String code;
        private String message;
    }
}
