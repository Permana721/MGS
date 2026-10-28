package mana.game.shop.service;

import mana.game.shop.entity.Coupon;

import java.util.List;

public interface CouponService {
    void addCoupon(Coupon coupon);
    void updateCoupon(Coupon coupon);
    void deleteCoupon(String code);
    void deactivateCoupon(String code);
    void activateCoupon(String code);
    Coupon searchCouponByCode(String code);
    List<Coupon> searchAllCoupon();
}
