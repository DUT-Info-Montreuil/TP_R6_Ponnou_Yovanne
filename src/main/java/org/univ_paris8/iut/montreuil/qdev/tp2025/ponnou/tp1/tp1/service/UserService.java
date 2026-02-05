package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.service;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.dao.UserDAO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.utils.EntityManagerUtil;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public User create(String username, String email, String password) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            if (userDAO.countWithFilters(em, Map.of("username", username)) > 0) {
                throw new IllegalArgumentException("Ce nom d'utilisateur existe déjà");
            }

            if (userDAO.countWithFilters(em, Map.of("email", email)) > 0) {
                throw new IllegalArgumentException("Cet email existe déjà");
            }

            User user = new User(username, email, password);
            em.getTransaction().begin();
            return userDAO.save(em, user);
        } finally {
            em.close();
        }
    }

    public User update(Long id, String username, String email) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
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

            em.getTransaction().begin();
            return userDAO.update(em, user);
        } finally {
            em.close();
        }
    }

    public void changePassword(Long id, String newPassword) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            User user = userDAO.findById(em, id)
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

            user.setPassword(newPassword);
            em.getTransaction().begin();
            userDAO.update(em, user);
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

    public void delete(Long id) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            userDAO.deleteById(em, id);
        } finally {
            em.close();
        }
    }
}
