package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.service;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.utils.EntityManagerUtil;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class UserService {

    public User create(String username, String email, String password) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            if (existsByUsername(em, username)) {
                throw new IllegalArgumentException("Ce nom d'utilisateur existe déjà");
            }

            if (existsByEmail(em, email)) {
                throw new IllegalArgumentException("Cet email existe déjà");
            }

            User user = new User(username, email, password);
            em.persist(user);
            em.getTransaction().commit();
            return user;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public User update(Long id, String username, String email) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            User user = em.find(User.class, id);
            if (user == null) {
                throw new IllegalArgumentException("Utilisateur non trouvé");
            }

            if (!user.getUsername().equals(username) && existsByUsername(em, username)) {
                throw new IllegalArgumentException("Ce nom d'utilisateur existe déjà");
            }

            if (!user.getEmail().equals(email) && existsByEmail(em, email)) {
                throw new IllegalArgumentException("Cet email existe déjà");
            }

            user.setUsername(username);
            user.setEmail(email);

            em.getTransaction().commit();
            return user;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void changePassword(Long id, String newPassword) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            User user = em.find(User.class, id);
            if (user == null) {
                throw new IllegalArgumentException("Utilisateur non trouvé");
            }

            user.setPassword(newPassword);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<User> authenticate(String username, String password) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username AND u.password = :password",
                    User.class);
            query.setParameter("username", username);
            query.setParameter("password", password);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public Optional<User> findById(Long id) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(User.class, id));
        } finally {
            em.close();
        }
    }

    public Optional<User> findByUsername(String username) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public List<User> findAll() {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u ORDER BY u.createdAt DESC", User.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            User user = em.find(User.class, id);
            if (user != null) {
                em.remove(user);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    private boolean existsByUsername(EntityManager em, String username) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class);
        query.setParameter("username", username);
        return query.getSingleResult() > 0;
    }

    private boolean existsByEmail(EntityManager em, String email) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class);
        query.setParameter("email", email);
        return query.getSingleResult() > 0;
    }
}
