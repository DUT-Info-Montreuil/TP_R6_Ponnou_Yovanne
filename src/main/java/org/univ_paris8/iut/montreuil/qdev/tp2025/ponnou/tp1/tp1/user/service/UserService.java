package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.service;

import lombok.extern.slf4j.Slf4j;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.EntityManagerUtil;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.repository.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User create(String username, String email, String password) {
        log.info("Creating user username={}", username);
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            if (userRepository.countWithFilters(em, Map.of("username", username)) > 0) {
                log.warn("Username already exists username={}", username);
                throw new IllegalArgumentException("Ce nom d'utilisateur existe déjà");
            }

            if (userRepository.countWithFilters(em, Map.of("email", email)) > 0) {
                log.warn("Email already exists email={}", email);
                throw new IllegalArgumentException("Cet email existe déjà");
            }

            User user = new User(username, email, password);
            User saved = userRepository.save(em, user);
            tx.commit();
            log.info("User created id={}", saved.getId());
            return saved;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            if (!(e instanceof IllegalArgumentException)) {
                log.error("Error creating user username={}", username, e);
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public User update(Long id, String username, String email) {
        log.info("Updating user id={}", id);
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            User user = userRepository.findById(em, id)
                    .orElseThrow(() -> {
                        log.warn("User not found id={}", id);
                        return new IllegalArgumentException("Utilisateur non trouvé");
                    });

            if (!user.getUsername().equals(username) && userRepository.countWithFilters(em, Map.of("username", username)) > 0) {
                log.warn("Username already exists username={}", username);
                throw new IllegalArgumentException("Ce nom d'utilisateur existe déjà");
            }

            if (!user.getEmail().equals(email) && userRepository.countWithFilters(em, Map.of("email", email)) > 0) {
                log.warn("Email already exists email={}", email);
                throw new IllegalArgumentException("Cet email existe déjà");
            }

            user.setUsername(username);
            user.setEmail(email);

            User updated = userRepository.update(em, user);
            tx.commit();
            log.info("User updated id={}", id);
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            if (!(e instanceof IllegalArgumentException)) {
                log.error("Error updating user id={}", id, e);
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void changePassword(Long id, String newPassword) {
        log.info("Changing password for user id={}", id);
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            User user = userRepository.findById(em, id)
                    .orElseThrow(() -> {
                        log.warn("User not found id={}", id);
                        return new IllegalArgumentException("Utilisateur non trouvé");
                    });

            user.setPassword(newPassword);
            userRepository.update(em, user);
            tx.commit();
            log.info("Password changed for user id={}", id);
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            if (!(e instanceof IllegalArgumentException)) {
                log.error("Error changing password for user id={}", id, e);
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<User> authenticate(String username, String password) {
        log.debug("Authenticating user username={}", username);
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            Optional<User> user = userRepository.findOneWithFilters(em, Map.of("username", username, "password", password));
            if (user.isPresent()) {
                log.info("Authentication successful username={}", username);
            } else {
                log.warn("Authentication failed username={}", username);
            }
            return user;
        } finally {
            em.close();
        }
    }

    public Optional<User> findById(Long id) {
        log.debug("Fetching user by id={}", id);
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return userRepository.findById(em, id);
        } finally {
            em.close();
        }
    }

    public Optional<User> findByUsername(String username) {
        log.debug("Fetching user by username={}", username);
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return userRepository.findOneWithFilters(em, Map.of("username", username));
        } finally {
            em.close();
        }
    }

    public List<User> findAll() {
        log.debug("Listing all users");
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return userRepository.findWithFilters(em, null, null, null, "createdAt DESC", null);
        } finally {
            em.close();
        }
    }

    public List<User> findAllPaginated(int page, int size) {
        log.debug("Listing users page={} size={}", page, size);
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return userRepository.findWithFilters(em, null, null, null, "createdAt DESC", null, page, size);
        } finally {
            em.close();
        }
    }

    public long count() {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return userRepository.count(em);
        } finally {
            em.close();
        }
    }

    public User patch(Long id, String username, String email, String password) {
        log.info("Patching user id={}", id);
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            User user = userRepository.findById(em, id)
                    .orElseThrow(() -> {
                        log.warn("User not found id={}", id);
                        return new IllegalArgumentException("Utilisateur non trouvé");
                    });

            if (username != null) {
                if (!user.getUsername().equals(username) && userRepository.countWithFilters(em, Map.of("username", username)) > 0) {
                    log.warn("Username already exists username={}", username);
                    throw new IllegalArgumentException("Ce nom d'utilisateur existe déjà");
                }
                user.setUsername(username);
            }
            if (email != null) {
                if (!user.getEmail().equals(email) && userRepository.countWithFilters(em, Map.of("email", email)) > 0) {
                    log.warn("Email already exists email={}", email);
                    throw new IllegalArgumentException("Cet email existe déjà");
                }
                user.setEmail(email);
            }
            if (password != null) {
                user.setPassword(password);
            }

            User updated = userRepository.update(em, user);
            tx.commit();
            log.info("User patched id={}", id);
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            if (!(e instanceof IllegalArgumentException)) {
                log.error("Error patching user id={}", id, e);
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        log.info("Deleting user id={}", id);
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (!userRepository.deleteById(em, id)) {
                log.warn("User not found id={}", id);
                throw new IllegalArgumentException("Utilisateur non trouvé");
            }
            tx.commit();
            log.info("User deleted id={}", id);
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            if (!(e instanceof IllegalArgumentException)) {
                log.error("Error deleting user id={}", id, e);
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
