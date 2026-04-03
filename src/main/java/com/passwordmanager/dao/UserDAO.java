package com.passwordmanager.dao;

import com.passwordmanager.model.User;
import java.util.List;
import java.util.Optional;

public interface UserDAO {
    boolean addUser(User user);
    Optional<User> findByUsername(String username);
    List<User> findAllUsers();
}
