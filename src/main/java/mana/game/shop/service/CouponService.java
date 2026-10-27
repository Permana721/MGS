package mana.game.shop.service;

import mana.game.shop.entity.Coupon;

public interface CouponService {
    Coupon addCoupon(Coupon coupon);
    Coupon updateCoupon(Coupon coupon);
    boolean deleteCoupon(int id);
    Coupon findCouponByCode(String code);
}
