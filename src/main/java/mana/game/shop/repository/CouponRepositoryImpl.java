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
import java.util.Optional;

public class CouponRepositoryImpl implements CouponRepository{
    private final DataSource dataSource;

    public CouponRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Coupon> save(Coupon coupon) {
        String sql = """
            INSERT INTO coupons
            (code, discount_percent, max_discount, min_purchase, usage_limit, used_count, valid_from, valid_until)
            VALUES
            (?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING *
            """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, coupon.getCode());
            preparedStatement.setBigDecimal(2, coupon.getDiscount_percent());
            preparedStatement.setBigDecimal(3, coupon.getMax_discount());
            preparedStatement.setBigDecimal(4, coupon.getMin_purchase());
            preparedStatement.setInt(5, coupon.getUsage_limit());
            preparedStatement.setInt(6, coupon.getUsed_count());
            preparedStatement.setObject(7, coupon.getValid_from());
            preparedStatement.setObject(8, coupon.getValid_until());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(rowHelper(resultSet));
                }
            }
            return Optional.empty();

        } catch (SQLException exception) {
            if ("23505".equals(exception.getSQLState())) {
                throw new BusinessValidationException("Coupon code '" + coupon.getCode() + "' already exists!");
            }
            throw new RuntimeException("Failed to save coupon: " + exception.getMessage(), exception);
        }
    }

    @Override
    public Optional<Coupon> update(Coupon coupon) {
        return Optional.empty();
    }

    @Override
    public boolean delete(int id) {
        return false;
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return Optional.empty();
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
