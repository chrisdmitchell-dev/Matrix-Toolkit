package com.chrisdmitchell.matrix.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chrisdmitchell.matrix.model.Matrix;

import static com.chrisdmitchell.matrix.util.Constants.Formatting.*;
import static com.chrisdmitchell.matrix.util.Constants.Numeric.*;

import java.util.Arrays;
import java.util.Random;

/**
 * A class of utility methods for working with the {@link Matrix} class.
 * 
 * @author Chris Mitchell
 */
public final class MatrixUtils {
	
	// ------
	// FIELDS
	// ------
	
	private static final Logger log = LoggerFactory.getLogger(MatrixUtils.class);
	
	// ------------
	// CONSTRUCTORS
	// ------------
	
	private MatrixUtils() {}

	// ----------------------
	// TOLERANCES AND SCALING
	// ----------------------
	
	/**
	 * Determines whether {@code value} is close enough to zero to effectively be
	 * zero.
	 * <p>
	 * A scale parameter is specified to determine if the value is close enough to
	 * zero for a particular operation.
	 * </p>
	 *
	 * @param value the value to test
	 * @param scale a scale factor
	 * @return {@code true} if the value is nearly zero, {@code false} otherwise
	 */
	public static boolean nearlyZero(double value, double scale) {

		return Math.abs(value) <= ABSOLUTE_EPSILON + (RELATIVE_EPSILON * scale);

	}
	
	/**
	 * Calculates a scale factor based on the largest absolute value of the elements
	 * of the array.
	 * <p>
	 * If the array is all zeros, return a scale factor of 1.0.
	 * </p>
	 * 
	 * @param source		the source array
	 * @return				the scale value or 1.0 if the maxValue is 0.0
	 */
	public static double safeScale(double[][] source) {
		
		double maxValue = getMaxValue(source);
		return maxValue == 0.0 ? 1.0 : maxValue;

	}

	/**
	 * Finds the largest absolute element in a {@code double[][]} array.
	 *
	 * @return the largest absolute element
	 */
	public static double getMaxValue(double[][] source) {

		return Arrays.stream(source)
					 .flatMapToDouble(Arrays::stream)
					 .map(Math::abs)
					 .max()
					 .orElse(1.0);

	}
	
	/**
	 * Bucketing with width {@code 2 * EPSILON} so values with {@code |a − b| < EPSILON} compare equal;
	 * equality at exactly {@code EPSILON} may differ.
	 * <p>
	 * Normalizes {@code -0.0} to {@code +0.0}. {@code NaN}, {@code +∞}, and {@code −∞} map to sentinel
	 * bucket values.
	 * </p>
	 *
	 * @param value			a double to be quantized
	 * @return				the bucket index
	 */
	public static long bucketIndex(double value) {

		if (Double.isNaN(value)) {
			return NaN_SENTINEL;
		} else if (value == Double.POSITIVE_INFINITY) {
			return POSITIVE_INFINITY_SENTINEL;
		} else if (value == Double.NEGATIVE_INFINITY) {
			return NEGATIVE_INFINITY_SENTINEL;
		} else if (value == 0.0) {
			value = 0.0;
		}
		return Math.round(value / (2 * EPSILON));

	}
	
	// -----------------
	// COPIES AND SLICES
	// -----------------
	
	public static double[][] copy2D(double[][] source) {
		
		LogUtils.logMethodEntry(log);
		
		double[][] copy = new double[source.length][];
		
		for (int i = 0; i < source.length; i++) {
			copy[i] = source[i].clone();
		}
		
		log.debug("Copied {}.", source.toString());
		return copy;
		
	}
	
	// --------
	// SWAPPING
	// --------
	
	public static void swapRows(double[][] source, int rowFrom, int rowTo) {

		double[] temp;

		temp = source[rowTo];
		source[rowTo] = source[rowFrom];
		source[rowFrom] = temp;

	}
	
	public static void swapEntries(int[] source, int from, int to) {
		
		int temp;
		
		temp = source[to];
		source[to] = source[from];
		source[from] = temp;
		
	}
	
	// -----------------
	// STRING FORMATTING
	// -----------------

