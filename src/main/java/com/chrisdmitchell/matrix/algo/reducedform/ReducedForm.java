package com.chrisdmitchell.matrix.algo.reducedform;

import static com.chrisdmitchell.matrix.util.Constants.Numeric.*;

import java.util.ArrayList;
import java.util.List;

import com.chrisdmitchell.matrix.util.MatrixUtils;

/**
 * A class of reduced form methods used for determining the REF and RREF of an 2D-array.
 * 
 * @author Chris Mitchell
 */
public final class ReducedForm {
	
	private ReducedForm() {}

	/**
	 * Calculates the row echelon form (REF) of the parameter {@code matrix}
	 * and returns the REF as a new {@code double[][]}.
	 *
	 * @param		the matrix from which to calculate the ref
	 * @return		the REF of the matrix
	 * @implNote	Algorithm and refinements provided by ChatGPT (OpenAI).
	 */
	public static double[][] calculateREF(double[][] matrix) {

		final int rows = matrix.length;
		final int columns = matrix[0].length;
		int nextRow = 0;
		double[][] ref = MatrixUtils.copy2D(matrix);

		// Get a good scale value from the matrix
		double scale = MatrixUtils.safeScale(ref);

	    // Walk left to right over the columns placing pivots top-down
		for (int j = 0; j < columns && nextRow < rows; j++) {
			// Choose a pivot row such that the pivot contains the largest value in the column
			int pivotRow = -1;
			double bestAbs = 0.0;
			for (int i = nextRow; i < rows; i++) {
				double valueAbs = Math.abs(ref[i][j]);
				if (valueAbs > bestAbs) {
					bestAbs = valueAbs;
					pivotRow = i;
				}
			}
			// If there is no useable pivot in this column
			if (pivotRow == -1 || MatrixUtils.nearlyZero(bestAbs, scale)) {
				continue;
			}
			// Perform partial pivoting by swapping the pivot row up - this avoids round-off
			// errors with the floating-point arithmetic
			if (pivotRow != nextRow) {
				MatrixUtils.swapRows(ref, pivotRow, nextRow);
			}
			// Eliminate the values below the pivot
			double pivot = ref[nextRow][j];
			for (int i = nextRow + 1; i < rows; i++) {
				// Skip processing the row if the element is very close to 0.0
	            if (MatrixUtils.nearlyZero(ref[i][j], bestAbs)) {
	            	continue;
	            }
	            double factor = ref[i][j] / pivot;
	            for (int k = j; k < columns; k++) {
	                double updated = ref[i][k] - (factor * ref[nextRow][k]);
	                // Clamp to 0.0 if the value of updated is very close to 0.0
	                if (MatrixUtils.nearlyZero(updated, scale)) {
	                	updated = 0.0;
	                }
		            ref[i][k] = updated;
	            }
	        }
			nextRow++;
		}

		return ref;

	}		
		

	/**
	 * Calculates the reduced row echelon form (RREF) of the parameter {@code matrix}
	 * and returns the RREF as a new {@code double[][]}.
	 *
	 * @return		the RREF of the matrix
	 * @implNote	Algorithm and refinements provided by ChatGPT (OpenAI).
	 */
	public static double[][] calculateRREF(double[][] matrix) {

		// Start with a Matrix in row echelon form
		double[][] rref = ReducedForm.calculateREF(matrix);
		final int rows = matrix.length;
		final int columns = matrix[0].length;

		// Get a good scale value from the row echelon form of the matrix
		double scale = MatrixUtils.safeScale(rref);

	    // Build a list of pivots (first non-zero element in each row)
		record Pivot(int row, int column) {};
		List<Pivot> pivots = new ArrayList<>();
		
	    for (int i = 0; i < rows; i++) {
	    	int tempColumn = -1;
	    	for (int j = 0; j < columns; j++) {
	    		if (!MatrixUtils.nearlyZero(rref[i][j], scale)) {
	    			tempColumn = j;
	    			break;
	    		}
	    	}
	    	if (tempColumn != -1) {
	    		pivots.add(new Pivot(i, tempColumn));
	    	}
	    }

	    // Normalize pivot rows, eliminating from bottom to top
	    for (int p = pivots.size() - 1; p >= 0; p--) {
	    	int row = pivots.get(p).row();
	    	int column = pivots.get(p).column();
	    	// Normalize pivot row so pivot = 1
	    	double pivot = rref[row][column];
	    	if (!MatrixUtils.nearlyZero(pivot, scale) && Math.abs(pivot - 1.0) > ABSOLUTE_EPSILON) {
	    		for (int j = column; j < columns; j++) {
	    			double factor = rref[row][j] / pivot;
	    			// Clamp value to 0.0 in case of very small round-off errors
	    			rref[row][j] = MatrixUtils.nearlyZero(factor, scale) ? 0.0 : factor;
	    		}
	    	}
	    	// Clamp value to 1.0 in case of very small round-off errors
	    	rref[row][column] = 1.0;
	    	// Eliminate above so that the elements above the pivot are 0
	    	for (int i = 0; i < row; i++) {
	    		double factor = rref[i][column];
	    		if (MatrixUtils.nearlyZero(factor, scale)) {
	    			continue;
	    		}
	    		for (int j = column; j < columns; j++) {
	    			double updated = rref[i][j] - factor * rref[row][j];
	    			rref[i][j] = MatrixUtils.nearlyZero(updated, scale) ? 0.0 : updated;
	    		}
	    	}
	    }

		return rref;

	}
	
	/**
	 * Calculates the rank of the matrix provided in the {@code double[][] source} parameter.
	 * 
	 * @param source		the array for which the rank is computed
	 * @return				the rank of the matrix
	 */
	public static int calculateRank(double[][] source) {

		int rank = 0;
		double scale = MatrixUtils.safeScale(source);
		
		for (double[] row : source) {
			boolean nonZero = false;
			for (double value : row) {
				if (!MatrixUtils.nearlyZero(value, scale)) {
					nonZero = true;
					break;
				}
			}
			if (nonZero) {
				rank++;
			}
		}
		
		return rank;
		
	}
}
