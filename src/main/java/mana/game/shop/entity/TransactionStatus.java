package mana.game.shop.entity;

public enum TransactionStatus {
    PENDING("Pending"),
    SUCCESS("Completed"),
    FAILED("Failed"),
    REFUNDED("Refund");

    private String displayName;

    TransactionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