	/**
	 * Generates a random name for a matrix with length {@code RANDOM_MATRIX_NAME_LENGTH} consisting
	 * of random capital letters.
	 *
	 * @return		the generated name
	 */
	public static String generateRandomMatrixName() {

		Random random = new Random();
		StringBuilder generatedName = new StringBuilder(RANDOM_MATRIX_NAME_LENGTH);

		for (int i = 0; i < RANDOM_MATRIX_NAME_LENGTH; i++) {
			generatedName.append((char) ('A' + random.nextInt(26)));
		}

		return generatedName.toString();

	}
	
	/**
	 * Builds a compact, single line string representation of the matrix.
	 * <p>
	 * Output will resemble:
	 * </p>
	 * <pre>{@code
	 * A=[[1.200,3.400,5.600],[2.300,4.500,6.700],[3.400,5.600,7.800]]
	 * }</pre>
	 * 
	 * @param matrix		the matrix whose values are converted into the formatted string
	 * @return				a compact, single line representation of the matrix suitable for logging
	 */
	public static String toShortString(Matrix matrix) {
		
		LogUtils.logMethodEntry(log);
		
		int rows = matrix.getRows();
		int columns = matrix.getColumns();
		StringBuilder outputString = new StringBuilder();
		
		outputString.append(matrix.getName()).append("=[");
		for (int i = 0; i < rows; i++) {
			outputString.append("[");
			for (int j = 0; j < columns; j++) {
				outputString.append(MATRIX_FORMAT.format(matrix.getValue(i, j)));
				if (j != columns - 1) {
					outputString.append(",");
				}
			}
			outputString.append("]");
			if (i != rows - 1) {
				outputString.append(",");
			}
		}
		outputString.append("]");
		
		log.info("Successfully built a short string for matrix {}.", matrix.getName());
		return outputString.toString();
		
	}
	
	/**
	 * Builds a nicely formatted, pretty printed string representation of the matrix.
	 * <p>Output will resemble:</p>
	 * <pre>{@code
	 *     ┌                          ┐
	 *     │    1.200   3.400   5.600 │
	 * A = │    2.300   4.500   6.700 │
	 *     │    3.400   5.600   7.800 │
	 *     └                          ┘
	 * }</pre>
	 * 
	 * @param matrix		the matrix whose values are converted into the formatted string
	 * @return				a pretty printed string representation of the matrix
	 */
	public static String toPrettyString(Matrix matrix) {
		
		LogUtils.logMethodEntry(log);
		
		int rows = matrix.getRows();
		int columns = matrix.getColumns();
		int wholeWidth = 8;
		int decimalWidth = PRECISION;
		int paddingSpaces = ((wholeWidth + 1) * columns) + 1;
		int centerLine = (rows % 2 == 0) ? (rows / 2) - 1 : (rows / 2);
		String formatParameter = "%" + wholeWidth + "." + decimalWidth + "f ";
		StringBuilder outputString = new StringBuilder();

		String header = String.format("%s = ", matrix.getName());
		int headerIndent = header.length();
		
		outputString.append(" ".repeat(headerIndent)).append("┌").append(" ".repeat(paddingSpaces)).append("┐\n");
		for (int i = 0; i < rows; i++) {
			if (i == centerLine) {
				outputString.append(header);
			} else {
				outputString.append(" ".repeat(headerIndent));
			}
			outputString.append("│ ");
			for (int j = 0; j < columns; j++) {
				outputString.append(String.format(formatParameter, matrix.getValue(i, j)));
			}
			outputString.append("│\n");
		}
		outputString.append(" ".repeat(headerIndent)).append("└").append(" ".repeat(paddingSpaces)).append("┘\n");

		log.info("Successfully built a pretty string for matrix {}.", matrix.getName());
		return outputString.toString();
	}
	
	// -----------------
	// MATRIX GENERATION
	// -----------------
	
	public static Matrix generateIdentity(int rows) {
		
		Matrix identity = new Matrix(rows, rows, "Identity", false);
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				if (i == j) {
					identity.setValue(i, j, 1);
				} else {
					identity.setValue(i, j, 0);
				}
			}
		}
		
		return identity;
		
	}
	
	public static Matrix generateZero(int rows, int columns) {
		
		Matrix zeros = new Matrix(rows, columns, "Zeros", false);
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				zeros.setValue(i, j, 0);
			}
		}
		
		return zeros;
		
	}
	
}
