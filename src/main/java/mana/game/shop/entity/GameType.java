package mana.game.shop.entity;

public enum GameType {
    PHYSICAL("Physical"),
    DIGITAL("Digital");

    private String displayName;

    GameType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
