package com.quickcommerce.controller;

import java.util.List;

import com.quickcommerce.model.User;
import com.quickcommerce.service.IUserService;

/**
 * UserController — mediates UI user actions and delegates to the user service.
 *
 * GRASP: Controller delegates to the Information Expert (`IUserService`).
 * SOLID: - SRP: coordinates user use-cases only.
 *        - DIP: depends on `IUserService` abstraction instead of a concrete service.
 */
public class UserController {

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    public com.quickcommerce.model.User login(String email, String password) {
        return userService.login(email, password);
    }

    public void registerUser(User u) { userService.registerUser(u); }

    public List<User> getAllUsers() { return userService.getAllUsers(); }

    public void logout(User u) { if (u != null) userService.logout(u.getUserId()); }
}
