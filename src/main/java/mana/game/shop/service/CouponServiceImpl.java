package mana.game.shop.service;

import mana.game.shop.entity.Coupon;
import mana.game.shop.exception.BusinessException;
import mana.game.shop.exception.BusinessValidationException;
import mana.game.shop.exception.ResourceNotFoundException;
import mana.game.shop.repository.CouponRepository;

import java.math.BigDecimal;
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

        if (coupon.getCode().length() <= 3) {
            throw new BusinessValidationException("Please input a valid coupon code!");
        }

        if (coupon.getDiscount_percent() == null || coupon.getDiscount_percent().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("Discount percentage must be greater than 0!");
        }

        if (coupon.getValid_until().isBefore(coupon.getValid_from())) {
            throw new BusinessValidationException("Valid until date must be after valid from date!");
        }

        couponRepository.save(coupon);
    }

    @Override
    public void updateCoupon(Coupon coupon) {
        if (Objects.isNull(coupon)) {
            throw new BusinessValidationException("Please input a valid coupon!");
        }

        if (coupon.getCode().length() <= 3) {
            throw new BusinessValidationException("Please input a valid coupon code!");
        }

        if (coupon.getDiscount_percent() == null || coupon.getDiscount_percent().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("Discount percentage must be greater than 0!");
        }

        if (coupon.getValid_until().isBefore(coupon.getValid_from())) {
            throw new BusinessValidationException("Valid until date must be after valid from date!");
        }

        boolean result = couponRepository.update(coupon);
        if (!result) {
            throw new BusinessException("Failed to update coupon ");
        }
    }

    @Override
    public void deactivateCoupon(String code) {
        if (code.isBlank()) {
            throw new BusinessValidationException("Please input a valid code!");
        }

        if (code.length() <= 3) {
            throw new BusinessValidationException("Code length must greater than 3!");
        }

        Coupon coupon = searchCouponByCode(code);

        if (!coupon.isIs_active()) {
            throw new BusinessValidationException("This coupon is already inactive!");
        }

        boolean result = couponRepository.updateIsActiveStatus(code, false);
        if (!result) {
            throw new BusinessException("Failed to deactivate coupon " + code);
        }
    }

    @Override
    public void activateCoupon(String code) {
        if (code.isBlank()) {
            throw new BusinessValidationException("Please input a valid code!");
        }

        if (code.length() <= 3) {
            throw new BusinessValidationException("Code length must greater than 3!");
        }

        Coupon coupon = searchCouponByCode(code);

        if (coupon.isIs_active()) {
            throw new BusinessValidationException("This coupon is already active!");
        }

        boolean result = couponRepository.updateIsActiveStatus(code, true);
        if (!result) {
            throw new BusinessException("Failed to activate coupon " + code);
        }
    }

    @Override
    public void deleteCoupon(String code) {
        if (code.isBlank()) {
            throw new BusinessValidationException("Please input a valid code!");
        }

        if (code.length() <= 3) {
            throw new BusinessValidationException("Code length must greater than 3!");
        }

        searchCouponByCode(code);

        boolean result = couponRepository.delete(code);
        if (!result) {
            throw new BusinessException("Failed to delete coupon " + code);
        }
    }

    @Override
    public Coupon searchCouponByCode(String code) {
        if (code.isBlank()) {
            throw new BusinessValidationException("Please input a valid code!");
        }

        if (code.length() <= 3) {
            throw new BusinessValidationException("Code length must greater than 3!");
        }

        return couponRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon with code " + code + " not found!"));
    }

    @Override
    public List<Coupon> searchAllCoupon() {
        return couponRepository.findAll();
    }
}
