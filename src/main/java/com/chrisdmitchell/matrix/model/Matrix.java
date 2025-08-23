package com.chrisdmitchell.matrix.model;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.chrisdmitchell.matrix.algo.reducedform.ReducedForm;
import com.chrisdmitchell.matrix.util.Determinants;
import com.chrisdmitchell.matrix.util.LogUtils;
import com.chrisdmitchell.matrix.util.MatrixUtils;

/*
TODO (Matrix Project — next ML-focused operations)

See BACKLOG.md
*/

/* TODO (Matrix cleanup + maintainability roadmap)

See BACKLOG.md
 */

/**
 * Represents a two-dimensional numerical matrix and provides basic matrix algebra operations.
 * <p>
 * This class supports matrix creation, element access, addition, subtraction, multiplication,
 * transposition, and other linear algebra operations. Matrices are internally stored as a
 * two-dimensional array of doubles. The class is mutable by default but provides a {@code readonly}
 * flag to prohibit changes.
 * </p>
 * <p>
 * The class contains {@code getMatrix()} and {@code setMatrix} methods which allow the user to
 * read or write the matrix elements. Both methods use defensive copies to prevent external changes
 * to the matrix elements.
 * </p>
 * <p>
 * This class assumes matrices are rectangular (i.e., each row has the same number of columns).
 * Operations that require shape compatibility will throw an {@link IllegalArgumentException}
 * if dimensions are incompatible.
 * </p>
 * <p>
 * All element indices are zero-based.
 * </p>
 *
 * @author Chris Mitchell
 */
public final class Matrix {
	
	// ------
	// FIELDS
	// ------

	private int rows, columns;
	private String name;
	private boolean savedToDisk, readonly;

	/**
	 * Set to {@code @JsonIgnore} so that Jackson uses {@code getMatrix()} and {@code setMatrix()}
	 * to get/set the {@code double[][] matrix}.
	 */
	@JsonIgnore
	private double[][] matrix;

	/**
	 * Records and declarations for result caching.
	 */
	@JsonIgnore
	private int modCount = 0;
	
	private static record REFResult(Matrix ref, int rank, int version) {}
	private static record RREFResult(Matrix rref, int rank, int version) {}
	private static record LUResult(Matrix L, Matrix U, int[] pivots, int sign, double determinant, int version) {}
	private static record QRResult(Matrix Q, Matrix R, int version) {}
	
	private static final class CachedResults {
		REFResult ref = null;
		RREFResult rref = null;
		LUResult lu = null;
		QRResult qr = null;
	}
	
	private static final Logger log = LoggerFactory.getLogger(Matrix.class);
	
	@JsonIgnore
	private final CachedResults cache = new CachedResults();

	// ------------
	// CONSTRUCTORS
	// ------------
	
	/**
	 * Default constructor required by Jackson.
	 */
	public Matrix() {}

	/**
	 * Constructor used to create a matrix with a randomly generated name containing the
	 * elements provided in the {@code source} parameter.
	 * <p>
	 * Values are defensively copied from {@code source} in {@code setMatrix()}.
	 * </p>
	 *
	 * @param source	a 2D array of doubles containing the elements of the matrix
	 */
	public Matrix(double[][] source) {

		setMatrix(source);
		this.name = MatrixUtils.generateRandomMatrixName();

	}

	/**
	 * Constructor used to create a matrix with the specified name containiong the elements
	 * provided in the {@code source} parameter.
	 *
	 * @param source	a 2D array of doubles containing the elements of the matrix
	 * @param name		the name of the matrix
	 */
	public Matrix(double[][] source, String name) {

		setMatrix(source);
		this.name = Objects.requireNonNull(name, "Name parameter must not be null.");

	}

	/**
	 * Constructor used to initialize a matrix where values will be set separately.
	 *
	 * @param rows			the number of rows
	 * @param columns		the number of columns
	 * @param name			the name of the matrix
	 * @param savedToDisk	{@code true} if saved to a disk, {@code false} otherwise
	 * @throws IllegalArgumentException		if {@code rows} or {@code columns} is negative
	 */
	public Matrix(int rows, int columns, String name, boolean savedToDisk) {

		if (rows < 1 || columns < 1) {
			throw new IllegalArgumentException("Minimum of 1 row and 1 column: rows = " + rows + ", columns = " + columns + ".");
		}
		this.rows = rows;
		this.columns = columns;
		this.matrix = new double[rows][columns];
		this.name = name;
		this.savedToDisk = savedToDisk;
		this.readonly = false;

	}

