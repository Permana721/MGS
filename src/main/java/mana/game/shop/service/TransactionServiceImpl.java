package mana.game.shop.service;

import mana.game.shop.entity.Game;
import mana.game.shop.entity.GameType;
import mana.game.shop.entity.Transaction;
import mana.game.shop.entity.User;
import mana.game.shop.repository.GameRepository;
import mana.game.shop.repository.TransactionRepository;
import mana.game.shop.repository.UserRepository;
import mana.game.shop.util.DatabaseUtil;

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
    public boolean buyGame(int userId, int gameId) throws SQLException {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User with Id " + userId + " not found!"));
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game with Id + " + gameId + " not found!"));
        Connection connection = null;

        try {
            connection = DatabaseUtil.getDataSource().getConnection();
            connection.setAutoCommit(false);
            double userBalance = user.getBalance();
            double gamePrice = game.getPrice();

            if (game.getGameType().equals(GameType.PHYSICAL) && game.getStock() <= 0) {
                throw new IllegalArgumentException("Physical copies of the game are out of stock!");
            } else if (game.getGameType().equals(GameType.DIGITAL) && transactionRepository.existsByUserIdAndGameId(userId, gameId)) {
                throw new IllegalStateException("User already owns this digital game!");
            } else if (gamePrice > userBalance) {
                throw new RuntimeException("User balance is insufficient");
            } else {
                boolean deductBalanceResult = userRepository.deductBalance(connection, userId, game.getPrice());
                if (!deductBalanceResult) {
                    throw new RuntimeException("Your balance is low please top up your balance first.");
                }

                if (game.getGameType().equals(GameType.PHYSICAL)) {
                    boolean decreaseStockResult = gameRepository.decreaseStock(connection, gameId, 1);
                    if (!decreaseStockResult) {
                        throw new RuntimeException("The game is out of stock");
                    }
                }

                boolean result = transactionRepository.saveTransaction(connection, userId, gameId, game.getPrice());
                if (!result) {
                    connection.rollback();
                } else {
                    connection.commit();
                }
                return result;
            }
        } catch (Exception exception) {
            if (connection != null) connection.rollback();
            throw new RuntimeException(exception);
        } finally {
            if (connection != null) connection.close();
        }
    }

    @Override
    public List<Transaction> findTransactionById(int id) {
        return transactionRepository.findAllByUserId(id);
    }
}
