package mana.game.shop.service;

import mana.game.shop.entity.User;
import mana.game.shop.entity.UserRole;
import mana.game.shop.repository.UserRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {
    UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User addUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<User> updateUser(User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty!");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty!");
        }

        return userRepository.update(user);
    }

    @Override
    public boolean deleteUser(int id) {
        findUser(id);
        return userRepository.delete(id);
    }

    @Override
    public boolean banUser(int adminId, int userId, boolean is_active) {
        if (adminId == userId) {
            throw new IllegalArgumentException("Cannot ban yourself!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with Id " + userId + " not found!"));
        if (user.getUserRole().equals(UserRole.ADMIN)) {
            throw new IllegalArgumentException("Cannot ban admin!");
        }
        return userRepository.bannedUser(userId, is_active);
    }

    @Override
    public User findUser(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User with Id " + id + " not found!"));
    }

    @Override
    public User authenticate(String username, String password) {
        return userRepository.login(username, password)
                .orElseThrow(() -> new RuntimeException("Username or password is wrong!"));
    }

    @Override
    public List<User> getAll(UserRole role) {
        return userRepository.findAll(role);
    }

    private void checkBalance(BigDecimal balance) {

    }

    private void checkIsActive(User user) {

    }
}