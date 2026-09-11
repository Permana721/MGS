package mana.game.shop.entity;

public enum TransactionStatus {
    PENDING("Pending"),
    COMPLETED("Completed"),
    REFUND("Refund");

    private String displayName;

    TransactionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
