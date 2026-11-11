package mana.game.shop.service;

import mana.game.shop.entity.*;
import mana.game.shop.exception.AuthenticationException;
import mana.game.shop.exception.BusinessValidationException;
import mana.game.shop.exception.ResourceNotFoundException;
import mana.game.shop.repository.CouponRepository;
import mana.game.shop.repository.GameRepository;
import mana.game.shop.repository.TransactionRepository;
import mana.game.shop.repository.UserRepository;
import mana.game.shop.util.ColorUtil;
import mana.game.shop.util.CurrencyUtil;
import mana.game.shop.util.DatabaseUtil;
import mana.game.shop.util.DateUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository, GameRepository gameRepository, UserRepository userRepository, CouponRepository couponRepository) {
        this.transactionRepository = transactionRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.couponRepository = couponRepository;
    }

    @Override
    public Transaction buyGame(int userId, int gameId, String couponCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with Id " + userId + " not found!"));
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game with Id " + gameId + " not found!"));

        Coupon coupon = null;

        if (Objects.nonNull(couponCode)) {
            coupon = couponRepository.findByCode(couponCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Coupon with code " + couponCode + " not found!"));
            boolean redeemed = couponRepository.checkIsRedeemed(userId, coupon.getId());
            if (redeemed) {
                throw new BusinessValidationException("Cannot apply this coupon because this coupon has already use.");
            }
        }

        BigDecimal discountAmount = calculateDiscount(game.getPrice(), coupon);

        if (game.getGameType().equals(GameType.PHYSICAL) && game.getStock() <= 0) {
            throw new BusinessValidationException("Physical copies of the game are out of stock!");
        }

        if (game.getGameType().equals(GameType.DIGITAL) && transactionRepository.existsByUserIdAndGameId(userId, gameId)) {
            throw new BusinessValidationException("User already owns this digital game!");
        }

        if (user.getBalance().compareTo(game.getPrice()) < 0) {
            throw new AuthenticationException("User balance is insufficient");
        }

        if (discountAmount.compareTo(game.getPrice()) > 0) {
            throw new BusinessValidationException("Cannot apply this coupon because amount coupon is more bigger than game price.");
        }

        Connection connection = null;

        try {
            connection = DatabaseUtil.getDataSource().getConnection();
            connection.setAutoCommit(false);

            boolean balanceDeducted = userRepository.decreaseBalance(connection, user, game.getPrice().subtract(discountAmount));

            if (!balanceDeducted) {
                throw new BusinessValidationException("Failed to deduct balance.");
            }

            if (game.getGameType().equals(GameType.PHYSICAL)) {
                gameRepository.decreaseStock(connection, game.getId(), 1);
            }

            Transaction savedTransaction = transactionRepository
                    .save(connection, user.getId(), game.getId(),
                            coupon.getId(), game.getPrice(), discountAmount,
                            game.getPrice().subtract(discountAmount)).orElseThrow(() -> new RuntimeException("Failed to save transaction!"));


            if (Objects.nonNull(coupon)) {
                couponRepository.recordCouponUsage(connection, userId, coupon.getId());
                couponRepository.increaseCouponUsedCount(couponCode);
            }

            connection.commit();

            return savedTransaction;

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

        if (DateUtil.isOlderThanDays(transaction.getCreated_at(), 7)) {
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
            boolean increaseBalance = userRepository.increaseBalance(connection, user.getId(), transaction.getTotalAmount());
            if (!increaseBalance) {
                throw new BusinessValidationException("Failed to increase user balance");
            }
            if (game.getGameType().equals(GameType.PHYSICAL)) {
                try {
                    gameRepository.addStock(connection, transaction.getGameId(), 1);
                } catch (Exception exception) {
                    ColorUtil.printError("Failed for add stock: " + exception.getMessage());
                }
            }
            boolean decreaseCouponUsedCount = couponRepository.decreaseCouponUsedCount(transaction.getCouponId());
            boolean result = transactionRepository.update(connection, transactionId, TransactionStatus.REFUNDED);
            boolean refundCoupon = couponRepository.refundCoupon(connection, userId, transaction.getCouponId());
            if (result && refundCoupon && decreaseCouponUsedCount) {
                connection.commit();
                return true;
            }
        } catch (Exception exception) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (Exception exception1) {
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
        return false;
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
    public List<Transaction> filteredFindTransactionById(int userId) {
        if (userId < 0) throw new BusinessValidationException("Please input a valid id!");
        return transactionRepository.filteredFindAllByUserId(userId);
    }

    @Override
    public List<Transaction> findTransactionById(int userId) {
        if (userId < 0) throw new BusinessValidationException("Please input a valid id!");
        return transactionRepository.findAllByUserId(userId);
    }

    private BigDecimal calculateDiscount (BigDecimal gamePrice, Coupon coupon) {
        if (!coupon.isIs_active()) {
            throw new BusinessValidationException("This coupon is nonactive! please use another coupon.");
        }

        if (DateUtil.isNotStartedYet(coupon.getValid_from())) {
            throw new BusinessValidationException("Cannot use this coupon, because coupon cannot be used yet!");
        }

        if (DateUtil.isExpired(coupon.getValid_until())) {
            throw new BusinessValidationException("Cannot use this coupon, because it has expired.");
        }

        if (coupon.getUsed_count() >= coupon.getUsage_limit()) {
            throw new BusinessValidationException("Cannot use this coupon, because limit count got reached!");
        }

        if (gamePrice.compareTo(coupon.getMin_purchase()) <= 0) {
            throw new BusinessValidationException("Cannot use this coupon, because this coupon has minimal transaction Rp. "
                    + CurrencyUtil.decimalFormat(coupon.getMin_purchase())
                    + " please buy more expensive game for use this coupon.");
        }

        BigDecimal discountAmount = gamePrice.multiply(coupon.getDiscount_percent()
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));

        if (discountAmount.compareTo(coupon.getMax_discount()) < 0) {
            return coupon.getMax_discount();
        }

        return discountAmount;
    }
}
