package mana.game.shop.entity;

import java.math.BigDecimal;
import java.time.Instant;

public class Coupon {
    private int id;
    private String code;
    private BigDecimal discount_percent;
    private BigDecimal max_discount;
    private BigDecimal min_purchase;
    private int usage_limit;
    private int used_count;
    private Instant valid_from;
    private Instant valid_until;
    private boolean is_active;
    private Instant created_at;
    private Instant updated_at;

    public Coupon(String code, BigDecimal discount_percent, BigDecimal max_discount, BigDecimal min_purchase, int usage_limit, Instant valid_from, Instant valid_until) {
        this.code = code;
        this.discount_percent = discount_percent;
        this.max_discount = max_discount;
        this.min_purchase = min_purchase;
        this.usage_limit = usage_limit;
        this.valid_from = valid_from;
        this.valid_until = valid_until;
    }

    public Coupon() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getDiscount_percent() {
        return discount_percent;
    }

    public void setDiscount_percent(BigDecimal discount_percent) {
        this.discount_percent = discount_percent;
    }

    public BigDecimal getMax_discount() {
        return max_discount;
    }

    public void setMax_discount(BigDecimal max_discount) {
        this.max_discount = max_discount;
    }

    public BigDecimal getMin_purchase() {
        return min_purchase;
    }

    public void setMin_purchase(BigDecimal min_purchase) {
        this.min_purchase = min_purchase;
    }

    public int getUsage_limit() {
        return usage_limit;
    }

    public void setUsage_limit(int usage_limit) {
        this.usage_limit = usage_limit;
    }

    public int getUsed_count() {
        return used_count;
    }

    public void setUsed_count(int used_count) {
        this.used_count = used_count;
    }

    public Instant getValid_from() {
        return valid_from;
    }

    public void setValid_from(Instant valid_from) {
        this.valid_from = valid_from;
    }

    public Instant getValid_until() {
        return valid_until;
    }

    public void setValid_until(Instant valid_until) {
        this.valid_until = valid_until;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    public Instant getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Instant created_at) {
        this.created_at = created_at;
    }

    public Instant getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Instant updated_at) {
        this.updated_at = updated_at;
    }
}
