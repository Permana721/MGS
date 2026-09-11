package mana.game.shop.service;

import mana.game.shop.entity.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface UserService {
    User addUser(User user);
    boolean updateUser(User user);
    boolean deleteUser(int id);
    boolean banUser(int adminId, int userId, boolean is_active);
    User findUser(int id);
    User authenticate(String username, String password);
    boolean addBalance(int id, double balance);
    boolean decreaseBalance(Connection connection, int id, double balance) throws SQLException;
    List<User> findAllUser();
}