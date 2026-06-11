public enum SteeringSystem {
    BOSCH(1, "Bosch"),
    MOBIS(2, "Mobis");

    private final int    index;
    private final String displayName;

    SteeringSystem(int index, String displayName) {
        this.index       = index;
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    public static SteeringSystem fromIndex(int i) {
        for (SteeringSystem s : values()) if (s.index == i) return s;
        return null;
    }
}
