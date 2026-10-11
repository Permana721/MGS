package mana.game.shop.service;

import mana.game.shop.entity.Game;
import mana.game.shop.entity.GameType;
import mana.game.shop.entity.Transaction;
import mana.game.shop.entity.User;
import mana.game.shop.exception.AuthenticationException;
import mana.game.shop.exception.BusinessException;
import mana.game.shop.exception.BusinessValidationException;
import mana.game.shop.exception.ResourceNotFoundException;
import mana.game.shop.repository.GameRepository;
import mana.game.shop.repository.TransactionRepository;
import mana.game.shop.repository.UserRepository;
import mana.game.shop.util.DatabaseUtil;
import mana.game.shop.util.InputUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository, GameRepository gameRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
    }

    @Override
    public boolean buyGame(int userId, int gameId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with Id " + userId + " not found!"));
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game with Id " + gameId + " not found!"));

        if (game.getGameType().equals(GameType.PHYSICAL) && game.getStock() <= 0) {
            throw new BusinessValidationException("Physical copies of the game are out of stock!");
        }

        if (game.getGameType().equals(GameType.DIGITAL) && transactionRepository.existsByUserIdAndGameId(userId, gameId)) {
            throw new BusinessValidationException("User already owns this digital game!");
        }

        if (user.getBalance().compareTo(game.getPrice()) <= 0) {
            throw new AuthenticationException("User balance is insufficient");
        }

        Connection connection = null;

        try {
            connection = DatabaseUtil.getDataSource().getConnection();
            connection.setAutoCommit(false);

            boolean balanceDeducted = decreaseBalance(connection, user, game.getPrice());
            if (!balanceDeducted) {
                throw new BusinessValidationException("Failed to deduct balance.");
            }

            if (game.getGameType().equals(GameType.PHYSICAL)) {
                boolean stockDecreased = gameRepository.decreaseStock(connection, gameId, 1);
                if (!stockDecreased) {
                    throw new BusinessValidationException("The physical game is out of stock!");
                }
            }

            boolean savedTransaction = transactionRepository.saveTransaction(connection, userId, gameId, game.getPrice());
            if (!savedTransaction) {
                throw new BusinessValidationException("Failed to record transaction log!");
            }

            connection.commit();

            return true;

        } catch (Exception exception) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {

                }
            }
            throw new BusinessValidationException("Transaction failed and rolled back: " + exception.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public boolean addBalance(int id, BigDecimal balance) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("Inefficient Balance! Remaining balance: Rp. " + InputUtil.decimalFormat(user.getBalance()));
        }
        if (!user.isIs_active()) {
            throw new BusinessValidationException("User must be active! please contact admin for activate your account.");
        }
        return transactionRepository.topup(id, balance);
    }

    @Override
    public boolean decreaseBalance(Connection connection, User user, BigDecimal balance) throws SQLException {
        if (balance.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessValidationException("Inefficient Balance!");
        }

        if (!user.isIs_active()) {
            throw new BusinessValidationException("User must be active! please contact admin for activate your account.");
        }

        if (user.getBalance().compareTo(balance) <=  0) {
            throw new BusinessValidationException("Inefficient Balance! Remaining balance: Rp. " + InputUtil.decimalFormat(user.getBalance()));
        }

        return transactionRepository.deductBalance(connection, user, balance);
    }

    @Override
    public List<Transaction> findTransactionById(int userId) {
        return transactionRepository.findAllByUserId(userId);
    }
}
