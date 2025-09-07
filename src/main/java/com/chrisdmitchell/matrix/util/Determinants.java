package com.chrisdmitchell.matrix.util;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class of methods used in calculating the determinant of the matrix.
 * 
 * @author Chris Mitchell
 */
public final class Determinants {
	
	// ------
	// FIELDS
	// ------
	
	private static final Logger log = LoggerFactory.getLogger(Determinants.class);
	
	// ------------
	// CONSTRUCTORS
	// ------------
	
	private Determinants() {}
	
	// ------------------------
	// DETERMINANT CALCULATIONS
	// ------------------------
	
	/**
	 * Calculates the determinant of a matrix recursively.
	 * <p>
	 * {@code det(A) = Sum(j = 1...n) ((-1)^(1 + j)) * a(1, j) * minor(1, j)}
	 * </p>
	 *
	 * @param matrix		the matrix array for which to find the determinant
	 * @return				the calculated determinant
	 */
	public static double calculateDeterminantRecursively(double[][] source) {

		final int rows = source.length;
		final int columns = source[0].length;

		if (rows == 1) {
			return source[0][0];
		}
		if (rows == 2) {
			return (source[0][0] * source[1][1]) - (source[0][1] * source[1][0]);
		}

		double det = 0.0;
		for (int j = 0; j < columns; j++) {
			double[][] minorMatrix = minorMatrix(source, 0, j);
			det += sign(0, j) * source[0][j] * calculateDeterminantRecursively(minorMatrix);
		}

		return det;

	}
	
	/**
	 * Calculates the cofactor of the {@code source} element at {@code [row, column]}.
	 *
	 * @param row			the row to be removed
	 * @param column		the column to be removed
	 * @return				the cofactor of the matrix element
	 */
	public static double cofactor(double[][] source, int row, int column) {

		double[][] minorMatrix = minorMatrix(source, row, column);
		double minor = calculateDeterminantRecursively(minorMatrix);
		double cofactor = sign(row, column) * minor;
		
		return cofactor;

	}
	
	/**
	 * Generates a matrix array which is the minor matrix of the {@code source} matrix made by
	 * dropping the row and column at {@code [row, column]}.
	 *
	 * @param row			the row to remove
	 * @param column		the column to remove
	 * @return				the minor matrix
	 */
	public static double[][] minorMatrix(double[][] source, int row, int column) {

		LogUtils.logMethodEntry(log);

		final int rows = source.length;
		final int columns = source[0].length;

	    if (row < 0 || row >= rows) {
	        throw new IndexOutOfBoundsException("row " + row + " out of bounds [0..." + (rows - 1) + "]");
	    }
	    if (column < 0 || column >= columns) {
	        throw new IndexOutOfBoundsException("column " + column + " out of bounds [0..." + (columns - 1) + "]");
	    }
	    if (rows <= 1 || columns <= 1) {
	        throw new IllegalArgumentException("Minor is undefined for matrices smaller than 2 x 2.");
	    }

	    double[][] minorMatrix = new double[rows - 1][columns - 1];

	    int newRow = 0;
	    for (int i = 0; i < rows; i++) {
	        if (i == row) {
	        	continue;
	        }
	        int newColumn = 0;
	        for (int j = 0; j < columns; j++) {
	            if (j == column) {
	            	continue;
	            }
	            minorMatrix[newRow][newColumn] = source[i][j];
	            newColumn++;
	        }
	        newRow++;
	    }

	    log.debug("Minor matrix {} calculated from {} at [{}, {}]",
	    		  Arrays.deepToString(minorMatrix), Arrays.deepToString(source), row, column);
	    return minorMatrix;

	}

	/**
	 * Gets the sign of the matrix at a specific location
	 * <p>
	 * <pre>{@code
	 * ┌           ┐
	 * │  +  -  +  │
	 * │  -  +  -  │
	 * │  +  -  +  │
	 * └           ┘
	 * }</pre>
	 * </p>
	 *
	 * @param row			the row of the sign
	 * @param column		the column of the sign
	 * @return				the sign {@code (-1)^(i + j)}
	 */
	private static int sign(int row, int column) {

		return ((row + column) % 2) == 0 ? 1 : -1;

	}


}
