package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
