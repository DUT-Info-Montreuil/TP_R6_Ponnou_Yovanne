package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ExecutionLoggingAspect {

    @Around("execution(* org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1..controller..*(..)) || execution(* org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1..service..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("event=execution class={} method={} durationMs={}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    duration);
        }
    }
}
