package shared.utils;

import java.util.List;
import java.util.Scanner;

/**
 * Utility class for handling user input
 */
public class InputUtils {
    private static final Scanner scanner = new Scanner(System.in);

    public static  String askForText(String message) {
        System.out.println(message);
        return scanner.nextLine().trim();
    }

    public static  String askForNonBlankText(String message) {
        String input = askForText(message);
        while (input.isBlank()) {
            System.err.println("Input cannot be blank. Please try again.");
            input = askForText(message);
        }
        return input;
    }

    public static  String askForNonBlankText(String message, List<String> options) {
        options = options.stream().map(String::toLowerCase).toList();
        String input = askForText(message);

        while (input.isBlank() || !options.contains(input.toLowerCase())) {
            System.err.println("Invalid input, please select from: " + options.stream().map(String::toLowerCase).toList());
            input = askForText(message);
        }

        return input;
    }

    /**
     * Ask the user for a number value
     *
     * @param message the message to display to the user
     * @return the number value
     */
    public static  int askForNumber(String message) {
        String userInput = askForNonBlankText(message);
        boolean isValid = false;
        int answer = 0;

        while (!isValid) {
            try {
                answer = Integer.parseInt(userInput);
                isValid = true;
            } catch (NumberFormatException nfe) {
                System.err.println(userInput + " is not a valid number value. ");
                userInput = askForNonBlankText(message);
            }
        }

        return answer;

    }

    /**
     * Ask the user for a number value, with an exception message
     *
     * @param message          the message to display to the user
     * @param exceptionMessage the message to display if the user wants to cancel
     * @return the number value
     */
    public static  int askForNumber(String message, String exceptionMessage) {
        String userInput = askForNonBlankText(message);
        boolean isValid = false;
        int answer = 0;

        while (!isValid) {
            try {
                answer = Integer.parseInt(userInput);
                isValid = true;
            } catch (NumberFormatException nfe) {
                if (exceptionMessage.equalsIgnoreCase(userInput)) {
                    return -1;
                } else {
                    System.err.println(userInput + " is not a valid number value.");
                    userInput = askForNonBlankText(message);
                }
            }
        }

        return answer;
    }
}
