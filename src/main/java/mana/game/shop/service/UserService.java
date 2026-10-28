package mana.game.shop.service;

import mana.game.shop.entity.User;
import mana.game.shop.entity.UserRole;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UserService {
    void addUser(User user);
    void updateUser(User user);
    void deleteUser(int id);
    void banUser(int adminId, int userId, boolean is_active);
    User findUser(int id);
    User authenticate(String username, String password);
    void addBalance(int id, BigDecimal balance);
    void decreaseBalance(Connection connection, User user, BigDecimal balance) throws SQLException;
    List<User> getAll(UserRole role);
}