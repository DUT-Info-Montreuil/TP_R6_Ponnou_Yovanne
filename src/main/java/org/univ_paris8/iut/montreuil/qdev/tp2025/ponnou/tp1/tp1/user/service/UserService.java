package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.service;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config.EntityManagerUtil;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.dao.UserDAO;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public User create(String username, String email, String password) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            if (userDAO.countWithFilters(em, Map.of("username", username)) > 0) {
                throw new IllegalArgumentException("Ce nom d'utilisateur existe déjà");
            }

            if (userDAO.countWithFilters(em, Map.of("email", email)) > 0) {
                throw new IllegalArgumentException("Cet email existe déjà");
            }

            User user = new User(username, email, password);
            User saved = userDAO.save(em, user);
            tx.commit();
            return saved;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public User update(Long id, String username, String email) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            User user = userDAO.findById(em, id)
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

            if (!user.getUsername().equals(username) && userDAO.countWithFilters(em, Map.of("username", username)) > 0) {
                throw new IllegalArgumentException("Ce nom d'utilisateur existe déjà");
            }

            if (!user.getEmail().equals(email) && userDAO.countWithFilters(em, Map.of("email", email)) > 0) {
                throw new IllegalArgumentException("Cet email existe déjà");
            }

            user.setUsername(username);
            user.setEmail(email);

            User updated = userDAO.update(em, user);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void changePassword(Long id, String newPassword) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            User user = userDAO.findById(em, id)
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

            user.setPassword(newPassword);
            userDAO.update(em, user);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<User> authenticate(String username, String password) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return userDAO.findOneWithFilters(em, Map.of("username", username, "password", password));
        } finally {
            em.close();
        }
    }

    public Optional<User> findById(Long id) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return userDAO.findById(em, id);
        } finally {
            em.close();
        }
    }

    public Optional<User> findByUsername(String username) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return userDAO.findOneWithFilters(em, Map.of("username", username));
        } finally {
            em.close();
        }
    }

    public List<User> findAll() {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return userDAO.findWithFilters(em, null, null, null, "createdAt DESC", null);
        } finally {
            em.close();
        }
    }

    public List<User> findAllPaginated(int page, int size) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return userDAO.findWithFilters(em, null, null, null, "createdAt DESC", null, page, size);
        } finally {
            em.close();
        }
    }

    public long count() {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return userDAO.count(em);
        } finally {
            em.close();
        }
    }

    public User patch(Long id, String username, String email, String password) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            User user = userDAO.findById(em, id)
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

            if (username != null) {
                if (!user.getUsername().equals(username) && userDAO.countWithFilters(em, Map.of("username", username)) > 0) {
                    throw new IllegalArgumentException("Ce nom d'utilisateur existe déjà");
                }
                user.setUsername(username);
            }
            if (email != null) {
                if (!user.getEmail().equals(email) && userDAO.countWithFilters(em, Map.of("email", email)) > 0) {
                    throw new IllegalArgumentException("Cet email existe déjà");
                }
                user.setEmail(email);
            }
            if (password != null) {
                user.setPassword(password);
            }

            User updated = userDAO.update(em, user);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (!userDAO.deleteById(em, id)) {
                throw new IllegalArgumentException("Utilisateur non trouvé");
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
