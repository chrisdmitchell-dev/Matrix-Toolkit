package com.chrisdmitchell.matrix.io;

import static com.chrisdmitchell.matrix.util.Constants.UI.PROMPT;

import java.util.Arrays;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chrisdmitchell.matrix.model.Action;
import com.chrisdmitchell.matrix.model.Command;
import com.chrisdmitchell.matrix.model.Matrix;
import com.chrisdmitchell.matrix.util.LogUtils;

/**
 * A set of methods to be used to collect user input. Any work that requires getting input
 * from the keyboard will be included here.
 *
 * @author Chris Mitchell
 */
public class ConsoleUI {

	private static final Scanner scanner = new Scanner(System.in);

	private static final Logger log = LoggerFactory.getLogger(ConsoleUI.class);

	/**
	 * Reads a line from the default scanner object and returns it stripped.
	 *
	 * @return		The stripped input string
	 */
	private static String readLineStrip() {

		return scanner.nextLine().strip();

	}

	/**
	 * Reads a natural number from the default scanner object and returns it as an integer type
	 *
	 * @param prompt	The string with which to prompt the user for input
	 * @return			The integer parsed from the user input.
	 */
    public static int readNaturalNumber(String prompt) {

        while (true) {
            System.out.print(prompt + " ");
            String input = readLineStrip();
            try {
                int value = Integer.parseInt(input);
                if (value < 1) {
                    System.out.println("Value must be greater than 0.");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input: " + e.getMessage());
                System.out.println("Please enter an integer greater than 0.");
            }
        }
    }

    /**
     * Gets user input from a command line.
     * <p>
     * The user is prompted for input using the {@code PROMPT} variable.
     * Then the user can issue various commands to the interface. This interface
     * processes and validates user commands of various types and equates those
     * strings to an appropriate value in the enum {@code Action}.
     * </p>
     *
     * @return		Returns a new Command object to the caller.
     */
	public static Command getUserInput() {

		LogUtils.logMethodEntry(log);

		System.out.print(PROMPT);
		String input = readLineStrip();
		if (input.isEmpty()) {
			return new Command(null, null);
		} else {
			log.debug("User gave the following command at the command prompt: {}", input);
			return parseCommand(input);
		}
	}
	
	public static boolean getYorN(String message, String defaultResponse) {
		
		LogUtils.logMethodEntry(log);
		
		String defaultString = null;
		defaultResponse = defaultResponse.toLowerCase().substring(0, 1);
		if (defaultResponse.equals("y")) {
			defaultString = "([y]/n)";
		} else {
			defaultResponse = "n";
			defaultString = "(y/[n])";
		}

		while (true) {
			System.out.print(message + " " + defaultString + "? ");
			String input = readLineStrip();
			if (input.isEmpty()) {
				input = defaultResponse;
			}
			if (input.equals("y") || input.equals("n")) {
				if (input.equals("y")) {
					return true;
				} else {
					return false;
				}
			}
		}
		
	}

	/**
	 * Creates and returns a matrix entered by the user from the keyboard.
	 * <p>
	 * The user is prompted for each row of values, which are specified using
	 * spaces as separators. The method then calls {@code Matrix.setValue()}
	 * for each double specified in the input.
	 * </p>
	 *
	 * @param name		the variable name of the matrix
	 * @return			the Matrix object built from user input
	 */
	public static Matrix inputMatrix(String name) {

		LogUtils.logMethodEntry(log);

		Matrix matrix;

		int rows = readNaturalNumber("Enter the number of matrix rows as an integer greater than 0:");
		int columns = readNaturalNumber("Enter the number of matrix columns as an integer greater than 0:");
		matrix = new Matrix(rows, columns, name, false);
		log.info("Creating matrix '{}' with dimensions {}x{}", name, rows, columns);

		System.out.print("Please enter the matrix values one row at a time.\n" +
						 "Separate values with spaces. Values may be decimals.\n");
		for (int i = 0; i < rows; i++) {

			while (true) {

				System.out.print("Row " + (i + 1) + ": ");
				String[] input = readLineStrip().split("\\s+");
	            if (input.length != columns) {
	                System.out.println("You must enter exactly " + columns + " values per line.");
	                log.warn("User entered {} values for an expected {} columns.", input.length, columns);
	                continue;
	            }
	            log.debug("Reading row {}: raw input = {}", i + 1, Arrays.toString(input));

	            double[] rowValues = new double[columns];
				boolean valid = true;
	            for (int j = 0; j < columns; j++) {
	                try {
	                    rowValues[j] = Double.parseDouble(input[j]);
	                } catch (NumberFormatException e) {
	                    System.out.println("Invalid number '" + input[j] + "' provided. Please reenter the row.");
	                    log.warn("Double.parseDouble failed converting '{}' in {}.", input[j], LogUtils.methodWithLocationAtDepth(4));
	                    valid = false;
	                    break;
	                }
	            }
	            if (valid) {
	            	log.debug("Successfully parsed double values from row {}.", i + 1);
	                for (int j = 0; j < columns; j++) {
	                    matrix.setValue(i, j, rowValues[j]);
	                }
	                break;
	            }

			}

		}

		LogUtils.logMatrix(log, "User entered matrix", matrix);
		return matrix;

	}

	/**
	 * Parses and validates user input.
	 * <p>
	 * Given a string containing the solicited user input, the method goes on to associate
	 * raw input with a {@code Command} object for processing in the main loop. The method
	 * calls {@code validate()} to ensure that the given input contains the expected number
	 * of arguments.
	 * </p>
	 *
	 * @param input			raw input as a string
	 * @return				a {@code Command} object containing the processed input
	 */
	private static Command parseCommand(String input) {

		LogUtils.logMethodEntry(log);

		String[] args = input.split("\\s+");
		String[] trimmedArgs = Arrays.copyOfRange(args, 1, args.length);
		String command = args[0].toLowerCase();

		log.debug("Parsing full command {} with args {}.", command, Arrays.toString(trimmedArgs));

		return switch(command) {
			case "add" -> validate(Action.ADD, trimmedArgs, Action.ADD.getNumberOfArguments());
			case "clear" -> validate(Action.CLEAR, trimmedArgs, Action.CLEAR.getNumberOfArguments());
			case "cofactors" -> validate(Action.COFACTORS, trimmedArgs, Action.COFACTORS.getNumberOfArguments());
			case "determinant", "det" -> validate(Action.DETERMINANT, trimmedArgs, Action.DETERMINANT.getNumberOfArguments());
			case "exit", "quit", "q" -> validate(Action.EXIT, trimmedArgs, Action.EXIT.getNumberOfArguments());
			case "frobenius" -> validate(Action.FROBENIUS, trimmedArgs, Action.FROBENIUS.getNumberOfArguments());
			case "hadamard" -> validate(Action.HADAMARD, trimmedArgs, Action.HADAMARD.getNumberOfArguments());
			case "help", "h", "?" -> validate(Action.HELP, trimmedArgs, Action.HELP.getNumberOfArguments());
			case "infinity" -> validate(Action.INFINITY, trimmedArgs, Action.INFINITY.getNumberOfArguments());
			case "input" -> validate(Action.INPUT, trimmedArgs, Action.INPUT.getNumberOfArguments());
			case "inverse" -> validate(Action.INVERSE, trimmedArgs, Action.INVERSE.getNumberOfArguments());
			case "kronecker" -> validate(Action.KRONECKER, trimmedArgs, Action.KRONECKER.getNumberOfArguments());
			case "l1" -> validate(Action.L1, trimmedArgs, Action.L1.getNumberOfArguments());
			case "list" -> validate(Action.LIST, trimmedArgs, Action.LIST.getNumberOfArguments());
			case "load" -> {
				String[] newArgs = normalizeLoadAndSaveInput(trimmedArgs, "from");
				if (newArgs == null) {
					Action.LOAD.printHelp();
					yield new Command(null, null);
				}
				yield validate(Action.LOAD, newArgs, Action.LOAD.getNumberOfArguments());
			}
			case "lu" -> validate(Action.LU, trimmedArgs, Action.LU.getNumberOfArguments());
			case "multiply" -> validate(Action.MULTIPLY, trimmedArgs, Action.MULTIPLY.getNumberOfArguments());
			case "print" -> validate(Action.PRINT, trimmedArgs, Action.PRINT.getNumberOfArguments());
			case "ref" -> validate(Action.REF, trimmedArgs, Action.REF.getNumberOfArguments());
			case "rref" -> validate(Action.RREF, trimmedArgs, Action.RREF.getNumberOfArguments());
			case "save" -> {
				String[] newArgs = normalizeLoadAndSaveInput(trimmedArgs, "to");
				if (newArgs == null) {
					Action.SAVE.printHelp();
					yield new Command(null, null);
				}
				yield validate(Action.SAVE, newArgs, Action.SAVE.getNumberOfArguments());
			}
			case "set" -> {
				String[] newArgs = null;
				if (trimmedArgs[1].equals("to")) {
					if (trimmedArgs.length == 5) {
						newArgs = new String[] { trimmedArgs[0], trimmedArgs[2], trimmedArgs[3], trimmedArgs[4] };
					} else if (trimmedArgs.length == 4) {
						newArgs = new String[] { trimmedArgs[0], trimmedArgs[2], trimmedArgs[3] };
					}
				} else if (trimmedArgs.length == 4 || trimmedArgs.length == 3) {
					newArgs = trimmedArgs;
				}
				yield validate(Action.SET, newArgs, Action.SET.getNumberOfArguments());
			}
			case "subtract" -> validate(Action.SUBTRACT, trimmedArgs, Action.SUBTRACT.getNumberOfArguments());
			case "trace" -> validate(Action.TRACE, trimmedArgs, Action.TRACE.getNumberOfArguments());
			case "transpose" -> validate(Action.TRANSPOSE, trimmedArgs, Action.TRANSPOSE.getNumberOfArguments());
			default -> new Command(null, null);
		};
	}

	/**
	 * Checks whether the {@code load} or {@code save} commands contains an expected
	 * keyword.
	 * <p>
	 * The {@code load} and {@code save} commands take input as:
	 * <pre>{@code
	 * load MATRIX_NAME from FILENAME
	 * save MATRIX_NAME to FILENAME
	 * }</pre>
	 * But it can also take commands as:
	 * <pre>{@code
	 * load MATRIX_NAME FILENAME
	 * save MATRIX_NAME FILENAME
	 * }</pre>
	 * </p>
	 * <p>
	 * This method goes on to check which style of the commands has been given and adjusts
	 * the string array of command arguments depending on which style command is given.
	 * </p>
	 *
	 * @param args			the command parsed into separate strings
	 * @param keyword		the given keyword to check for
	 * @return				the string array adjusted to no longer include the keyword
	 */
	private static String[] normalizeLoadAndSaveInput(String[] args, String keyword) {

		if (args.length == 3) {
			if (!keyword.equalsIgnoreCase(args[1])) {
				return null;
			}
			return new String[] { args[0], args[2] };
		}
	    if (args.length == 2) {
	        if (keyword.equalsIgnoreCase(args[1])) {
	            return null;
	        }
	        return args;
	    }

	    return (args.length == 1) ? args : null;

	}

	/**
	 * Validates that the number of arguments for a given command given by the user matches
	 * with what is expected for that particular command.
	 * <p>
	 * If the number of arguments given is not valid for the particular command, a new
	 * {@code Command} object is returned containing null values.
	 * </p>
	 *
	 * @param action			the command given as an {@code Action} object
	 * @param args				the command parsed into separate strings
	 * @param validLengths		the varargs parameter containing additional arguments to the
	 * 							give command
	 * @return					a {@code Command} object containing the processed command
	 */
	private static Command validate(Action action, String[] args, int... validLengths) {

		LogUtils.logMethodEntry(log);

		for (int len : validLengths) {
			if (args.length == len) {
				log.info("Successfully validated that {} was called with {} arguments.", action, args.length);
				return new Command(action, args);
	        }
	    }
		action.printHelp();
		log.info("Printing help for action {}.", action);
		return new Command(null, null);
	}
	
}
