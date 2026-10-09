package mana.game.shop.service;

import mana.game.shop.entity.User;
import mana.game.shop.entity.UserRole;
import mana.game.shop.repository.UserRepository;
import mana.game.shop.util.InputUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserRepository userRepository;
    private UserService userService;

    private User createMockUser(int id, String username, UserRole role, BigDecimal balance) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword("password123");
        user.setEmail("test@mail.com");
        user.setBalance(balance);
        user.setUserRole(role);
        user.setIs_active(true);
        return user;
    }

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    @EnabledOnJre(JRE.JAVA_21)
    @DisplayName("Add User")
    void testAddUser() {
        User mockUser = createMockUser(1, "surya", UserRole.CUSTOMER, BigDecimal.valueOf(10_000));

        Mockito.when(userRepository.save(mockUser))
                .thenReturn(mockUser);

        User addedUser = userService.addUser(mockUser);

        Assertions.assertEquals(mockUser.getUsername(), addedUser.getUsername());
        Mockito.verify(userRepository).save(mockUser);
    }

    @Test
    @EnabledOnJre(JRE.JAVA_21)
    @DisplayName("Find By Id")
    void findById() {
        User mockUser = createMockUser(1, "surya", UserRole.CUSTOMER, BigDecimal.valueOf(10_000));
//
//        Mockito.when(userRepository.findById(mockUser.getId()))
//                .thenReturn(Optional.of(mockUser));
//
//        User findUser = userService.findUser(mockUser.getId());
//
//        Assertions.assertEquals(mockUser.getUsername(), findUser.getUsername());
//        Mockito.verify(userRepository).findById(mockUser.getId());
    }

    @Test
    @EnabledOnJre(JRE.JAVA_21)
    @DisplayName("Delete User")
    void testDeleteUser() {
        User mockUser = createMockUser(1, "surya", UserRole.CUSTOMER, BigDecimal.valueOf(10_000));
//
//        Mockito.when(userRepository.findById(mockUser.getId()))
//                .thenReturn(Optional.of(mockUser));
//
//        Mockito.when(userService.deleteUser(mockUser.getId()))
//                .thenReturn(false);
//
//        boolean result = userService.deleteUser(mockUser.getId());
//
//        Assertions.assertFalse(result);
//        Mockito.verify(userRepository).delete(mockUser.getId());
    }

    @Test
    @EnabledOnJre(JRE.JAVA_21)
    @DisplayName("Edit User")
    void testEditUser() {
        User mockUser = createMockUser(1, "surya", UserRole.CUSTOMER, BigDecimal.valueOf(10_000));

        System.out.println(mockUser.getUsername());

//        Mockito.when(userRepository.findById(mockUser.getId()))
//                .thenReturn(Optional.of(mockUser));

//        Mockito.when(userService.updateUser(mockUser))
//                        .thenReturn(false);

        mockUser.setUsername("Arido");
        mockUser.setUserRole(UserRole.ADMIN);

        Optional<User> result = userService.updateUser(mockUser);

//        Assertions.assertFalse(result);
        System.out.println(mockUser.getUsername());
        Mockito.verify(userRepository).update(mockUser);
    }

    @Test
    @EnabledOnJre(JRE.JAVA_21)
    @DisplayName("Add Balance")
    void testAddBalance() {
//        User mockUser = createMockUser(1, "surya", UserRole.CUSTOMER, BigDecimal.valueOf(10_000));
//
//        Mockito.when(userRepository.findById(mockUser.getId()))
//                .thenReturn(Optional.of(mockUser));
//
//        boolean result = userService.addBalance(mockUser.getId(), BigDecimal.valueOf(25_000));
//
//        Assertions.assertFalse(result);
//        Mockito.verify(userRepository).topup(mockUser.getId(), BigDecimal.valueOf(25_000));
    }

    @Test
    @EnabledOnJre(JRE.JAVA_21)
    @DisplayName("Deduct Balance")
    void testDeductBalance() throws SQLException {
        User mockUser = createMockUser(1, "surya", UserRole.CUSTOMER, BigDecimal.valueOf(10_000));
        Connection connection = Mockito.mock(Connection.class);
//
//        Mockito.when(userRepository.findById(mockUser.getId()))
//                .thenReturn(Optional.of(mockUser));

//        boolean result = userService.decreaseBalance(connection, mockUser.getId(), BigDecimal.valueOf(5000));
//
//        assertFalse(result);
//        Mockito.verify(userRepository).deductBalance(connection, mockUser.getId(), BigDecimal.valueOf(5000));
    }

    @Test
    @EnabledOnJre(JRE.JAVA_21)
    @DisplayName("Ban User")
    void testBanUser() {
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
    @EnabledOnJre(JRE.JAVA_21)
    @DisplayName("Login")
    void testLogin() {
        User mockUser = createMockUser(1, "surya", UserRole.CUSTOMER, BigDecimal.valueOf(10_000));

        Mockito.when(userRepository.login(mockUser.getUsername(), mockUser.getPassword()))
                .thenReturn(Optional.of(mockUser));

        User loginUser = userService.authenticate(mockUser.getUsername(), mockUser.getPassword());

        Assertions.assertNotNull(loginUser);
        assertEquals(mockUser.getUsername(), loginUser.getUsername());
        Mockito.verify(userRepository).login(mockUser.getUsername(), mockUser.getPassword());
    }

    @Test
    @EnabledOnJre(JRE.JAVA_21)
    @DisplayName("Find All Users")
    void findAllUsers() {
        List<User> users = null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.of("Asia/Jakarta"));


    }
}
