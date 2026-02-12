package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.List;
import java.util.stream.Collectors;

public class ExceptionMappers {

    @Provider
    public static class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
        @Override
        public Response toResponse(ConstraintViolationException e) {
            List<String> messages = e.getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.toList());
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse("VALIDATION_ERROR", messages))
                    .build();
        }
    }

    @Provider
    public static class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {
        @Override
        public Response toResponse(ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse("NOT_FOUND", e.getMessage()))
                    .build();
        }
    }

    @Provider
    public static class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {
        @Override
        public Response toResponse(IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse("BAD_REQUEST", e.getMessage()))
                    .build();
        }
    }

    @Provider
    public static class IllegalStateExceptionMapper implements ExceptionMapper<IllegalStateException> {
        @Override
        public Response toResponse(IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse("CONFLICT", e.getMessage()))
                    .build();
        }
    }

    @Provider
    public static class GenericExceptionMapper implements ExceptionMapper<Exception> {
        @Override
        public Response toResponse(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "Erreur interne du serveur"))
                    .build();
        }
    }
}
