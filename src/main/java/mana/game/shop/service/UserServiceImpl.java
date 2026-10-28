package mana.game.shop.service;

import mana.game.shop.entity.User;
import mana.game.shop.entity.UserRole;
import mana.game.shop.exception.AuthenticationException;
import mana.game.shop.exception.BusinessValidationException;
import mana.game.shop.exception.ResourceNotFoundException;
import mana.game.shop.repository.UserRepository;
import mana.game.shop.util.InputUtil;
import mana.game.shop.util.PasswordUtil;

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
    public void addUser(User user) {
        if (user.getUsername().isBlank() || user.getUsername() == null) {
            throw new BusinessValidationException("Username cannot be empty!");
        }
        if (user.getPassword().length() < 6) {
            throw new BusinessValidationException("Password must be at least 6 characters!");
        }
        String hashedPassword = PasswordUtil.hashPassword(user.getPassword());
        user.setPassword(hashedPassword);
        try {
            userRepository.save(user);
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    @Override
    public void updateUser(User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty!");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty!");
        }

        try {
            userRepository.update(user);
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    @Override
    public void deleteUser(int id) {
        findUser(id);
        try {
            userRepository.delete(id);
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    @Override
    public void banUser(int adminId, int userId, boolean is_active) {
        if (adminId == userId) {
            throw new BusinessValidationException("Cannot ban yourself!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with Id " + userId + " not found!"));

        if (user.getUserRole().equals(UserRole.ADMIN)) {
            throw new BusinessValidationException("Cannot ban admin!");
        }

        try {
            userRepository.bannedUser(is_active, userId);
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    @Override
    public User findUser(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with Id " + id + " not found!"));
    }

    @Override
    public User authenticate(String username, String password) {
        User user = userRepository.login(username)
                .orElseThrow(() -> new AuthenticationException("Username or password is wrong!"));

        if (!PasswordUtil.checkPassword(password, user.getPassword())) {
            throw new AuthenticationException("Username or password is wrong!");
        }

        if (!user.isIs_active()) {
            throw new AuthenticationException("Your account is deactivated. Please contact admin for activate your account!");
        }

        return user;
    }

    @Override
    public void addBalance(int id, BigDecimal balance) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));

        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("Inefficient Balance! Remaining balance: Rp. "
                    + InputUtil.decimalFormat(user.getBalance()));
        }
        if (!user.isIs_active()) {
            throw new BusinessValidationException("User must be active! please contact admin for activate your account.");
        }
        try {
            userRepository.topup(id, balance);
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    @Override
    public void decreaseBalance(Connection connection, User user, BigDecimal balance) {
        if (balance.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessValidationException("Inefficient Balance!");
        }

        if (!user.isIs_active()) {
            throw new BusinessValidationException("User must be active! please contact admin for activate your account.");
        }

        if (user.getBalance().compareTo(balance) <  0) {
            throw new BusinessValidationException("Inefficient Balance! Remaining balance: Rp. " + InputUtil.decimalFormat(user.getBalance()));
        }

        try {
            userRepository.decreaseBalance(connection, user, balance);
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    @Override
    public List<User> getAll(UserRole role) {
        if (role == null) {
            throw new BusinessValidationException("Role cannot be null!");
        }
        return userRepository.findAll(role);
    }
}