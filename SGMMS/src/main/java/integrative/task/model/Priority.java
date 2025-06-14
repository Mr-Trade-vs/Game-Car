package integrative.task.model;

public enum Priority {
    LOW(0),
    HIGH(1);

    private final int value;

    Priority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
