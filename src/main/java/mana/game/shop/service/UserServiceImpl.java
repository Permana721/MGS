package mana.game.shop.service;

import mana.game.shop.entity.User;
import mana.game.shop.entity.UserRole;
import mana.game.shop.repository.UserRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

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
    public boolean updateUser(User user) {
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
    public boolean addBalance(int id, double balance) {
        User user = findUser(id);
        checkIsActive(user);
        checkBalance(balance);

        return userRepository.topup(id, balance);
    }

    @Override
    public boolean decreaseBalance(Connection connection, int id, double balance) throws SQLException {
        User user = findUser(id);
        checkIsActive(user);
        checkBalance(balance);

        if (user.getBalance() < balance) {
            throw new IllegalArgumentException("Inefficient Balance!");
        }

        return userRepository.deductBalance(connection, id, balance);
    }

    @Override
    public List<User> findAllUser() {
        return userRepository.findAll();
    }

    private void checkBalance(double balance) {
        if (balance <= 0) {
            throw new RuntimeException("The amount must be greater than 0!");
        }
    }

    private void checkIsActive(User user) {
        if (!user.isIs_active()) {
            throw new RuntimeException("User must be active! please contact admin for activate your account.");
        }
    }
}