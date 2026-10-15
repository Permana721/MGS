package mana.game.shop.service;

import mana.game.shop.entity.User;
import mana.game.shop.entity.UserRole;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User addUser(User user);
    User updateUser(User user);
    boolean deleteUser(int id);
    User banUser(int adminId, int userId, boolean is_active);
    User findUser(int id);
    User authenticate(String username, String password);
    List<User> getAll(UserRole role);
}