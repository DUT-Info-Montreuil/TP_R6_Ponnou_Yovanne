package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.filter.AuthTokenFilter;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.UUID;

@Provider
@Priority(Priorities.USER)
public class RequestResponseLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String REQUEST_ID_KEY = "request_id";
    private static final String REQUEST_START_NANOS = "requestStartNanos";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String requestId = requestContext.getHeaderString(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        requestContext.setProperty(REQUEST_ID_KEY, requestId);
        requestContext.setProperty(REQUEST_START_NANOS, System.nanoTime());
        MDC.put(REQUEST_ID_KEY, requestId);

        LOGGER.info(
                "event=http_request_start request_id={} method={} path={} query={}",
                requestId,
                requestContext.getMethod(),
                requestContext.getUriInfo().getPath(),
                requestContext.getUriInfo().getRequestUri().getQuery()
        );
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String requestId = (String) requestContext.getProperty(REQUEST_ID_KEY);
        Object startNanosObj = requestContext.getProperty(REQUEST_START_NANOS);
        long durationMs = -1L;
        if (startNanosObj instanceof Long) {
            durationMs = (System.nanoTime() - (Long) startNanosObj) / 1_000_000L;
        }

        Object userId = requestContext.getProperty(AuthTokenFilter.USER_ID_PROPERTY);

        LOGGER.info(
                "event=http_request_end request_id={} method={} path={} status={} duration_ms={} user_id={}",
                requestId,
                requestContext.getMethod(),
                requestContext.getUriInfo().getPath(),
                responseContext.getStatus(),
                durationMs,
                userId == null ? "anonymous" : userId
        );

        if (requestId != null && !requestId.isBlank()) {
            responseContext.getHeaders().putSingle(REQUEST_ID_HEADER, requestId);
        }
        MDC.remove(REQUEST_ID_KEY);
    }
}
