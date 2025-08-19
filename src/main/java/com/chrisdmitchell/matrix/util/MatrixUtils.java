package com.chrisdmitchell.matrix.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chrisdmitchell.matrix.model.Matrix;

import static com.chrisdmitchell.matrix.util.Constants.Formatting.*;

/**
 * A class of utility methods for working with the {@link Matrix} class.
 * 
 * @author Chris Mitchell
 */
public final class MatrixUtils {
	
	private static final Logger log = LoggerFactory.getLogger(MatrixUtils.class);
	
	private MatrixUtils() {}

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
				outputString.append(DECIMAL_FORMAT.format(matrix.getValue(i, j)));
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
		int decimalWidth = 3;
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


	
}
