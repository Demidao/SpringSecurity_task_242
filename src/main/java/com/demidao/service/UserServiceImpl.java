package com.demidao.service;

import com.demidao.models.User;
import com.demidao.util.FishData;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @PersistenceContext
    private EntityManager em;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getSingleResult();

        if (user == null) {
            throw new UsernameNotFoundException("No user with email " + email);
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                user.getPassword(), user.getRoles());
    }

    @Override
    public List<User> index() {
        List<User> out = em.createQuery("SELECT u FROM User u", User.class)
                .getResultList();
        if (out.isEmpty()) {
            for (User user : FishData.getInitUserList()) {
                save(user);
            }
            out = FishData.getInitUserList();
        }
        return out;
    }

    @Override
    public User show(long id) {
        return em.find(User.class, id);
    }

    @Override
    public User findByEmail(String email) {
        User user = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getSingleResult();

        if (user == null) {
            throw new UsernameNotFoundException("No user with email " + email);
        }
        return user;
    }

    @Override
    public void save(User user) {
        em.persist(user);
        em.flush();
    }

    @Override
    public void update(User newUser) {
        em.merge(newUser);
        em.flush();
    }

    @Override
    public void delete(long id) {
        User user = show(id);
        if (user == null) {
            throw new NullPointerException("User with id=" + id + " not found");
        }
        em.remove(user);
        em.flush();
    }

    @Override
    public String passEncoder(String password) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
        return passwordEncoder.encode(password);
    }

    @Override
    public boolean checkPasswords(User u1, User u2) {
        return u1.getPassword().equals(u2.getPassword());
    }

}
