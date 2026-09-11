package mana.game.shop.repository;

import mana.game.shop.entity.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    boolean update(User user);
    boolean topup(int id, double balance);
    boolean bannedUser(int id, boolean is_active);
    boolean deductBalance(Connection connection, int id, double balance) throws SQLException;
    boolean delete(int id);
    Optional<User> findById(int id);
    Optional<User> login(String username, String password);
    List<User> findAll();
}
