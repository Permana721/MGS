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
    Optional<User> bannedUser(boolean is_active, int id);
    boolean delete(int id);
    boolean increaseBalance(Connection connection, int id, BigDecimal balance);
    boolean topup(int id, BigDecimal balance);
    boolean decreaseBalance(Connection connection, User user, BigDecimal balance);
    Optional<User> findById(int id);
    Optional<User> login(String username);
    List<User> findAll(UserRole role);
}
