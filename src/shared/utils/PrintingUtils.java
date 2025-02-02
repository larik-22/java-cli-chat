package shared.utils;

import shared.constants.ConsoleColors;

/**
 * PrintingUtils is a utility class that provides methods for printing messages to the console.
 */
public class PrintingUtils {
    public static synchronized void printMessage(String message, ConsoleColors color) {
        System.out.println(color.getColor() + message + ConsoleColors.RESET.getColor());
    }
    public static synchronized void printMessage(String message) {
        System.out.println(ConsoleColors.WHITE.getColor() + message + ConsoleColors.RESET.getColor());
    }
}
