package com.chrisdmitchell.matrix.algo.decompose;

import java.util.Objects;

import com.chrisdmitchell.matrix.model.LUPDecompositionResults;
import com.chrisdmitchell.matrix.model.QRDecompositionResults;
import com.chrisdmitchell.matrix.util.MatrixUtils;

/**
 * A class for the lu decomposition of a matrix.
 * 
 * @author Chris Mitchell
 */
public class Decompose {

	private Decompose() {}
	
	/**
	 * Performs an LU decomposition with partial pivoting of the given matrix {@code A}.
	 * <p>
	 * For an input matrix {@code A} of size {@code m × n}, this method computes a
	 * decomposition:
	 * </p>
	 * <pre>
	 *     P · A = L · U
	 * </pre>
	 * <ul>
	 *   <li>{@code L} is unit lower-triangular (ones on the diagonal, entries below
	 *       the diagonal may be nonzero).</li>
	 *   <li>{@code U} is upper-triangular (entries below the diagonal are zero).</li>
	 *   <li>{@code P} is a permutation matrix, encoded via the {@code pivot} array
	 *       and {@code pivotSign}, which tracks row swaps performed during partial
	 *       pivoting.</li>
	 * </ul>
	 *
	 * <p>The decomposition is stored in a compact packed form: the strictly lower
	 * part of {@code lu} contains the multipliers that define {@code L}, while the
	 * diagonal and upper part contain {@code U}.</p>
	 *
	 * <p>Additional results:</p>
	 * <ul>
	 *   <li>The rank of the matrix is determined as the number of nonzero pivots.</li>
	 *   <li>The determinant is computed if and only if {@code A} is square, and is
	 *       set to zero if {@code A} is singular (rank-deficient).</li>
	 * </ul>
	 *
	 * @param matrix the input matrix {@code A} (not {@code null}), assumed rectangular
	 * @return an {@code LUPDecompositionResults} containing the packed LU factors,
	 *         pivot array, pivot sign, determinant (if applicable), and matrix dimensions
	 * @throws NullPointerException if {@code matrix} or any row is {@code null}
	 * @throws IllegalArgumentException if {@code matrix} has zero rows or columns,
	 *         or if rows have inconsistent lengths
	 *
	 * @implNote This implementation follows the Doolittle algorithm with partial
	 *           pivoting for numerical stability. It was developed with guidance
	 *           from OpenAI’s ChatGPT (GPT-5 model). Row swaps are tracked via the
	 *           {@code pivot} array and {@code pivotSign}. The determinant is computed
	 *           only for square matrices.
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
	
	/**
	 * Performs a Householder QR decomposition of the given matrix {@code A}.
	 * <p>
	 * For an input matrix {@code A} of size {@code m × n}, this method computes
	 * an orthogonal matrix {@code Q} ({@code m × m}) and an upper-triangular
	 * matrix {@code R} ({@code m × n}) such that:
	 * </p>
	 * <pre>
	 *     A ≈ Q · R
	 * </pre>
	 *
	 * <p>Properties:</p>
	 * <ul>
	 *   <li>{@code Q} is orthogonal: {@code Qᵀ · Q = I} (up to numerical tolerance).</li>
	 *   <li>{@code R} is upper-triangular; entries below the main diagonal are zero.</li>
	 *   <li>When {@code A} is rank-deficient, some diagonal entries of {@code R}
	 *       will be zero (or nearly zero within tolerance).</li>
	 * </ul>
	 *
	 * <p>Usage notes:</p>
	 * <ul>
	 *   <li>{@code Q} and {@code R} are returned as new matrices; the input array
	 *       is not modified.</li>
	 *   <li>For deterministic results, this implementation flips the signs of rows
	 *       of {@code R} and corresponding columns of {@code Q} to ensure that the
	 *       diagonal entries of {@code R} are nonnegative.</li>
	 * </ul>
	 *
	 * @param matrix the input matrix {@code A} (not {@code null}), assumed rectangular
	 * @return a {@code QRDecompositionResults} holding the orthogonal matrix {@code Q}
	 *         and the upper-triangular matrix {@code R}
	 * @throws NullPointerException if {@code matrix} or any row is {@code null}
	 * @throws IllegalArgumentException if {@code matrix} has zero rows or columns,
	 *         or if rows have inconsistent lengths
	 *
	 * @implNote This method was implemented with guidance from OpenAI’s ChatGPT
	 *           (GPT-5 model). Numerical stability is handled via Householder
	 *           reflections; sign canonicalization ensures deterministic {@code R}
	 *           diagonals.
	 */
    public static QRDecompositionResults householderQR(double[][] matrix) {
    	
        Objects.requireNonNull(matrix, "Matrix must not be null.");
        
        final int rows = matrix.length;
        if (rows == 0) {
        	throw new IllegalArgumentException("Matrix must have at least one row");
        }
        
        final int columns = matrix[0].length;
        if (columns == 0) {
        	throw new IllegalArgumentException("Matrix must have at least one column");
        }
        
        for (int i = 1; i < rows; i++) {
            if (matrix[i] == null || matrix[i].length != columns) {
                throw new IllegalArgumentException("A must be rectangular (no null rows, consistent column count)");
            }
        }

        // Set R to be the original matrix
	    double[][] R = new double[rows][columns];
	    for (int i = 0; i < rows; i++) {
	        System.arraycopy(matrix[i], 0, R[i], 0, columns);
	    }

	    // Set Q to be a rows x rows identity matrix
        double[][] Q = identity(rows);

        final int p = Math.min(rows, columns);
        for (int k = 0; k < p; k++) {
            // Build Householder vector v that zeros out R[k+1..rows-1][k]
            int rowsBelow = rows - k; // length of working column segment
            double[] x = new double[rowsBelow];
            for (int i = 0; i < rowsBelow; i++) x[i] = R[k + i][k];

            double xNorm = norm2(x);
            if (xNorm == 0.0) {
                // Column already zero below diagonal; skip
                continue;
            }

            double sign = (x[0] >= 0.0) ? 1.0 : -1.0; // sign(0) = +1
            double alpha = -sign * xNorm;

            // u = x - alpha * e1
            double[] u = x.clone();
            u[0] -= alpha;

            double uNorm = norm2(u);
            if (uNorm == 0.0) {
                // Degenerate: reflector is identity; skip
                continue;
            }

            // v = u / ||u||  (Householder vector)
            double[] v = new double[rowsBelow];
            for (int i = 0; i < rowsBelow; i++) v[i] = u[i] / uNorm;

            // Apply H = I - 2 v v^T to R's trailing block: rows k..rows-1, cols k..columns-1
            for (int j = k; j < columns; j++) {
                double dot = 0.0;
                for (int i = 0; i < rowsBelow; i++) dot += v[i] * R[k + i][j];
                double coeff = 2.0 * dot;
                for (int i = 0; i < rowsBelow; i++) R[k + i][j] -= coeff * v[i];
            }

            // Accumulate into Q on the left: Q := H * Q  (applies to rows k..rows-1 of Q)
            for (int j = 0; j < rows; j++) {
                double dot = 0.0;
                for (int i = 0; i < rowsBelow; i++) dot += v[i] * Q[k + i][j];
                double coeff = 2.0 * dot;
                for (int i = 0; i < rowsBelow; i++) Q[k + i][j] -= coeff * v[i];
            }

            // Nice diagonal value
            R[k][k] = alpha;

            // Clean numerical noise below diagonal
            for (int i = k + 1; i < rows; i++) R[i][k] = 0.0;
        }

        // At this point Q holds H_{p-1} ... H_0  ==  Q^T  (since R = H_{p-1}...H_0 A = Q^T A)
        // To return conventional Q so that A ≈ Q * R, transpose Q now.
        double[][] Qt = transpose(Q);

        // Optional: canonicalize so diag(R) >= 0 (flip row i of R and column i of Q if needed)
        int diagLen = Math.min(rows, columns);
        for (int i = 0; i < diagLen; i++) {
            if (R[i][i] < 0.0) {
                // flip row i of R
                for (int j = 0; j < columns; j++) R[i][j] = -R[i][j];
                // flip column i of Qt (which is Q after transpose)
                for (int r = 0; r < rows; r++) Qt[r][i] = -Qt[r][i];
            }
        }
        return new QRDecompositionResults(Qt, R);
        
    }

