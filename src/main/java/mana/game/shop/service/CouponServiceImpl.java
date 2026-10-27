package mana.game.shop.service;

import mana.game.shop.entity.Coupon;
import mana.game.shop.repository.CouponRepository;

public class CouponServiceImpl implements CouponService{
    private CouponRepository couponRepository;

    public CouponServiceImpl(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Override
    public Coupon addCoupon(Coupon coupon) {
        return couponRepository.save(coupon)
                .orElseThrow(() -> new RuntimeException("Failed to add coupon"));
    }

    @Override
    public Coupon updateCoupon(Coupon coupon) {
        return couponRepository.update(coupon)
                .orElseThrow(() -> new RuntimeException("Failed to update coupon"));
    }

    @Override
    public boolean deleteCoupon(int id) {
        return couponRepository.delete(id);
    }

    @Override
    public Coupon findCouponByCode(String code) {
        return couponRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Coupon with code " + code + " not found!"));
    }
}
