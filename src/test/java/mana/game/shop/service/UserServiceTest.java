package mana.game.shop.service;

import com.zaxxer.hikari.HikariDataSource;
import mana.game.shop.entity.User;
import mana.game.shop.entity.UserRole;
import mana.game.shop.repository.UserRepository;
import mana.game.shop.repository.UserRepositoryImpl;
import mana.game.shop.util.CurrencyUtil;
import mana.game.shop.util.DatabaseUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserServiceTest {
    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void testAddUser() {
        var user1 = userService.addUser(new User("test1", "test123", "test@mail.com", UserRole.ADMIN));
        var user2 = userService.addUser(new User("coba", "coba123", "coba@mail.com", UserRole.ADMIN));

        Assertions.assertNotNull(user1);
        Assertions.assertNotNull(user2);
    }

    @Test
    void getUser() {
        User user = userService.findUser(2);
        System.out.print(user.getUsername());
    }

    @Test
    void testDeleteUser() {
        boolean result = userService.deleteUser(0);
        System.out.print(result);
    }

    @Test
    void testEditUserRole() {
        User user = userService.findUser(5);
        user.setUserRole(UserRole.CUSTOMER);
        boolean result = userService.updateUser(user);
        Assertions.assertEquals(Boolean.TRUE, result);
    }

    @Test
    void testAddSaldo() {
        boolean result = userService.addBalance(1, 500_000);
        User userAddSaldo = userService.findUser(1);
        assertEquals(550_000, userAddSaldo.getBalance());
        System.out.println(userAddSaldo.getBalance());
        System.out.println(result);
    }

    @Test
    void testDeductSaldo() {
//        boolean result = userService.decreaseBalance(3, 25_000);
//        User userAddSaldo = userService.findUser(3);
//        assertEquals(50_000, userAddSaldo.getBalance());
//        System.out.println(userAddSaldo.getBalance());
//        System.out.println(result);
    }

    @Test
    void testBanUser_Success() {
        int adminId = 1;
        int targetId = 2;
        boolean statusToSet = false;

        User dummyUser = new User();
        dummyUser.setId(targetId);
        dummyUser.setUserRole(UserRole.CUSTOMER);

        Mockito.when(userRepository.findById(targetId))
                .thenReturn(Optional.of(dummyUser));

        Mockito.when(userRepository.bannedUser(targetId, statusToSet))
                .thenReturn(true);

        boolean result = userService.banUser(adminId, targetId, statusToSet);

        assertTrue(result);
        Mockito.verify(userRepository).findById(targetId);
        Mockito.verify(userRepository).bannedUser(targetId, statusToSet);
    }

    @Test
    void login() {
        User user = userService.authenticate("Permana", "admin123");
        System.out.println(user.getUsername());
    }

    @Test
    void findAllUser() {
        List<User> users = userService.findAllUser();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.of("Asia/Jakarta"));

        users.stream()
                .sorted(Comparator.comparingInt(User::getId))
                .forEach(user -> {
                    String updatedAt = user.getUpdated_at() != null
                            ? formatter.format(user.getUpdated_at())
                            : "-";
                    String createdAt = user.getCreated_at() != null
                            ? formatter.format(user.getCreated_at())
                            : "-";
                    System.out.println("========================================================");
                    System.out.println("User Id: " + user.getId());
                    System.out.println("Username: " + user.getUsername());
                    System.out.println("Password: " + user.getPassword());
                    System.out.println("Email: " + user.getEmail());
                    System.out.println("Role: " + user.getUserRole());
                    System.out.println("Balance: " + CurrencyUtil.toRupiahNumber(user.getBalance()));
                    if (user.isIs_active()) {
                        System.out.println("Status: Active");
                    } else {
                        System.out.println("Status: Banned");
                    }
                    System.out.println("Updated At: " + updatedAt);
                    System.out.println("Created At: " + createdAt);
                    System.out.println("========================================================\n");
                });
    }
}
