package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.exception;

/**
 * Exception metier pour signaler qu'une ressource est introuvable.
 * Mappee sur HTTP 404 par le ExceptionMapper correspondant.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