	/**
	 * Constructor used to initialize a matrix with the values provided as a 2D array of doubles.
	 * <p>
	 * The values will be deep-copied from the {@code values} array so as to prevent modifications.
	 * </p>
	 *
	 * @param rows			the number of rows
	 * @param columns		the number of columns
	 * @param values		a 2D array of doubles containing the values
	 * @param name			the name of the matrix
	 * @param savedToDisk	{@code true} if saved to a disk, {@code false} otherwise
	 * @throws IllegalArgumentException		if {@code rows} or {@code columns} is negative; if
	 * 										{@code values.length != rows}; or if any row length differs
	 * 										from {@code columns}
	 * @throws NullPointerException			if {@code values} is {@code null} or any row is {@code null}.
	 */
	public Matrix(int rows, int columns, double[][] values, String name, boolean savedToDisk) {

		if (rows < 0 || columns < 0) {
			throw new IllegalArgumentException("Rows or columns are negative: rows = " + rows + ", columns = " + columns + ".");
		}
		Objects.requireNonNull(values, "Values array must not be null.");
	    if (values.length != rows) {
	        throw new IllegalArgumentException("Values array must have exactly " + rows + " rows.");
	    }
		this.rows = rows;
		this.columns = columns;
		this.matrix = new double[rows][columns];
	    for (int i = 0; i < rows; i++) {
	        if (values[i] == null) {
	            throw new NullPointerException("Matrix cannot contain null rows.");
	        }
	        if (values[i].length != columns) {
	            throw new IllegalArgumentException("Matrix rows must have the same length.");
	        }
	        System.arraycopy(values[i], 0, this.matrix[i], 0, columns);
	    }
		this.name = name;
		this.savedToDisk = savedToDisk;
		this.readonly = false;

	}
	
	// ----------------------
	// ACCESSORS AND MUTATORS
	// ----------------------
	
	/**
	 * Retrieves the number of rows in the matrix.
	 *
	 * @return		the number of rows
	 */
	public int getRows() { return this.rows; }

	/**
	 * Retrieves the number of columns in the matrix.
	 *
	 * @return		the number of columns
	 */
	public int getColumns() { return this.columns; }

	/**
	 * Sets the name of the matrix to the given string.
	 *
	 * @param name		the name of the matrix
	 */
	public void setName(String name) { this.name = Objects.requireNonNull(name, "Matrix name cannot be null."); }

	/**
	 * Returns a string containing the name of the matrix.
	 *
	 * @return		the name of the matrix
	 */
	public String getName() { return this.name; }

	/**
	 * Sets the {@code readonly} flag.
	 *
	 * @param readonly		{@code true} if the matrix is read-only; otherwise {@code false}
	 */
	public void setReadOnly(boolean readonly) {
		
		this.readonly = readonly;
		
		log.debug("Matrix {} set to {}.", this.getName(), ((readonly) ? "read-only" : "mutable"));
		
	}

	/**
	 * Retrieves the {@code readonly} flag.
	 *
	 * @return		{@code true} if the matrix is read-only; otherwise {@code false}
	 */
	public boolean isReadOnly() { return this.readonly; }

	/**
	 * Sets the {@code savedToDisk} flag.
	 *
	 * @param savedToDisk		{@code true} if saved to disk, {@code false} otherwise
	 */
	public void setSavedToDisk(boolean savedToDisk) {
		
		this.savedToDisk = savedToDisk;
		
		log.debug("Matrix {} set to {}.", this.getName(), ((savedToDisk) ? "saved to disk" : "not saved to disk"));

	}

	/**
	 * Retrieves the {@code savedToDisk} flag.
	 *
	 * @return		{@code true} if saved to disk, {@code false} otherwise
	 */
	public boolean isSavedToDisk() { return this.savedToDisk; }
	
	/**
	 * Sets the value of the matrix element at {@code row} and {@code column}.
	 *
	 * @param row		the row of the element (zero-indexed)
	 * @param column	the column of the element (zero-indexed)
	 * @param value		the value to assign to the element
	 * @throws UnsupportedOperationException		if modifications are attempted to a read-only matrix
	 * @throws IndexOutOfBoundsException			if {@code row} is outside {@code [0, rows)}, or
	 * 												{@code column} is outside {@code [0, columns)}
	 */
	public void setValue(int row, int column, double value) {

		checkNotReadOnly();
		checkIndex(row, column);

		this.matrix[row][column] = value;
		markDirty();
		log.trace("Set matrix[{}][{}] to {}.", row, column, value);

	}

	/**
	 * Retrieves a value from the matrix at {@code row} and {@code column}.
	 *
	 * @param row		the row of the element (zero-indexed)
	 * @param column	the column of the element (zero-indexed)
	 * @return			the element value
	 * @throws IndexOutOfBoundsException		if {@code row} is outside {@code [0, rows)}, or
	 * 											{@code column} is outside {@code [0, columns)}
	 */
	public double getValue(int row, int column) {

		checkIndex(row, column);

		double value = this.matrix[row][column];
		log.trace("Retrieved element {} from matrix[{}][{}].", value, row, column);
		return value;

	}

