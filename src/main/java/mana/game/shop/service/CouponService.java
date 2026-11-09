package mana.game.shop.service;

import java.util.List;
import mana.game.shop.entity.Coupon;

public interface CouponService {
    void addCoupon(Coupon coupon);
    void updateCoupon(Coupon coupon);
    void deleteCoupon(String code);
    void deactivateCoupon(String code);
    void activateCoupon(String code);
    Coupon searchCouponByCode(String code);
    List<Coupon> searchAllCoupon();
}
