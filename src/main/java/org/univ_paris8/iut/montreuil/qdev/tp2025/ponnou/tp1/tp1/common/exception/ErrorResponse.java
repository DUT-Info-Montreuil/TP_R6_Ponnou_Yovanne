package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Format standard des erreurs API")
public class ErrorResponse {

    @Schema(description = "Code d'erreur applicatif", example = "VALIDATION_ERROR")
    private String error;
    @Schema(description = "Liste de messages d'erreur", example = "[\"title: title is required\"]")
    private List<String> messages;

    public ErrorResponse() {
    }

    public ErrorResponse(String error, List<String> messages) {
        this.error = error;
        this.messages = messages;
    }

    public ErrorResponse(String error, String message) {
        this.error = error;
        this.messages = List.of(message);
    }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public List<String> getMessages() { return messages; }
    public void setMessages(List<String> messages) { this.messages = messages; }
}
