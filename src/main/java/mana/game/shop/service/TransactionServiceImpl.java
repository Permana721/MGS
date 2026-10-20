package mana.game.shop.service;

import mana.game.shop.entity.*;
import mana.game.shop.exception.AuthenticationException;
import mana.game.shop.exception.BusinessException;
import mana.game.shop.exception.BusinessValidationException;
import mana.game.shop.exception.ResourceNotFoundException;
import mana.game.shop.repository.GameRepository;
import mana.game.shop.repository.TransactionRepository;
import mana.game.shop.repository.UserRepository;
import mana.game.shop.util.DatabaseUtil;
import mana.game.shop.util.DateUtil;
import mana.game.shop.util.InputUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

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

        if (user.getBalance().compareTo(game.getPrice()) < 0) {
            throw new AuthenticationException("User balance is insufficient");
        }

        Connection connection = null;

        try {
            connection = DatabaseUtil.getDataSource().getConnection();
            connection.setAutoCommit(false);

            boolean balanceDeducted = userRepository.decreaseBalance(connection, user, game.getPrice());
            if (!balanceDeducted) {
                throw new BusinessValidationException("Failed to deduct balance.");
            }

            if (game.getGameType().equals(GameType.PHYSICAL)) {
                gameRepository.decreaseStock(connection, gameId, 1);
            }

            boolean savedTransaction = transactionRepository.saveTransaction(connection, userId, gameId, game.getPrice());
            if (!savedTransaction) {
                throw new BusinessValidationException("Failed to record transaction log!");
            }

            connection.commit();

            return true;

        } catch (Exception exception) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage());
            }
            throw new BusinessValidationException(exception.getMessage());
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
    public boolean refundTransaction(int transactionId, int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with Id " + userId + " not found!"));
        Transaction transaction = transactionRepository.findTransaction(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction with Id " + transactionId + " not found!"));
        Game game = gameRepository.findById(transaction.getGameId())
                .orElseThrow(() -> new ResourceNotFoundException("Game with Id " + transaction.getGameId() + " not found!"));

        if (user.getId() != transaction.getUserId()) {
            throw new BusinessValidationException("This transaction isn't yours!");
        }

        if (DateUtil.isOlderThanDays(transaction.getTransactionDate(), 7)) {
            throw new BusinessValidationException("The 7-day refund deadline has passed");
        }

        if (transaction.getStatus().equals(TransactionStatus.REFUNDED)) {
            throw new BusinessValidationException("This transaction has already been refunded.");
        } else if (transaction.getStatus().equals(TransactionStatus.PENDING)) {
            throw new BusinessValidationException("This transaction has not been completed!");
        } else if (transaction.getStatus().equals(TransactionStatus.FAILED)) {
            throw new BusinessValidationException("This transaction failed to process, so you cannot request a refund");
        }

        Connection connection = null;

        try {
            connection = DatabaseUtil.getDataSource().getConnection();
            connection.setAutoCommit(false);
            userRepository.increaseBalance(userId, transaction.getAmount());
            if (game.getGameType().equals(GameType.PHYSICAL)) {
                gameRepository.addStock(connection, transaction.getGameId(), 1);
            }
            transactionRepository.update(connection, userId, TransactionStatus.REFUNDED)
                    .orElseThrow(() -> new RuntimeException("Failed to process the transaction"));
            connection.commit();
            return true;
        } catch (SQLException exception) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException exception1) {
                throw new RuntimeException(exception1.getMessage());
            }
            throw new RuntimeException(exception.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException exception) {
                    throw new RuntimeException(exception.getMessage());
                }
            }
        }
    }

    @Override
    public Transaction findTransaction(int id) {
        if (id < 0) throw new BusinessValidationException("Please input a valid id!");
        return transactionRepository.findTransaction(id)
                .orElseThrow(() -> new RuntimeException("Transaction with id " + id + " not found!"));
    }

    @Override
    public List<Transaction> findTransactionHistory(Instant fromDate, Instant toDate) {
        if (fromDate == null || toDate == null) {
            throw new BusinessValidationException("Both start date and end date are required!");
        }

        if (fromDate.isAfter(toDate)) {
            throw new BusinessValidationException("From date (" + fromDate + ") cannot be later than To date (" + toDate + ")!");
        }

        return transactionRepository.findTransactionsByDate(fromDate, toDate);
    }

    @Override
    public List<Transaction> findTransactionById(int userId) {
        return transactionRepository.findAllByUserId(userId);
    }
}
