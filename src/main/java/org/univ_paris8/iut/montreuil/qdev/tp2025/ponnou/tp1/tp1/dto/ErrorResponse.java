package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.dto;

import java.util.List;

/**
 * DTO normalise pour les reponses d'erreur JSON.
 * Format : {"error": "TYPE_ERREUR", "messages": ["detail1", "detail2"]}
 */
public class ErrorResponse {

    private String error;
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
