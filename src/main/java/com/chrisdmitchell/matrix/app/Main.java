package com.chrisdmitchell.matrix.app;

import static com.chrisdmitchell.matrix.util.Constants.Files.FILE_EXTENSION;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chrisdmitchell.matrix.io.ConsoleUI;
import com.chrisdmitchell.matrix.io.FileIO;
import com.chrisdmitchell.matrix.io.ScreenIO;
import com.chrisdmitchell.matrix.model.Action;
import com.chrisdmitchell.matrix.model.Command;
import com.chrisdmitchell.matrix.model.Matrix;
import com.chrisdmitchell.matrix.util.LogUtils;

/*
 * Javadoc Style Guide
 *
 * 1. General Guidelines
 *    - Start with a short, single-sentence summary of what the class/method/field does.
 *    - Follow with a more detailed explanation in separate paragraphs if needed.
 *    - Use <p> tags to break paragraphs — do NOT rely on blank lines.
 *    - Keep each line ≤ 100 characters for readability.
 *
 * 2. Class-Level Javadoc
 *    - Describe the purpose of the class and its main responsibilities.
 *    - Provide example usage in a {@code ...} block or <pre>{@code ...}</pre>.
 *    - Mention important constraints (e.g., rectangular shape, immutability rules).
 *
 * 3. Method Javadoc
 *    - Always start with an action verb (e.g., "Returns...", "Sets...", "Computes...").
 *    - For parameters:
 *         @param paramName description
 *      — Use lowercase for the description unless it starts with a proper noun.
 *      — Wrap parameter names in {@code ...} when referring to them in prose.
 *    - For return values:
 *         @return description
 *    - For exceptions:
 *         @throws ExceptionType description of when it’s thrown
 *      — Always list all checked exceptions; list unchecked ones if they are likely.
 *
 * 4. Code Formatting
 *    - Use {@code ...} for inline variable names, constants, literals, and short code.
 *    - Use <pre>{@code ...}</pre> for multi-line code examples.
 *    - Avoid tabs in Javadoc text; align descriptions with spaces.
 *
 * 5. Special Notes
 *    - Mention immutability, read-only restrictions, or side effects.
 *    - For methods that copy arrays, state clearly that the returned array is a copy
 *      and will not be affected by external modifications.
 *    - For equality methods, state whether comparisons are exact or approximate.
 *
 * Example Method Javadoc:
 *
 * /**
 *  * Returns a deep copy of the internal matrix values.
 *  *
 *  * <p>The returned array is safe to modify; changes will not affect
 *  * this {@code Matrix} instance.
 *  *
 *  * @return a 2-D {@code double} array containing the matrix values
 *  *\/
 *
 * Example Constructor Javadoc:
 *
 * /**
 *  * Constructs a new {@code Matrix} with the specified dimensions and values.
 *  *
 *  * @param rows the number of rows (must be ≥ 0)
 *  * @param columns the number of columns (must be ≥ 0)
 *  * @param values the initial matrix values (must be rectangular and non-null)
 *  * @param name the name of the matrix
 *  * @param savedToDisk {@code true} if the matrix has been saved to disk
 *  * @throws IllegalArgumentException if {@code rows} or {@code columns} is negative
 *  * @throws NullPointerException if {@code values} or any of its rows is {@code null}
 *  *\/
 */

/*
TODO (Daily Git Workflow for this project)

1. Update your local main branch
   git checkout main
   git pull origin main

2. Create a new branch for your work
   git checkout -b feature/<short-description>
   // Example: git checkout -b feature/matrix-multiply

3. Do your coding
   // Edit files, add methods, run tests

4. Stage and commit changes
   git status               // see changes
   git add .                // stage all changed files
   git commit -m "Implement <feature>"

5. Push your branch to GitHub
   git push -u origin feature/<short-description>

6. Open a Pull Request on GitHub
   // Compare & pull request → write description → merge into main

7. Clean up after merge
   git checkout main
   git pull origin main
   git branch -d feature/<short-description>
   git push origin --delete feature/<short-description>

Notes:
- Keep commits small and messages meaningful.
- Never code directly on main — always use a branch.
- Use git status often to see what’s going on.
- Run git pull before starting new work to stay in sync.
*/

