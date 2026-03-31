package com.quickcommerce.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.quickcommerce.exception.InvalidOperationException;
import com.quickcommerce.exception.ResourceNotFoundException;
import com.quickcommerce.model.Administrator;
import com.quickcommerce.model.Customer;
import com.quickcommerce.model.DeliveryPartner;
import com.quickcommerce.model.User;
import com.quickcommerce.model.Vendor;
import com.quickcommerce.persistence.entity.UserEntity;
import com.quickcommerce.persistence.repo.UserRepository;

/**
 * UserService supports optional repository-backed persistence when
 * a UserRepository is provided. Otherwise it functions in-memory.
 */
/**
 * UserService — domain service for user registration and authentication.
 *
 * GRASP: Information Expert — user-related data and operations are owned by this service.
 * SOLID: - SRP: primary business responsibilities are here; mapping helpers remain but
 *   could be extracted to a mapper to further improve SRP.
 *        - DIP: implements `IUserService` so controllers can depend on the abstraction.
 */
public class UserService implements IUserService {

    private final List<User> registeredUsers;
    private final UserRepository userRepository; // nullable

    public UserService() {
        this.registeredUsers = new ArrayList<>();
        this.userRepository = null;
    }

    public UserService(UserRepository userRepository) {
        this.registeredUsers = new ArrayList<>();
        this.userRepository = userRepository;
        // don't eagerly rehydrate domain objects to avoid complex associations
    }

    // -----------------------------------------------------------------------
    // Registration
    // -----------------------------------------------------------------------

    /**
     * Registers a new user in the system.
     * Throws if the e-mail address is already taken.
     *
     * @param user the user to register
     */
    @Override
    public void registerUser(User user) {
        boolean emailTaken = registeredUsers.stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(user.getEmail()));
        if (emailTaken) {
            throw new InvalidOperationException(
                    "E-mail already registered: " + user.getEmail());
        }
        registeredUsers.add(user);

        if (userRepository != null) {
            try {
                userRepository.save(toEntity(user));
            } catch (Exception ex) {
                System.out.println("[USER-SERVICE] Warning: failed to persist user: " + ex.getMessage());
            }
        }

        System.out.println("[USER-SERVICE] " + user.getRole() + " '"
                + user.getName() + "' registered successfully.");
    }

    // -----------------------------------------------------------------------
    // Authentication
    // -----------------------------------------------------------------------

    /**
     * Attempts to authenticate a user with the given credentials.
     *
     * @param email    the e-mail to authenticate
     * @param password the password to verify
     * @return the authenticated {@link User}
     * @throws ResourceNotFoundException if no user with that e-mail exists
     * @throws InvalidOperationException if the password is incorrect
     */
    @Override
    public User login(String email, String password) {
        User user = findByEmail(email);
        boolean success = user.login(email, password);
        if (!success) {
            throw new InvalidOperationException("Incorrect password for: " + email);
        }
        return user;
    }

    /**
     * Logs the specified user out.
     *
     * @param userId the ID of the user to log out
     */
    @Override
    public void logout(String userId) {
        findById(userId).logout();
    }

    // -----------------------------------------------------------------------
    // Query methods
    // -----------------------------------------------------------------------

    /**
     * Finds a user by their unique ID.
     *
     * @param userId the ID to look up
     * @return the matching {@link User}
     * @throws ResourceNotFoundException if not found
     */
    public User findById(String userId) {
        return registeredUsers.stream()
                .filter(u -> u.getUserId().equals(userId))
                .findFirst()
                .orElseGet(() -> {
                    // attempt DB lookup if repository present
                    if (userRepository != null) {
                        Optional<UserEntity> ue = userRepository.findById(userId);
                        if (ue.isPresent()) {
                            User m = fromEntity(ue.get());
                            registeredUsers.add(m);
                            return m;
                        }
                    }
                    throw new ResourceNotFoundException("User not found with ID: " + userId);
                });
    }

    /**
     * Finds a user by their e-mail address.
     *
     * @param email the e-mail to look up
     * @return the matching {@link User}
     * @throws ResourceNotFoundException if not found
     */
    public User findByEmail(String email) {
        return registeredUsers.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseGet(() -> {
                    if (userRepository != null) {
                        Optional<UserEntity> ue = userRepository.findByEmail(email);
                        if (ue.isPresent()) {
                            User m = fromEntity(ue.get());
                            registeredUsers.add(m);
                            return m;
                        }
                    }
                    throw new ResourceNotFoundException("User not found with email: " + email);
                });
    }

    /**
     * Returns all registered users of a specific role.
     *
     * @param role the role string to filter by (e.g., "Customer", "Vendor")
     * @return list of users with that role
     */
    public List<User> getUsersByRole(String role) {
        List<User> result = new ArrayList<>();
        for (User u : registeredUsers) {
            if (u.getRole().equalsIgnoreCase(role)) result.add(u);
        }
        return Collections.unmodifiableList(result);
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    /** Returns an unmodifiable view of all registered users. */
    @Override
    public List<User> getAllUsers() {
        return Collections.unmodifiableList(registeredUsers);
    }

    // ----------------------
    // Mapping helpers
    // ----------------------
    private User fromEntity(UserEntity e) {
        String role = e.getRole();
        User u;
        switch (role) {
            case "Customer":
                u = new Customer(e.getName(), e.getEmail(), e.getPassword(), e.getDeliveryAddress());
                break;
            case "Vendor":
                u = new Vendor(e.getName(), e.getEmail(), e.getPassword(), e.getStoreName());
                break;
            case "Delivery Partner":
                u = new DeliveryPartner(e.getName(), e.getEmail(), e.getPassword());
                break;
            case "Administrator":
                u = new Administrator(e.getName(), e.getEmail(), e.getPassword(), e.getAdminLevel());
                break;
            default:
                u = new Customer(e.getName(), e.getEmail(), e.getPassword(), e.getDeliveryAddress());
        }
        u.setUserId(e.getUserId());
        return u;
    }

    private UserEntity toEntity(User u) {
        UserEntity e = new UserEntity();
        e.setUserId(u.getUserId());
        e.setName(u.getName());
        e.setEmail(u.getEmail());
        e.setPassword(u.getPassword());
        // store plain password as before (note: not secure)
        try {
            // attempt to access subclass-specific fields
            if (u instanceof Customer) {
                e.setRole("Customer");
                e.setDeliveryAddress(((Customer) u).getDeliveryAddress());
            } else if (u instanceof Vendor) {
                e.setRole("Vendor");
                e.setStoreName(((Vendor) u).getStoreName());
            } else if (u instanceof DeliveryPartner) {
                e.setRole("Delivery Partner");
            } else if (u instanceof Administrator) {
                e.setRole("Administrator");
                e.setAdminLevel(((Administrator) u).getAdminLevel());
            } else {
                e.setRole(u.getRole());
            }
        } catch (Exception ignored) {}
        return e;
    }
}
