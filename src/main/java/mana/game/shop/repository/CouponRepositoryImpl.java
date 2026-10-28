package mana.game.shop.repository;

import mana.game.shop.entity.Coupon;
import mana.game.shop.exception.BusinessValidationException;
import mana.game.shop.exception.DuplicateDataException;
import mana.game.shop.util.DatabaseUtil;
import mana.game.shop.util.DateUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CouponRepositoryImpl implements CouponRepository{
    private final DataSource dataSource;

    public CouponRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean save(Coupon coupon) {
        String sql = """
            INSERT INTO coupons
            (code, discount_percent, max_discount, min_purchase, usage_limit, valid_from, valid_until)
            VALUES
            (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, coupon.getCode());
            preparedStatement.setBigDecimal(2, coupon.getDiscount_percent());
            preparedStatement.setBigDecimal(3, coupon.getMax_discount());
            preparedStatement.setBigDecimal(4, coupon.getMin_purchase());
            preparedStatement.setInt(5, coupon.getUsage_limit());
            preparedStatement.setObject(6, coupon.getValid_from().atOffset(ZoneOffset.UTC));
            preparedStatement.setObject(7, coupon.getValid_until().atOffset(ZoneOffset.UTC));

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException exception) {
            if ("23505".equals(exception.getSQLState())) {
                throw new BusinessValidationException("Coupon code '" + coupon.getCode() + "' already exists!");
            }
            throw new RuntimeException("Failed to save coupon: " + exception.getMessage(), exception);
        }
    }

    @Override
    public boolean update(Coupon coupon) {
        String sql = """
            UPDATE coupons\s
            SET code = ?,\s
            discount_percent = ?,\s
            max_discount = ?,\s
            min_purchase = ?,\s
            usage_limit = ?,\s
            valid_from = ?,\s
            valid_until = ?
            is_active = ?
            updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
           \s""";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, coupon.getCode());
            preparedStatement.setBigDecimal(2, coupon.getDiscount_percent());
            preparedStatement.setBigDecimal(3, coupon.getMax_discount());
            preparedStatement.setBigDecimal(4, coupon.getMin_purchase());
            preparedStatement.setInt(5, coupon.getUsage_limit());
            preparedStatement.setObject(6, coupon.getValid_from().atOffset(ZoneOffset.UTC));
            preparedStatement.setObject(7, coupon.getValid_until().atOffset(ZoneOffset.UTC));
            preparedStatement.setBoolean(8, coupon.isIs_active());
            preparedStatement.setInt(9, coupon.getId());

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to update coupon: " + exception.getMessage(), exception);
        }
    }

    @Override
    public boolean updateIsActiveStatus(String code, boolean is_active) {
        String sql = """
            UPDATE coupons\s
            SET is_active = ?,\s
            updated_at = CURRENT_TIMESTAMP
            WHERE code = ?
           \s""";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setBoolean(1, is_active);
            preparedStatement.setString(2, code);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to update coupon: " + exception.getMessage(), exception);
        }
    }

    @Override
    public boolean delete(String code) {
        String sql = """
            DELETE FROM coupons
            WHERE code = ?
           """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, code);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to delete coupon: " + exception.getMessage(), exception);
        }
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        String sql = """
            SELECT * FROM coupons
            WHERE code = ?
           """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, code);

            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if (resultSet.next()) {
                    return Optional.of(rowHelper(resultSet));
                }
            }
            return Optional.empty();
        } catch (SQLException exception) {
            throw new RuntimeException("An error occurred: " + exception.getMessage(), exception);
        }
    }

    @Override
    public List<Coupon> findAll() {
        List<Coupon> coupons = new ArrayList<>();
        String sql = """
                SELECT * FROM coupons
                ORDER BY id
                """;
        try(Connection connection = dataSource.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while (resultSet.next()) {
                    coupons.add(rowHelper(resultSet));
                }
            } catch (SQLException exception) {
                throw new RuntimeException(exception.getMessage());
            }
            return coupons;
        } catch (SQLException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    private Coupon rowHelper(ResultSet resultSet) {
        try {
            Coupon coupon = new Coupon();
            coupon.setId(resultSet.getInt("id"));
            coupon.setCode(resultSet.getString("code"));
            coupon.setDiscount_percent(resultSet.getBigDecimal("discount_percent"));
            coupon.setMax_discount(resultSet.getBigDecimal("max_discount"));
            coupon.setMin_purchase(resultSet.getBigDecimal("min_purchase"));
            coupon.setUsage_limit(resultSet.getInt("usage_limit"));
            coupon.setUsed_count(resultSet.getInt("used_count"));
            coupon.setValid_from(DatabaseUtil.getInstant(resultSet, "valid_from"));
            coupon.setValid_until(DatabaseUtil.getInstant(resultSet, "valid_until"));
            coupon.setIs_active(resultSet.getBoolean("is_active"));
            coupon.setCreated_at(DatabaseUtil.getInstant(resultSet,"created_at"));
            coupon.setUpdated_at(DatabaseUtil.getInstant(resultSet, "updated_at"));
            return coupon;
        } catch (SQLException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }
}