/**
 * The entry point of the Matrix Algebra application.
 * <p>
 * This class initializes and coordinates the execution of the program.
 * It is responsible for reading user input, invoking services, and displaying results.
 * </p>
 *
 * Usage example:
 * <pre>
 *     java -jar matrix-algebra.jar
 * </pre>
 *
 * @author Chris Mitchell
 */
public class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	private static final Map<String, Matrix> matrices = new TreeMap<>();

	/**
	 * Takes two strings and a function and performs that function on the matrices taken
	 * from the {@code Map<String, String> matrices} collection.
	 *
	 * @param matrixNameLeft		a string containing the name of the first matrix
	 * @param matrixNameRight		a string containing the name of the second matrix
	 * @param operation				a {@code BiFunction} function operating on the supplied matrices
	 */
	private static Matrix performMatrixOperation(String matrixNameLeft, String matrixNameRight, BinaryOperator<Matrix> operation) {

		Matrix matrixLeft = matrices.get(matrixNameLeft);
		Matrix matrixRight = matrices.get(matrixNameRight);

		if (matrixLeft == null && matrixRight == null) {
			System.out.printf("The matrices %s and %s do not exist in memory. " +
							  "Type 'print' to obtain a list of matrices in memory.%n",
							  matrixNameLeft, matrixNameRight);
			log.warn("The matrices {} and {} do not exist in memory.", matrixNameLeft, matrixNameRight);
		} else if (matrixLeft == null) {
			System.out.printf("The matrix %s does not exist in memory. " +
					  		  "Type 'print' to obtain a list of matrices in memory.%n",
					  		  matrixNameLeft);
			log.warn("The matrix {} does not exist in memory.", matrixNameLeft);
		} else if (matrixRight == null) {
			System.out.printf("The matrix %s does not exist in memory. " +
			  		  		  "Type 'print' to obtain a list of matrices in memory.%n",
			  		  		  matrixNameRight);
			log.warn("The matrix {} does not exist in memory.", matrixNameRight);
		}

		return operation.apply(matrixLeft, matrixRight);

	}

	private static Matrix performMatrixOperation(String matrixName, UnaryOperator<Matrix> operation) {

		Matrix matrix = matrices.get(matrixName);

		if (matrix == null) {
			System.out.printf("The matrix %s does not exist in memory. " +
							  "Type 'print' to obtain a list of matrices in memory.%n",
							  matrixName);
			log.warn("The matrix {} does not exist in memory.", matrixName);
		}

		return operation.apply(matrix);

	}

	/**
	 * Tests whether {@code string} can be parsed as a finite double or not.
	 *
	 * @param string		The string possibly containing a double value
	 * @return				a Double containing the finite value if successfully parsed,
	 * 						null if the string cannot be parsed
	 */
	private static Double tryParsingDouble(String string) {

		if (string == null) {
			return null;
		}
		try {
			double value = Double.parseDouble(string.strip());
			return Double.isFinite(value) ? value : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Resolves the filename as the last value in an array of strings.
	 * <p>
	 * Appends {@code FILE_EXTENSION} if it's not already there.
	 * </p>
	 *
	 * @param args		an array of strings, the last of which is the filename
	 * @return			the resolved filename with the appropriate extension
	 */
	private static String resolveFilename(String[] args) {

		String baseFilename = (args.length == 1 ? args[0] : args[1]);
		return baseFilename.endsWith(FILE_EXTENSION) ? baseFilename : baseFilename + FILE_EXTENSION;

	}

	/**
	 * Main loop for soliciting and processing commands from the user.
	 *
	 * @param args		unused
	 */
	public static void main(String[] args) {

		System.out.println("Welcome to the Matrix Toolkit!");

		while (true) {
			Command command = ConsoleUI.getUserInput();
		    if (command.action() == null) {
		        System.out.println("Unrecognized command. Type 'help' for a list of available commands.");
		        continue;
		    }
			switch (command.action()) {
				case ADD -> {
					Matrix result = performMatrixOperation(command.args()[0], command.args()[1], Matrix::add);
					ScreenIO.prettyPrintMatrix(result);
				}
				case CLEAR -> {
				    String matrixName = command.args().length > 0 ? command.args()[0] : null;
					if (matrices.containsKey(matrixName)) {
						matrices.remove(matrixName);
						System.out.printf("Cleared matrix %s from memory.%n", matrixName);
						log.info("Cleared matrix {} from memory.", matrixName);
					} else {
						System.out.printf("Matrix %s does not exist in memory.%n", matrixName);
						log.warn("Tried to clear matrix {} from memory which does not exist.", matrixName);
					}
				}
				case COFACTORS -> {
					Matrix result = performMatrixOperation(command.args()[0], Matrix::cofactors);
					ScreenIO.prettyPrintMatrix(result);
				}
				case DETERMINANT -> {
					String matrixName = command.args().length > 0 ? command.args()[0] : null;
					Matrix matrix = matrices.get(matrixName);
					if (matrix != null) {
						double det = matrix.determinant();
						ScreenIO.printScalarValues("Determinant", matrix.getName(), det);
					} else {
						System.out.printf("Matrix %s does not exist in memory.%n", matrixName);
						log.warn("Tried to find the determinant of matrix {} which does not exist in memory.", matrixName);
					}
				}
				case EXIT -> {
					System.out.println("Thank you for using the Matrix Toolkit!");
					log.info("Exiting program.");
					System.exit(0);
				}
				case FROBENIUS	 -> {
					String matrixName = command.args().length > 0 ? command.args()[0] : null;
					Matrix matrix = matrices.get(matrixName);
					if (matrix != null) {
						double frobenius = matrix.frobeniusNorm();
						ScreenIO.printScalarValues("Frobenius Norm", matrix.getName(), frobenius);
					} else {
						System.out.printf("Matrix %s does not exist in memory.%n", matrixName);
						log.warn("Tried to find the determinant of matrix {} which does not exist in memory.", matrixName);
					}
				}
				case HADAMARD -> {
					Matrix result = performMatrixOperation(command.args()[0], command.args()[1], Matrix::hadamard);
					ScreenIO.prettyPrintMatrix(result);
				}
				case HELP -> {
					String textCommand = command.args().length > 0 ? command.args()[0] : null;
					switch (textCommand) {
						case "add" -> Action.ADD.printHelp();
						case "clear" -> Action.CLEAR.printHelp();
						case "cofactors" -> Action.COFACTORS.printHelp();
						case "determinant", "det" -> Action.DETERMINANT.printHelp();
						case "exit", "quit", "q" -> Action.EXIT.printHelp();
						case "frobenius" -> Action.FROBENIUS.printHelp();
						case "infinity" -> Action.INFINITY.printHelp();
						case "input" -> Action.INPUT.printHelp();
						case "inverse" -> Action.INVERSE.printHelp();
						case "l1" -> Action.L1.printHelp();
						case "list" -> Action.LIST.printHelp();
						case "load" -> Action.LOAD.printHelp();
						case "multiply" -> Action.MULTIPLY.printHelp();
						case "print" -> Action.PRINT.printHelp();
						case "ref" -> Action.REF.printHelp();
						case "rref" -> Action.RREF.printHelp();
						case "save" -> Action.SAVE.printHelp();
						case "subtract" -> Action.SUBTRACT.printHelp();
						case "trace" -> Action.TRACE.printHelp();
						case "transpose" -> Action.TRANSPOSE.printHelp();
						default -> Action.HELP.printAllHelp();
					}
				}
				case INFINITY -> {
					String matrixName = command.args().length > 0 ? command.args()[0] : null;
					Matrix matrix = matrices.get(matrixName);
					if (matrix != null) {
						double infinity = matrix.infinityNorm();
						ScreenIO.printScalarValues("Infinity Norm", matrix.getName(), infinity);
					} else {
						System.out.printf("Matrix %s does not exist in memory.%n", matrixName);
						log.warn("Tried to find the determinant of matrix {} which does not exist in memory.", matrixName);
					}
				}
				case INPUT -> {
				    String matrixName = command.args().length > 0 ? command.args()[0] : null;
					if (matrices.containsKey(matrixName)) {
						log.info("User is entering matrix {} which already exists. Overwriting.", matrixName);
					} else {
						log.info("User is entering a new matrix {}.", matrixName);
					}
					Matrix matrix = ConsoleUI.inputMatrix(matrixName);
					matrices.put(matrix.getName(), matrix);
					LogUtils.logMatrix(log, "Loaded into memory", matrix);
				}
				case INVERSE -> {
					Matrix result = performMatrixOperation(command.args()[0], Matrix::inverse);
					ScreenIO.prettyPrintMatrix(result);
				}
				case KRONECKER -> {
					Matrix result = performMatrixOperation(command.args()[0], command.args()[1], Matrix::kronecker);
					ScreenIO.prettyPrintMatrix(result);
				}
				case L1 -> {
					String matrixName = command.args().length > 0 ? command.args()[0] : null;
					Matrix matrix = matrices.get(matrixName);
					if (matrix != null) {
						double l1 = matrix.l1Norm();
						ScreenIO.printScalarValues("L1 Norm", matrix.getName(), l1);
					} else {
						System.out.printf("Matrix %s does not exist in memory.%n", matrixName);
						log.warn("Tried to find the determinant of matrix {} which does not exist in memory.", matrixName);
					}
				}
				case LIST -> {
					Map<String, String> directoryListingWithNames = FileIO.listDirectoryContentsWithNames();
					if (!directoryListingWithNames.isEmpty()) {
						ScreenIO.printDirectoryListingWithNames(directoryListingWithNames);
					} else {
						System.out.println("No files found.");
					}
				}
				case LOAD -> {
				    String matrixName = command.args().length > 0 ? command.args()[0] : null;
					String filename = resolveFilename(command.args());
					if (matrices.containsKey(matrixName)) {
						System.out.printf("Unable to load matrix %s. Matrix already exists in memory.%n", matrixName);
						System.out.println("Use the 'clear' command to remove the existing matrix from memory first.");
						log.warn("Unable to load matrix {}. Matrix already exists in memory.", matrixName);
					} else {
						Matrix matrix = FileIO.loadMatrix(filename);
						if (matrix != null) {
							matrices.put(matrix.getName(), matrix);
							System.out.printf("Loaded matrix %s from %s.%n", matrix.getName(), filename);
						} else {
							System.out.printf("There was an error loading the file %s.%n", filename);
						}
					}
				}
				case LU -> {
					String matrixName = command.args().length > 0 ? command.args()[0] : null;
					Matrix matrix = matrices.get(matrixName);
					if (matrix != null) {
						Matrix l = matrix.lowerTriangular();
						Matrix u = matrix.upperTriangular();
						Matrix permutations = matrix.luPermutations();
						ScreenIO.printLU(matrix, l, u, permutations);
					} else {
						System.out.printf("Matrix %s does not exist in memory.%n", matrixName);
						log.warn("Tried to find the determinant of matrix {} which does not exist in memory.", matrixName);
					}
				}
				case MULTIPLY -> {
					if (matrices.get(command.args()[1]) == null) {
						Matrix source = matrices.get(command.args()[0]);
						Double scalar = tryParsingDouble(command.args()[1]);
						if (scalar != null) {
							System.out.println(source.multiply(scalar.doubleValue()).toPrettyString());
						} else {
							System.out.printf("%s is neither a matrix in memory nor can it be parsed as a double.%n", command.args()[1]);
							log.warn("{} is neither a matrix in memory nor can it be parsed as a double.", command.args()[1]);
						}
					} else {
						Matrix result = performMatrixOperation(command.args()[0], command.args()[1], Matrix::multiply);
						ScreenIO.prettyPrintMatrix(result);
					}
				}
				case PRINT -> {
				    String matrixName = command.args().length > 0 ? command.args()[0] : null;
					if (command.args().length == 0) {
						if (!matrices.isEmpty()) {
							System.out.println("Matrices in memory:");
							ScreenIO.printAllMatrices(matrices);
						} else {
							System.out.println("There are no matrices in memory to print.");
							log.info("User attempted to print matrices when there are none entered.");
						}
					} else {
						if (!matrices.containsKey(matrixName)) {
							System.out.printf("Matrix %s does not exist. Type 'print' to print all matrices in memory.%n", matrixName);
							log.warn("User attempted to print matrix {} which does not exist in memory.", matrixName);
						} else {
							ScreenIO.prettyPrintMatrix(matrices.get(matrixName));
						}
					}
				}
				case REF -> {
					Matrix result = performMatrixOperation(command.args()[0], Matrix::rowEchelonForm);
					ScreenIO.prettyPrintMatrix(result);
				}
				case RREF -> {
					Matrix result = performMatrixOperation(command.args()[0], Matrix::reducedRowEchelonForm);
					ScreenIO.prettyPrintMatrix(result);
				}
				case SAVE -> {
				    String matrixName = command.args().length > 0 ? command.args()[0] : null;
					String filename = resolveFilename(command.args());
					if (!matrices.isEmpty()) {
						if (matrices.containsKey(matrixName)) {
							if (FileIO.saveMatrix(matrices.get(matrixName), filename)) {
								System.out.printf("Matrix %s saved to file %s successfully.%n", matrixName, filename);
							} else {
								log.error("Error trying to save matrix {} to file {}.", matrixName, filename);
							}
						} else {
							System.out.printf("There is no matrix %s in memory to save.%n", matrixName);
							log.warn("User attempted to save matrix {} which does not exist in memory.", matrixName);
						}
					} else {
						System.out.println("There are no matrices in memory to save.");
						log.warn("User attempted to save a matrix when there are none in memory.");
					}
				}
				case SET -> {
					String assignmentName = command.args()[0];
					String commandName = command.args()[1];
					String leftMatrix = command.args()[2];
					String rightMatrix = command.args().length == 3 ? null : command.args()[3];
					Matrix assignmentExists = matrices.get(assignmentName);
					if (assignmentExists == null) {
						Matrix result = null;
						switch (commandName) {
							case "add" -> {
								result = performMatrixOperation(leftMatrix, rightMatrix, Matrix::add);
							}
							case "cofactors" -> {
								result = performMatrixOperation(leftMatrix, Matrix::cofactors);
							}
							case "hadamard" -> {
								result = performMatrixOperation(leftMatrix, rightMatrix, Matrix::hadamard);
							}
							case "inverse" -> {
								result = performMatrixOperation(leftMatrix, Matrix::inverse);
							}
							case "kronecker" -> {
								result = performMatrixOperation(leftMatrix, rightMatrix, Matrix::kronecker);
							}
							case "multiply" -> {
								result = performMatrixOperation(leftMatrix, rightMatrix, Matrix::multiply);
							}
							case "ref" -> {
								result = performMatrixOperation(leftMatrix, Matrix::rowEchelonForm);
							}
							case "rref" -> {
								result = performMatrixOperation(leftMatrix, Matrix::reducedRowEchelonForm);
							}
							case "subtract" -> {
								result = performMatrixOperation(leftMatrix, rightMatrix, Matrix::subtract);
							}
							case "transpose" -> {
								result = performMatrixOperation(leftMatrix, Matrix::transpose);
							}
							default -> {
								Action.SET.printHelp();
								continue;
							}
						}
						if (result != null) {
							result.setName(assignmentName);
							result.setReadOnly(true);
							matrices.put(assignmentName, result);
							ScreenIO.prettyPrintMatrix(result);
						}
					} else {
						System.out.printf("Matrix %s already exists in memory. Type 'clear' to erase from memory.%n",
										  assignmentExists.getName());
					}
				}
				case SUBTRACT -> {
					Matrix result = performMatrixOperation(command.args()[0], command.args()[1], Matrix::subtract);
					ScreenIO.prettyPrintMatrix(result);
				}
				case TRACE -> {
					String matrixName = command.args().length > 0 ? command.args()[0] : null;
					Matrix matrix = matrices.get(matrixName);
					if (matrix != null) {
						double trace = matrix.trace();
						ScreenIO.printScalarValues("trace", matrix.getName(), trace);
					} else {
						System.out.printf("Matrix %s does not exist in memory.%n", matrixName);
						log.warn("Tried to find the determinant of matrix {} which does not exist in memory.", matrixName);
					}
				}
				case TRANSPOSE -> {
					Matrix result = performMatrixOperation(command.args()[0], Matrix::transpose);
					ScreenIO.prettyPrintMatrix(result);
				}
				default -> {
			        System.out.println("Unrecognized command. Type 'help' for a list of available commands.");
				}
			}
		}

	}

}
