public enum CarType {
    SEDAN(1, "Sedan"),
    SUV  (2, "SUV"),
    TRUCK(3, "Truck");

    private final int    index;
    private final String displayName;

    CarType(int index, String displayName) {
        this.index       = index;
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    public static CarType fromIndex(int i) {
        for (CarType t : values()) if (t.index == i) return t;
        return null;
    }
}
