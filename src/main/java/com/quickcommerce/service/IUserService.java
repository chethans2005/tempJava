package com.quickcommerce.service;

import com.quickcommerce.model.User;

/**
 * Abstraction for user-related operations to allow higher-level modules
 * to depend on an interface (Dependency Inversion).
 */
public interface IUserService {
    User login(String email, String password);
    void registerUser(User u);
    java.util.List<User> getAllUsers();
    void logout(String userId);
}
