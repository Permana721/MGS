package mana.game.shop.repository;

import mana.game.shop.entity.User;
import mana.game.shop.entity.UserRole;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> update(User user);
    boolean topup(int id, BigDecimal balance);
    boolean bannedUser(int id, boolean is_active);
    boolean deductBalance(Connection connection, User user, BigDecimal balance) throws SQLException;
    boolean delete(int id);
    Optional<User> findById(int id);
    Optional<User> login(String username, String password);
    List<User> findAll(UserRole role);
}
