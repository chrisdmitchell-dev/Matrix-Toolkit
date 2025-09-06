package com.chrisdmitchell.matrix.algo.decompose;

import java.util.Objects;

import com.chrisdmitchell.matrix.model.LUPDecompositionResults;
import com.chrisdmitchell.matrix.util.MatrixUtils;

/**
 * A class for the lu decomposition of a matrix.
 * 
 * @author Chris Mitchell
 */
public class Decompose {

	private Decompose() {}
	
	/**
	 * Decomposes the current matrix to an LU packed matrix.
	 * <p>
	 * The factors satisfy PA = LU, where P is the permutation encoded by {@code pivot}.
	 * </p>
	 * 
	 * @param matrix		the matrix to decompose
	 * @return				a result record containing the LU decomposition
	 * @implNote			Algorithm and refinements provided by ChatGPT (OpenAI).
	 */
	public static LUPDecompositionResults decompose(double[][] matrix) {
		
	    Objects.requireNonNull(matrix, "Matrix must not be null.");
	    
	    final int rows = matrix.length;
	    if (rows == 0) {
	    	throw new IllegalArgumentException("Matrix must have at least one row.");
	    }
	    Objects.requireNonNull(matrix[0], "Matrix must be rectangular (no null rows).");
	    final int columns = matrix[0].length;
	    if (columns == 0) { 
	    	throw new IllegalArgumentException("Matrix must have at least one column.");
	    }
	    for (int i = 1; i < rows; i++) {
	    	Objects.requireNonNull(matrix[i], "Matrix must be rectangular (no null rows).");
	    	if (matrix[i].length != columns) {
	    		throw new IllegalArgumentException("Matrix must be rectangular.");
	    	}
	    }
		
	    final double scale = MatrixUtils.safeScale(matrix);
	    
	    double[][] lu = new double[rows][columns];
	    for (int i = 0; i < rows; i++) {
	        System.arraycopy(matrix[i], 0, lu[i], 0, columns);
	    }
		    
	    int[] pivot = new int[rows];
	    for (int i = 0; i < rows; i++) {
	        pivot[i] = i;
	    }
	    int pivotSign = 1;

	    final int steps = Math.min(rows, columns);
	    int rank = 0;
			
	    for (int j = 0; j < columns; j++) {
	        // Compute j-th column of LU (Doolittle update)
	        for (int i = 0; i < rows; i++) {
	            double sum = lu[i][j];
	            int kmax = Math.min(i, j);
	            for (int k = 0; k < kmax; k++) {
	                sum -= lu[i][k] * lu[k][j];
	            }
	            lu[i][j] = sum;
	        }
				
	        if (j < steps) { // only pivot/scale while we’re within the min(rows, columns) square
	            // Pivot: find row p >= j that maximizes |lu[p][j]|
	            int pivotEntry = j;
	            double max = Math.abs(lu[j][j]);
	            for (int i = j + 1; i < rows; i++) {
	                double value = Math.abs(lu[i][j]);
	                if (value > max) {
	                    max = value;
	                    pivotEntry = i;
	                }
	            }
				
	            // Row swap if needed
	            if (pivotEntry != j) {
	                MatrixUtils.swapRows(lu, pivotEntry, j);
	                MatrixUtils.swapEntries(pivot, pivotEntry, j);
	                pivotSign = -pivotSign;
	            }
				
	            // If pivot nonzero, scale the subdiagonal to form L column j
	            double pivotValue = lu[j][j];
	            if (!MatrixUtils.nearlyZero(pivotValue, scale)) {
	            	rank++;
	                for (int i = j + 1; i < rows; i++) {
	                    lu[i][j] /= pivotValue; // L entries below diag
	                }
	            }
	        }
	    }

	    Double determinant = null;
	    if (rows == columns) {
		    if (rank < rows) {
		    	determinant = 0.0;
		    } else {
		    	double det = (double) pivotSign;
		    	for (int i = 0; i < rows; i++) {
		    		det *= lu[i][i];
		    	}
		    	determinant = det;
		    }
	    }
	    
	    return new LUPDecompositionResults(lu, pivot, pivotSign, determinant, rows, columns);
	}
}
