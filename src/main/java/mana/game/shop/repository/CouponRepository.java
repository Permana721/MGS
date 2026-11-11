package mana.game.shop.repository;

import mana.game.shop.entity.Coupon;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface CouponRepository {
    boolean save(Coupon coupon);
    boolean update(Coupon coupon);
    boolean updateIsActiveStatus(String code, boolean is_active);
    boolean delete(String code);
    boolean increaseCouponUsedCount(String code);
    boolean decreaseCouponUsedCount(int id);
    boolean recordCouponUsage(Connection connection, int userId, int couponId);
    boolean checkIsRedeemed(int userId, int couponId);
    boolean refundCoupon(Connection connection, int userId, int couponId);
    Optional<Coupon> findByCode(String code);
    List<Coupon> findAll();
}
