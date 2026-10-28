package mana.game.shop.service;

import mana.game.shop.entity.Coupon;
import mana.game.shop.exception.BusinessValidationException;
import mana.game.shop.repository.CouponRepository;

import java.util.List;
import java.util.Objects;

public class CouponServiceImpl implements CouponService{
    private CouponRepository couponRepository;

    public CouponServiceImpl(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Override
    public void addCoupon(Coupon coupon) {
        if (Objects.isNull(coupon)) {
            throw new BusinessValidationException("Please input a valid coupon!");
        }
        couponRepository.save(coupon);
    }

    @Override
    public void updateCoupon(Coupon coupon) {
        if (Objects.isNull(coupon)) {
            throw new BusinessValidationException("Please input a valid coupon!");
        }
        couponRepository.update(coupon);
    }

    @Override
    public void deactivateCoupon(String code) {
        if (code.isBlank()) {
            throw new BusinessValidationException("Please input a valid code!");
        }
    }

    @Override
    public void activateCoupon(String code) {
        if (code.isBlank()) {
            throw new BusinessValidationException("Please input a valid code!");
        }
    }

    @Override
    public void deleteCoupon(String code) {
        if (code.isBlank()) {
            throw new BusinessValidationException("Please input a valid code!");
        }
        couponRepository.delete(code);
    }

    @Override
    public Coupon searchCouponByCode(String code) {
        if (code.isBlank()) {
            throw new BusinessValidationException("Please input a valid code!");
        }
        return couponRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Coupon with code " + code + " not found!"));
    }

    @Override
    public List<Coupon> searchAllCoupon() {
        try {
            return couponRepository.findAll();
        } catch (RuntimeException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }
}
