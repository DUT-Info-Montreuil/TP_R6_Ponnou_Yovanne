package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.PasswordUtils;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ResourceNotFoundException;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.dto.UserPatchDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.mapper.UserMapper;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.repository.UserRepository;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public User create(String username, String email, String password) {
        log.info("Creating user username={}", username);

        if (userRepository.existsByUsername(username)) {
            log.warn("Username already exists username={}", username);
            throw new IllegalArgumentException("Ce nom d'utilisateur existe deja");
        }

        if (userRepository.existsByEmail(email)) {
            log.warn("Email already exists email={}", email);
            throw new IllegalArgumentException("Cet email existe deja");
        }

        User user = new User(username, email, PasswordUtils.hash(password));
        User saved = userRepository.save(user);
        log.info("User created id={}", saved.getId());
        return saved;
    }

    @Transactional
    public User update(Long id, String username, String email) {
        log.info("Updating user id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found id={}", id);
                    return new ResourceNotFoundException("Utilisateur non trouve");
                });

        if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Ce nom d'utilisateur existe deja");
        }

        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Cet email existe deja");
        }

        user.setUsername(username);
        user.setEmail(email);

        User updated = userRepository.save(user);
        log.info("User updated id={}", id);
        return updated;
    }

    @Transactional
    public User patch(Long id, UserPatchDTO dto) {
        log.info("Patching user id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found id={}", id);
                    return new ResourceNotFoundException("Utilisateur non trouve");
                });

        if (dto.getUsername() != null && !user.getUsername().equals(dto.getUsername())
                && userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Ce nom d'utilisateur existe deja");
        }

        if (dto.getEmail() != null && !user.getEmail().equals(dto.getEmail())
                && userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Cet email existe deja");
        }

        userMapper.updateUserFromPatchDTO(dto, user);
        if (dto.getPassword() != null) {
            user.setPassword(PasswordUtils.hash(dto.getPassword()));
        }

        User updated = userRepository.save(user);
        log.info("User patched id={}", id);
        return updated;
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting user id={}", id);
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utilisateur non trouve");
        }
        userRepository.deleteById(id);
        log.info("User deleted id={}", id);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Page<User> findAllPaginated(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> authenticate(String username, String password) {
        log.debug("Authenticating user username={}", username);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent() && PasswordUtils.matches(password, user.get().getPassword())) {
            log.info("Authentication successful username={}", username);
            return user;
        }
        log.warn("Authentication failed username={}", username);
        return Optional.empty();
    }
}