	// --------------------------------------------------------------------------
	// SHAPE AND FACTS
	// Pure queries about this matrix. No mutation; some methods may use cached
	// decompositions for performance and are recomputed when the matrix mutates.
	// --------------------------------------------------------------------------
	
	/**
	 * Tests if the matrix is empty (either 0 x 0, 0 x n, or n x 0).
	 *
	 * @return		{@code true} if {@code rows == 0 || columns == 0}, otherwise {@code false}
	 */
	@JsonIgnore public boolean isEmpty() { return this.rows == 0 || this.columns == 0; }

	/**
	 * Tests if the matrix is square (n x n).
	 *
	 * @return		{@code true} if the matrix is square, {@code false} otherwise
	 */
	@JsonIgnore
	public boolean isSquare() { return this.rows == this.columns; }

	/**
	 * Tests if the matrix is invertible {@code det(matrix) != 0}.
	 *
	 * @return		{@code true} if the matrix is invertible, {@code false} otherwise
	 */
	@JsonIgnore
	public boolean isInvertible() {

		final double det = determinant();
		final double scale = MatrixUtils.safeScale(this.getMatrix());

		if (!MatrixUtils.nearlyZero(det, scale)) {
			return true;
		} else {
			return false;
		}

	}
	
	/**
	 * Returns the rank of the given matrix.
	 * <p>
	 * If the value of rank is cached, return that. Otherwise, return a new computation using ref().
	 * </p>
	 * 
	 * @return		the rank as an integer
	 */
	public int rank() {
		
		int rank;
		if (cache.ref != null && cache.ref.version == modCount) {
			rank = cache.ref.rank;
		} else if (cache.rref != null && cache.rref.version == modCount) {
			rank = cache.rref.rank;
		} else {
			rank = ref().rank;
		}
		return rank;
	}
	
	/**
	 * Returns the nullity of the given matrix as matrix rank subtracted from the number of columns.
	 * 
	 * @return
	 */
	public int nullity() {

		return this.columns - rank();

	}
	
	/**
	 * Calls {@code Determinants.calculateDeterminantRecursively()} after performing some checks.
	 *
	 * @return			the calculated determinant
	 * @implNote 		For n > 2 this delegates to recursive expansion via {@link Determinants}.
	 *           		Prefer LU (when available) for performance and stability.
	 */
	public double determinant() {

		LogUtils.logMethodEntry(log);

		if (!this.isSquare()) {
			throw new IllegalArgumentException("Determinant is undefined for non-square matrices.");
		}
		if (this.isEmpty()) {
			throw new IllegalArgumentException("Determinant is undefined for empty matrices.");
		}

		double det = Determinants.calculateDeterminantRecursively(this.getMatrix());
		log.debug("Determinant of {} found for matrix {}.", det, this);
		return det;

	}
	
	/**
	 * Calls {@code Determinants.minorMatrix()} to generate a minor matrix of the receiver matrix
	 * by dropping the {@code row} and {@code column} specified.
	 * 
	 * @param row			the row to drop
	 * @param column		the column to drop
	 * @return				a Matrix object with the resulting minor matrix
	 */
	public Matrix minorMatrix(int row, int column) {
		
		Matrix minorMatrix = new Matrix(Determinants.minorMatrix(this.getMatrix(), row, column), "MINOR");
		
		return minorMatrix;
	}

	// -------------------------------------------------
	// CORE API
	// Contains the methods for basic matrix arithmetic.
	// -------------------------------------------------
	
	/**
	 * Adds this matrix to the given matrix, provided they are both of the same size.
	 * <p>
	 * Added matrices can be read-only as the summation creates a new matrix.
	 * </p>
	 *
	 * @param addend		the addend
	 * @return				a new matrix containing the sum of {@code this} and {@code addend}.
	 * @throws IllegalArgumentException		if either matrix is empty or they are different sizes
	 * @throws NullPointerException			if the addend is null
	 */
	public Matrix add(Matrix addend) {

		LogUtils.logMethodEntry(log);

		Objects.requireNonNull(addend, "Addend must not be null.");
		if (this.isEmpty() || addend.isEmpty()) {
			throw new IllegalArgumentException("Cannot add empty matrices.");
		}

		final int rows = this.getRows();
		final int columns = this.getColumns();

		if ((this.getRows() != addend.getRows()) || (this.getColumns() != addend.getColumns())) {
			throw new IllegalArgumentException(
				String.format("Matrix addends must be the same size: matrix %s is [%d, %d], matrix %s is [%d, %d].",
					this.getName(), rows, columns,
					addend.getName(), addend.getRows(), addend.getColumns()));
		}

		final double[][] left = this.getMatrix();
		final double[][] right = addend.getMatrix();

		double[][] elements = new double[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				elements[i][j] = left[i][j] + right[i][j];
			}
		}

