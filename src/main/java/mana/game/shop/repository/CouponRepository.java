package mana.game.shop.repository;

import mana.game.shop.entity.Coupon;

import java.util.List;
import java.util.Optional;

public interface CouponRepository {
    boolean save(Coupon coupon);
    boolean update(Coupon coupon);
    boolean updateIsActiveStatus(String code, boolean is_active);
    boolean delete(String code);
    Optional<Coupon> findByCode(String code);
    List<Coupon> findAll();
}
