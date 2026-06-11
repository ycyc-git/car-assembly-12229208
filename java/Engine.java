public enum Engine {
    GM    (1, "GM"),
    TOYOTA(2, "TOYOTA"),
    WIA   (3, "WIA"),
    BROKEN(4, "고장난 엔진");

    private final int    index;
    private final String displayName;

    Engine(int index, String displayName) {
        this.index       = index;
        this.displayName = displayName;
    }

    public String  getDisplayName() { return displayName; }
    public boolean isBroken()       { return this == BROKEN; }

    public static Engine fromIndex(int i) {
        for (Engine e : values()) if (e.index == i) return e;
        return null;
    }
}
