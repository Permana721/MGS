package mana.game.shop.entity;

public enum UserRole {
    ADMIN("Admin"),
    CUSTOMER("Customer");

    private String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
