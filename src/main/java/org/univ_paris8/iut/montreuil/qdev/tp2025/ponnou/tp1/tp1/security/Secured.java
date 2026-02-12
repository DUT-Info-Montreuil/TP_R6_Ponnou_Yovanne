package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.security;

import javax.ws.rs.NameBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation de marquage pour les endpoints necessitant une authentification.
 * Tout endpoint ou resource annote @Secured sera intercepte par le
 * {@link AuthTokenFilter} qui verifie la presence d'un token valide
 * dans le header Authorization.
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Secured {
}