		Matrix sum = new Matrix(elements, this.getName() + "+" + addend.getName());
		log.debug("Added matrices {} and {} to get {}.", this, addend, sum);
		return sum;

	}

	/**
	 * Subtracts the given matrix from this matrix, provided they are both of the same size.
	 * <p>
	 * Subtracted matrices can be read-only as generating the difference creates a new matrix.
	 * </p>
	 *
	 * @param subtrahend		the subtrahend
	 * @return					a new matrix containing the difference between {@code this} and {@code subtrahend}.
	 * @throws IllegalArgumentException		if either matrix is empty or they are different sizes
	 * @throws NullPointerException			if the subtrahend is null
	 */
	public Matrix subtract(Matrix subtrahend) {

		LogUtils.logMethodEntry(log);

		Objects.requireNonNull(subtrahend, "Subtrahend must not be null.");
		if (this.isEmpty() || subtrahend.isEmpty()) {
			throw new IllegalArgumentException("Cannot subtract empty matrices.");
		}

		final int rows = this.getRows();
		final int columns = this.getColumns();

		if ((this.getRows() != subtrahend.getRows()) || (this.getColumns() != subtrahend.getColumns())) {
			throw new IllegalArgumentException(
				String.format("Matrices must be the same size: matrix %s is [%d, %d], matrix %s is [%d, %d].",
					this.getName(), rows, columns,
					subtrahend.getName(), subtrahend.getRows(), subtrahend.getColumns()));
		}

		final double[][] left = this.getMatrix();
		final double[][] right = subtrahend.getMatrix();

		double[][] elements = new double[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				elements[i][j] = left[i][j] - right[i][j];
			}
		}

		Matrix difference = new Matrix(elements, this.getName() + "-" + subtrahend.getName());
		log.debug("Subtracted matrix {} from matrix {} to get {}.", subtrahend, this, difference);
		return difference;

	}

	/**
	 * Multiplies this matrix by the given matrix.
	 * <p>
	 * The columns of this matrix must be equal to the rows of the given matrix. Matrices
	 * can be read-only as generating the product creates a new matrix.
	 * </p>
	 * @param factor		the factor
	 * @return				a new matrix containing the product of {@code this} and {@code factor}
	 * @throws IllegalArgumentException		if either matrix is empty or the columns of this matrix
	 * 										don't equal the rows of the given matrix
	 * @throws NullPointerException			if the factor is null
	 */
	public Matrix multiply(Matrix factor) {

		LogUtils.logMethodEntry(log);
		
		Objects.requireNonNull(factor, "Factor must not be null.");
		if (this.isEmpty() || factor.isEmpty()) {
			throw new IllegalArgumentException("Cannot multiply empty matrices.");
		}

		final int leftRows = this.getRows();
		final int leftColumns = this.getColumns();
		final int rightRows = factor.getRows();
		final int rightColumns = factor.getColumns();
		
		if (this.getColumns() != factor.getRows()) {
			throw new IllegalArgumentException(
				String.format("The columns of the first factor and the rows of the second factor are not equal: " +
					"matrix %s is [%d, %d], matrix %s is [%d, %d].",
					this.getName(), leftRows, leftColumns,
					factor.getName(), rightRows, rightColumns));
		}

		final double[][] left = this.getMatrix();
		final double[][] right = factor.getMatrix();

		double[][] elements = new double[leftRows][rightColumns];
		for (int i = 0; i < leftRows; i++) {
			for (int j = 0; j < rightColumns; j++) {
				double sum = 0.0;
				for (int k = 0; k < leftColumns; k++) {
					sum += left[i][k] * right[k][j];
				}
				elements[i][j] = sum;
			}
		}

		Matrix product = new Matrix(elements, this.getName() + "*" + factor.getName());
		log.debug("Multiplied matrix {} by matrix {} to get {}.", this, factor, product);
		return product;

	}

	/**
	 * Multiplies this matrix by a scalar.
	 * <p>
	 * The matrix can be read-only as this operation returns a new matrix.
	 * </p>
	 *
	 * @param scalar		The scalar with which to multiply the matrix
	 * @return				a new matrix containing the product of {@code this} and {@code scalar}
	 * @throws IllegalArgumentException		if this matrix is empty
	 */
	public Matrix multiply(double scalar) {

		LogUtils.logMethodEntry(log);
		
		if (this.isEmpty()) {
			throw new IllegalArgumentException("Cannot multiply an empty matrix by a scalar.");
		}

		final int rows = this.getRows();
		final int columns = this.getColumns();
		final double[][] source = this.getMatrix();

		double[][] elements = new double[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				elements[i][j] = source[i][j] * scalar;
			}
		}

		Matrix product = new Matrix(elements, this.getName() + "*" + scalar);
		log.debug("Multiplied matrix {} by the scalar {} to get matrix {}.", this, scalar, product);
		return product;

	}

	/**
	 * Calculates the transpose of the matrix.
	 *
	 * @return			the transpose of the matrix
	 */
	public Matrix transpose() {

		LogUtils.logMethodEntry(log);

		final int rows = this.getRows();
		final int columns = this.getColumns();
		final double[][] source = this.getMatrix();

		double[][] result = new double[columns][rows];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				result[j][i] = source[i][j];
			}
		}

		Matrix transpose = new Matrix(result, "T(" + this.getName() + ")");
		log.debug("Matrix {} transposed to {}.", this, transpose);
		return transpose;

	}

	/**
	 * Calculates the inverse of the matrix if it's both square and invertible.
	 *
	 * @return			the inverse of the receiver matrix
	 */
	public Matrix inverse() {

		LogUtils.logMethodEntry(log);

		if (!isInvertible()) {
			throw new IllegalArgumentException("The matrix is not invertible.");
		}
		if (!isSquare()) {
			throw new IllegalArgumentException("The matrix is not square.");
		}

		Matrix inverse = this.cofactors().transpose().multiply(1 / this.determinant());
		inverse.setName("I(" + this.getName() + ")");

		log.debug("Matrix {} inverted to {}.", this, inverse);
		return inverse;

	}
	
	/**
	 * Calculates the matrix of cofactors for the matrix parameter.
	 *
	 * @return		the matrix of cofactors
	 */
	public Matrix cofactors() {

		LogUtils.logMethodEntry(log);

		final int rows = this.getRows();
		final int columns = this.getColumns();
		final double[][] matrix = this.getMatrix();

		double[][] cofactors = new double[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				cofactors[i][j] = Determinants.cofactor(matrix, i, j);
			}
		}

		Matrix cof = new Matrix(cofactors, "C(" + this.getName() + ")");
		log.debug("Cofactor matrix {} calculated from matrix {}.", cof, this);
		return cof;

	}
	
	/**
	 * Finds the Hadamard product between two equally sized matrices.
	 * <p>
	 * The Hadamard product represents an element-wise multiplication, i.e.,
	 * c(i, j) = a(i, j) * b(i, j).
	 * </p>
	 * 
	 * @param factor		the right matrix
	 * @return				the Hadamard product
	 */
	public Matrix hadamard(Matrix factor) {

		LogUtils.logMethodEntry(log);
		
		Objects.requireNonNull(factor, "Factor must not be null.");
		if (this.isEmpty() || factor.isEmpty()) {
			throw new IllegalArgumentException("Cannot compute Hadamard product with empty matrices.");
		}

		final int rows = factor.getRows();
		final int columns = factor.getColumns();
		
		if (this.getRows() != rows || this.getColumns() != columns) {
			throw new IllegalArgumentException(
				String.format("The rows and columns of the factors do not match: " +
					"matrix %s is [%d, %d], matrix %s is [%d, %d].",
					this.getName(), this.getRows(), this.getColumns(),
					factor.getName(), rows, columns));
		}
		
		final double[][] left = this.getMatrix();
		final double[][] right = factor.getMatrix();

		double[][] elements = new double[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				elements[i][j] = left[i][j] * right[i][j];
			}
		}

		Matrix hadamard = new Matrix(elements, this.getName() + "∘" + factor.getName());
		log.debug("Found the Hadamard product of matrix {} and matrix {} to get {}.", this, factor, hadamard);
		return hadamard;

	}
	
	/**
	 * Finds the Kronecker product of two matrices.
	 * <p>
	 * The Kronecker product of an n x m and a p x q matrix fills a np x mq matrix
	 * with the results of the second matrix times the individual elements of the
	 * first matrix.
	 * 
	 * @param factor		the right matrix
	 * @return				the Kronecker product
	 */
	public Matrix kronecker(Matrix factor) {
		
		LogUtils.logMethodEntry(log);

		Objects.requireNonNull(factor, "Factor must not be null.");
		if (this.isEmpty() || factor.isEmpty()) {
			throw new IllegalArgumentException("Cannot compute Kronecker product with empty matrices.");
		}
		
		final int sourceRows = this.getRows();
		final int sourceColumns = this.getColumns();
		final int destRows = factor.getRows();
		final int destColumns = factor.getColumns();
		final double[][] sourceFactor = this.getMatrix();
		final double[][] destFactor = factor.getMatrix();
		
		double[][] elements = new double[sourceRows * destRows][sourceColumns * destColumns];
		
		for (int i = 0; i < sourceRows; i++) {
			for (int j = 0; j < sourceColumns; j++) {
				for (int k = 0; k < destRows; k++) {
					for (int l = 0; l < destColumns; l++) {
						elements[(i * destRows) + k][(j * destColumns) + l] = sourceFactor[i][j] * destFactor[k][l];
					}
				}
			}
		}
		
		Matrix kronecker = new Matrix(elements, this.getName() + "⊗" + factor.getName());
		log.debug("Found the Kronecker product of matrix {} and matrix {} to get {}.", this, factor, kronecker);
		return kronecker;
		
	}
	
	/**
	 * Applies a programmer defined function to the receiver matrix.
	 * 
	 * @param function		a function to apply to the matrix
	 * @return				the modified matrix
	 */
	public Matrix map(DoubleUnaryOperator function) {
		
		LogUtils.logMethodEntry(log);
		
		Objects.requireNonNull(function, "Function must not be null.");
		
		final int rows = this.getRows();
		final int columns = this.getColumns();
		double[][] source = this.getMatrix();
		double[][] dest = new double[rows][columns];
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				dest[i][j] = function.applyAsDouble(source[i][j]);
			}
		}
		
		Matrix applied = new Matrix(dest, "f(" + this.getName() + ")");
		log.debug("Applied a programmer supplied function to matrix {}.", this.getName());
		return applied;
		
	}
	
	
	// -------------------------------
	// DECOMPOSITIONS
	// Contains factorization methods.
	// -------------------------------

	/**
	 * Calculates the row echelon form (REF) of the receiver Matrix and returns
	 * the REF as a new Matrix object.
	 *
	 * @return		the REFResult containing the matrix
	 */
	public REFResult ref() {
		
		LogUtils.logMethodEntry(log);
		
	    if (cache.ref != null && cache.ref.version == modCount) {
	    	log.debug("Returned cached value of REF matrix {}.", cache.ref.ref());
	        return cache.ref;
	    }
		
	    double[][] matrixValues = this.getMatrix();
	    double[][] ref = ReducedForm.calculateREF(matrixValues);
		int rank = ReducedForm.calculateRank(ref);
		
		Matrix refMatrix = new Matrix(ref, "REF(" + this.getName() + ")");
		refMatrix.setReadOnly(true);
		cache.ref = new REFResult(refMatrix, rank, modCount);

		log.debug("Calculated REF matrix {} from {}.", refMatrix, this);
		return cache.ref;
		
	}
	
	/**
	 * Access the REF Matrix contained within the cached results.
	 * <p>
	 * Returns an immutable, cached view; recomputed only when the source matrix mutates.
	 * </p>
	 * 
	 * @return		the cached REF matrix
	 */
	public Matrix rowEchelonForm() {
		
		return ref().ref();
		
	}

	/**
	 * Calculates the reduced row echelon form (RREF) of the receiver Matrix and returns
	 * the RREF as a new Matrix object.
	 *
	 * @return		the RREFResult containing the matrix
	 */
	public RREFResult rref() {

		LogUtils.logMethodEntry(log);

	    if (cache.rref != null && cache.rref.version == modCount) {
	    	log.debug("Returned cached value of RREF matrix {}.", cache.rref.rref());
	        return cache.rref;
	    }
		
	    double[][] matrixValues = this.getMatrix();
	    double[][] rref = ReducedForm.calculateRREF(matrixValues);
	    int rank = ReducedForm.calculateRank(rref);
	    
		Matrix rrefMatrix = new Matrix(rref, "RREF(" + this.getName() + ")");
		rrefMatrix.setReadOnly(true);
		cache.rref = new RREFResult(rrefMatrix, rank, modCount);

		log.debug("Calculated RREF matrix {} from {}.", rrefMatrix, this);
		return cache.rref;

	}
	
	/**
	 * Access the RREF Matrix contained within the cached results.
	 * <p>
	 * Returns an immutable, cached view; recomputed only when the source matrix mutates.
	 * </p>
	 * 
	 * @return		the cached RREF matrix
	 */
	public Matrix reducedRowEchelonForm() {
		
		return rref().rref();
		
	}
	
	// ----- 
	// NORMS
	// -----
	
	public double trace() {
		
		double trace = 0;
		double[][] values = this.getMatrix();
		final int rows = this.getRows();
		
		if (!isSquare()) {
			throw new IllegalArgumentException("The matrix must be square.");
		}
		if (isEmpty()) {
			throw new IllegalArgumentException("The matrix cannot be empty.");
		}
		
		for (int i = 0; i < rows; i++) {
			trace += values[i][i];
		}
		
		return trace;
		
	}
	
	public double l1Norm() {
		
		double sum;
		double max = 0.0;
		double[][] values = this.getMatrix();
		final int rows = this.getRows();
		final int columns = this.getColumns();
		
		for (int j = 0; j < columns; j++) {
			sum = 0;
			for (int i = 0; i < rows; i++) {
				sum += Math.abs(values[i][j]);
			}
			if (sum > max) {
				max = sum;
			}
		}
		
		return max;
		
	}

	public double infinityNorm() {
		
		double sum;
		double max = 0.0;
		double[][] values = this.getMatrix();
		final int rows = this.getRows();
		final int columns = this.getColumns();
		
		for (int i = 0; i < rows; i++) {
			sum = 0;
			for (int j = 0; j < columns; j++) {
				sum += Math.abs(values[i][j]);
			}
			if (sum > max) {
				max = sum;
			}
		}
		
		return max;
		
	}
	
	public double frobeniusNorm() {
		
		if (isEmpty()) {
			throw new IllegalArgumentException("The matrix cannot be empty.");
		}
		
		double sumOfSquares = 0.0;
		double[][] values = this.getMatrix();
		final int rows = this.getRows();
		final int columns = this.getColumns();

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				sumOfSquares += values[i][j] * values[i][j];
			}
		}

		return Math.sqrt(sumOfSquares);
	}

	// --------------------------------------------------
	// EQUALITY, HASHING, STRING REPRESENTATIONS
	// --------------------------------------------------
	
	/**
	 * Tests whether two matrices are approximately equal to one another.
	 * <p>
	 * The method calls {@code bucketIndex()} to put doubles into buckets
	 * for comparison. Buckets are width {@code 2 * EPSILON}, so the method
	 * makes pairs with {@code |a − b| < EPSILON} equal; equality at exactly
	 * {@code EPSILON} may differ.
	 * </p>
	 * <p>
	 * NaN compares equal only to NaN.
	 * </p>
	 *
	 * @param matrix	a matrix to compare to the implicit values
	 * @return			{@code true} if the matrix elements are within EPSILON of
	 * 					each other, {@code false} if any element is not
	 */
	public boolean equalsApprox(Matrix matrix) {

	    Objects.requireNonNull(matrix, "Matrix to compare cannot be null.");

	    final int rows = this.getRows();
	    final int columns = this.getColumns();

		if (this == matrix) {
			return true;
		}
		if (rows != matrix.getRows() || columns != matrix.getColumns()) {
			return false;
		}
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				if (MatrixUtils.bucketIndex(this.matrix[i][j]) != MatrixUtils.bucketIndex(matrix.matrix[i][j])) {
					return false;
				}
			}
		}
		return true;

	}

	/**
	 * Tests whether two matrices are exactly equal to one another.
	 * <p>
	 * Treats {@code -0.0} and {@code +0.0} as equal; any comparison with {@code NaN}
	 * is unequal except when compared to one another ({@code NaN == NaN}).
	 * </p>
	 *
	 * @param matrix	a matrix to compare to the implicit values
	 * @return			{@code true} if the matrix elements are exactly equal to
	 * 					one another, {@code false} otherwise
	 */
	public boolean equalsExact(Matrix matrix) {

	    final int rows = this.getRows();
	    final int columns = this.getColumns();

		if (this == matrix) {
			return true;
		}
		if (rows != matrix.getRows() || columns != matrix.getColumns()) {
			return false;
		}
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				if (this.matrix[i][j] == 0.0 && matrix.matrix[i][j] == 0.0) {
					continue;
				}
				if (Double.isNaN(this.matrix[i][j]) && Double.isNaN(matrix.matrix[i][j])) {
					continue;
				}
				if (this.matrix[i][j] != matrix.matrix[i][j]) {
					return false;
				}
			}
		}
		return true;

	}

	/**
	 * Compares this matrix to the specified object for exact equality.
	 * <p>
	 * Equality is strict by class (using {@code getClass()}) and element-wise
	 * exact.
	 * </p>
	 *
	 * @param object 		the object to compare with
	 * @return 				{@code true} if {@code object} is a {@code Matrix} of the same class,
	 *        				dimensions, and with exactly equal elements; otherwise {@code false}
	 */
	@Override
	public boolean equals(Object object) {

	    if (this == object) {
	    	return true;
	    }
	    if (!(object instanceof Matrix other)) {
			return false;
		}
		return equalsExact(other);

	}

	/**
	 * Generates a hash code representing the matrix.
	 *
	 * @return			the hash code as an integer
	 * @implNote 		Normalizing -0.0 to +0.0 and NaNs using the IEEE 754 bit layout in
	 * 					row-major order. Created with the help of ChatGPT (OpenAI).
	 */
	@Override
	public int hashCode() {
		
		long seed = 1;
		final int rows = this.getRows();
		final int columns = this.getColumns();

		seed = (seed * 31) + rows;
		seed = (seed * 31) + columns;

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				double element = this.getValue(i, j);
				if (element == 0.0) {
					element = 0.0;
				}
				if (Double.isNaN(element)) {
					element = Double.NaN;
				}
	            long bits = (this.matrix[i][j] == 0.0 ? 0 : Double.doubleToLongBits(this.matrix[i][j]));
	            int elementHash = (int) (bits ^ (bits >>> 32));
				seed = (seed * 31) + elementHash;
			}
		}
		return (int) (seed ^ (seed >>> 32));

	}

	/**
	 * Returns a string representation of the matrix.
	 *
	 * @return		a short representation of the matrix
	 */
	@Override
	public String toString() {

		return toShortString();

	}

	/**
	 * Returns a compact, bracketed representation of matrix values.
	 *
	 * @return		a short representation of the matrix
	 */
	public String toShortString() {
		
		log.debug("Built short string for matrix {}.", this.getName());

		return MatrixUtils.toShortString(this);
		
	}

	/**
	 * Returns a pretty representation of matrix values.
	 *
	 * @return		a pretty representation of the matrix
	 */
	public String toPrettyString() {

		log.debug("Built pretty string for matrix {}.", this.getName());
		
		return MatrixUtils.toPrettyString(this);

	}

	// ---------
	// INTERNALS
	// ---------

	/**
	 * Calls {@code this.getMatrix} to provide a deep copy of the matrix array
	 *
	 * @return		a copy of the array
	 */
	public double[][] toArrayCopy() {

	    return this.getMatrix();

	}

	/**
	 * Returns a new Matrix object which is a deep copy of the receiver array
	 *
	 * @return		the copied matrix
	 */
	public Matrix copy() {

		return new Matrix(this.toArrayCopy(), "CopyOf" + this.getName());

	}
	
	/**
	 * Increments {@code modCount} when called.
	 * <p>
	 * This is used for determining cache versions.
	 * </p>
	 */
	private void markDirty() {
		 
		modCount++;
	 
	}
	
	/**
	 * Resets {@code rows} and {@code columns} from {@code source}.
	 * <p>
	 * Copies values defensively.
	 * </p>
	 *
	 * @param source		the elements of the matrix as a double[][]
	 * @throws UnsupportedOperationException		if an attempt is made to modify a read-only matrix
	 * @throws NullPointerException					if the {@code source} array is equal to null or contains
	 * 												null rows
	 * @throws IllegalArgumentException				if the {@code source} array does not contain at
	 * 												least one row or column or is not rectangular
	 */
	@JsonProperty("matrix")
	private void setMatrix(double[][] source) {
		
		Objects.requireNonNull(source, "Source array must not be null.");

		checkNotReadOnly();

	    final int columns = source[0].length;
	    
	    for (double[] row : source) {
			Objects.requireNonNull(row, "Matrix data must not contain null rows.");
	        if (row.length != columns) {
	            throw new IllegalArgumentException("Matrix rows must have the same length.");
	        }
	    }

	    double[][] copy = new double[source.length][columns];
	    for (int i = 0; i < source.length; i++) {
	        System.arraycopy(source[i], 0, copy[i], 0, columns);
	    }

	    this.rows = source.length;
	    this.columns = columns;
	    this.matrix = copy;
	    markDirty();

	}

	/**
	 * Retrieves a copy of the matrix elements as a double[][].
	 * <p>
	 * The elements are copied from the internal {@code matrix} array to allow for
	 * modifications.
	 * </p>
	 *
	 * @return		a deep copy of the matrix values so that the returned array is
	 * 				safe to modify
	 */
	@JsonProperty("matrix")
	private double[][] getMatrix() {

	    double[][] copy = new double[this.rows][this.columns];
	    for (int i = 0; i < this.rows; i++) {
	        System.arraycopy(this.matrix[i], 0, copy[i], 0, this.columns);
	    }
	    return copy;

	}
	
	/**
	 * Throws an exception if the matrix is read-only.
	 * <p>
	 * Used for checking whether a matrix is immutable before an attempt at mutation.
	 * </p>
	 * 
	 * @throws UnsupportedOperationException	if the matrix is read-only
	 */
	private void checkNotReadOnly() {
		
		if (this.readonly) {
		    log.warn("Attempt to write to read-only matrix in {}.", LogUtils.methodAtDepth(3));
		    throw new UnsupportedOperationException("The values of a read-only matrix cannot be set.");			
		}
	}
	
	/**
	 * Throws an exception if the {@code row} and {@code column} values are less than 1
	 * or greater than the size of the matrix.
	 * 
	 * @param row			the row value to check
	 * @param column		the column value to check
	 * @throws IndexOutOfBoundsException	if the parameters fail the check
	 */
	private void checkIndex(int row, int column) {
		
		if (row < 0 || row >= this.rows || column < 0 || column >= this.columns) {
		    log.warn("Index [{}, {}] out of bounds for size [{}, {}] in {}.",
		             row, column, rows, columns, LogUtils.methodAtDepth(3));
		    throw new IndexOutOfBoundsException(
		    		  String.format("Invalid index: [%d, %d] for matrix size [%d, %d].%n",
		    				        row, column, rows, columns));
		}	
	}
	
}
