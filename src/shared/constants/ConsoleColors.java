package shared.constants;

/**
 * Enum for console colors
 */
public enum ConsoleColors {
    RESET("\u001B[0m"),
    WHITE("\u001B[37m"),
    GREEN("\u001B[32m"),
    RED("\u001B[31m"),
    YELLOW("\u001B[33m");

    private final String color;

    public String getColor() {
        return color;
    }

    ConsoleColors(String color) {
        this.color = color;
    }
}