    /**
     * Generates an identity matrix as a {@code double[][]} where elements along the diagonal
     * equal 1.0 and the rest equal 0.0.
     * 
     * @param rows			the size of the identity matrix to generate
     * @return				the generated identity matrix
     */
    private static double[][] identity(int rows) {
    	
        double[][] I = new double[rows][rows];
        for (int i = 0; i < rows; i++) {
        	for (int j = 0; j < rows; j++) {
        		if (i == j) {
        			I[i][j] = 1.0;
        		} else {
        			I[i][j] = 0.0;
        		}
        	}
        }
        return I;
        
    }

    /**
     * Calculates the Euclidean norm of a vector {@code double[]}.
     * @param vector
     * @return
     */
    private static double norm2(double[] vector) {
    	
    	double sum = 0.0;
    	for (double element : vector) {
    		sum += element * element;
    	}
    	if (sum == 0.0) {
    		return 0.0;
    	} else {
    		return Math.sqrt(sum);
    	}
    	
    }
    
    /**
     * Calculates the transpose of {@code matrix}.
     * 
     * @param matrix			the matrix to be transposed
     * @return					the transposed matrix
     */
    private static double[][] transpose(double[][] matrix) {
 
    	final int rows = matrix.length;
    	final int columns = matrix[0].length;
    	
    	double[][] T = new double[columns][rows];
    	for (int i = 0; i < rows; i++) {
    		for (int j = 0; j < columns; j++) {
    			T[j][i] = matrix[i][j];
    		}
    	}
        return T;
        
    }
   
}
