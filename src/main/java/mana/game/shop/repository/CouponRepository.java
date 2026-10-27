package mana.game.shop.repository;

import mana.game.shop.entity.Coupon;

import java.util.Optional;

public interface CouponRepository {
    Optional<Coupon> save(Coupon coupon);
    Optional<Coupon> update(Coupon coupon);
    boolean delete(int id);
    Optional<Coupon> findByCode(String code);
}
