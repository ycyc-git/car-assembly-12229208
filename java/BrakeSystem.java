public enum BrakeSystem {
    MANDO      (1, "Mando"),
    CONTINENTAL(2, "Continental"),
    BOSCH      (3, "Bosch");

    private final int    index;
    private final String displayName;

    BrakeSystem(int index, String displayName) {
        this.index       = index;
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    public static BrakeSystem fromIndex(int i) {
        for (BrakeSystem b : values()) if (b.index == i) return b;
        return null;
    }
}
