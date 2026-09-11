package mana.game.shop.entity;

public enum GameCategory {
    ACTION("Action"),
    RPG("RPG"),
    SPORTS("Sports"),
    STRATEGY("Strategy"),
    ADVENTURE("Adventure");

    private String displayName;

    GameCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
