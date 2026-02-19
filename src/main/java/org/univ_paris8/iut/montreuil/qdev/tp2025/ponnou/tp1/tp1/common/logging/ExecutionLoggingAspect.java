package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Aspect
@Component
public class ExecutionLoggingAspect {

    @Around("execution(* org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1..service..*(..))")
    public Object logServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        String safeParams = buildSafeParams(signature.getParameterNames(), joinPoint.getArgs());

        log.info("event=enter class={} method={} params=[{}]", className, methodName, safeParams);

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("event=exit class={} method={} durationMs={}", className, methodName, duration);
            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - start;
            log.warn("event=error class={} method={} exception={} message={} durationMs={}",
                    className, methodName, ex.getClass().getSimpleName(), ex.getMessage(), duration);
            throw ex;
        }
    }

    private String buildSafeParams(String[] names, Object[] args) {
        if (names == null || args == null || names.length == 0 || args.length == 0) {
            return "";
        }
        int max = Math.min(names.length, args.length);
        return IntStream.range(0, max)
                .mapToObj(i -> names[i] + "=" + safeValue(names[i], args[i]))
                .collect(Collectors.joining(", "));
    }

    private String safeValue(String paramName, Object value) {
        if (isSecretParam(paramName)) {
            return "***";
        }
        if (value == null) {
            return "null";
        }
        if (value instanceof String s) {
            return "\"" + truncate(s, 50) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean || value instanceof Enum<?>) {
            return String.valueOf(value);
        }
        if (value instanceof Pageable pageable) {
            return "Pageable{page=" + pageable.getPageNumber() + ",size=" + pageable.getPageSize() + ",sort=" + pageable.getSort() + "}";
        }
        if (value.getClass().isArray()) {
            return "<array size=" + Array.getLength(value) + ">";
        }
        if (value instanceof Collection<?> collection) {
            return "<collection size=" + collection.size() + ">";
        }
        Package valuePackage = value.getClass().getPackage();
        if (valuePackage != null && valuePackage.getName().startsWith("org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1")) {
            return "<" + value.getClass().getSimpleName() + ">";
        }
        return "<" + value.getClass().getSimpleName() + ">";
    }

    private boolean isSecretParam(String name) {
        String lowered = name == null ? "" : name.toLowerCase();
        return lowered.contains("password") || lowered.contains("token") || lowered.contains("secret");
    }

    private String truncate(String value, int max) {
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max) + "...";
    }
}
