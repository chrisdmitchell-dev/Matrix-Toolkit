package com.chrisdmitchell.matrix.model;

import static com.chrisdmitchell.matrix.util.Constants.Numeric.ABSOLUTE_EPSILON;
import static com.chrisdmitchell.matrix.util.Constants.Numeric.EPSILON;
import static com.chrisdmitchell.matrix.util.Constants.Numeric.NEGATIVE_INFINITY_SENTINEL;
import static com.chrisdmitchell.matrix.util.Constants.Numeric.NaN_SENTINEL;
import static com.chrisdmitchell.matrix.util.Constants.Numeric.POSITIVE_INFINITY_SENTINEL;
import static com.chrisdmitchell.matrix.util.Constants.Numeric.RANDOM_MATRIX_NAME_LENGTH;
import static com.chrisdmitchell.matrix.util.Constants.Numeric.RELATIVE_EPSILON;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.chrisdmitchell.matrix.util.LogUtils;
import com.chrisdmitchell.matrix.util.MatrixUtils;

/* TODO: Matrix.java cleanup & enhancements
*
* 🔧 Correctness / API contracts
* [X] Stop mutating instance fields inside methods. Use locals instead.
*     - In reducedRowEchelonForm(): replace
*         rows = ref.getRows(); columns = ref.getColumns();
*       with
*         final int rows = ref.getRows(); final int columns = ref.getColumns();
*     - In transpose(), cofactors(), etc., read into locals instead of assigning to fields.
*
* [X] equals(Object) must not throw NPE and must obey the contract.
*     - Replace current implementation with:
*         @Override public boolean equals(Object o) {
*           if (this == o) return true;
*           if (!(o instanceof Matrix other)) return false; // or classic instanceof + cast
*           return equalsExact(other);
*         }
*
* [X] Align equalsExact() behavior with its Javadoc.
*     - Treat -0.0 and +0.0 as equal.
*     - Make NaN compare equal to NaN (per docs).
*     - Implement comparison via Double.doubleToLongBits() after normalizing zeros.
*       (Keeps equals/hashCode consistent.)
*
* [X] isInvertible() should be tolerance-aware (floating point).
*     - Use nearlyZero(det, scale) with a sensible scale (e.g., maxAbs of the matrix).
*
* 🧮 Numerical robustness
* [X] In REF/RREF, compute the scale from the working array you’re mutating.
*     - Prefer scanning newValues (or a) to get amax instead of calling getMaxValue() on this/ref.
*
* [X] REF loop header: fold the row guard into the for-condition.
*     - for (int j = 0; j < columns && nextRow < rows; j++) { ... }
*
* [ ] Use column-local scale for “below-pivot” tests and matrix-global scale for clamping.
*     - You already use bestAbs for below-pivot checks and maxValue/amax for clamping—keep that pattern.
*
* [ ] After normalizing pivot rows in RREF, set the pivot cell to exactly 1.0 and clamp “-0.0” to 0.0.
*     - You already set pivot to 1.0; ensure any “-0.0” are cleaned when clamping.
*
* 🧱 Helpers & structure
* [ ] Make helpers static where they don’t use instance state.
*     - swapRows(...), nearlyZero(...), and a new maxAbs(double[][]) would be private static.
*
* [ ] Consider renaming getMaxValue() → maxAbs() (it returns the maximum absolute value).
*     - Or update Javadoc to say “largest absolute element.”
*
* [ ] equalsApprox()/bucketIndex: confirm EPSILON/tolerance policy matches test helpers.
*     - Document that equality at exactly EPSILON may differ (already noted).
*
* ⚙️ Performance / future work
* [ ] inverse(): current adjugate/determinant method is educational and O(n^4)+, unstable.
*     - Replace with LU decomposition (with partial pivoting) or Gauss–Jordan on [A|I].
*     - Keep current method but mark in Javadoc as “for small matrices / demonstration.”
*
* [ ] Determinism and portability:
*     - If you need strict reproducibility across JVMs, consider `strictfp` methods for REF/RREF.
*
* 📝 Javadoc & style
* [ ] Update Javadoc for equalsExact to match the (fixed) NaN/±0 behavior.
* [ ] Keep @implNote on REF/RREF noting partial pivoting and absolute+relative tolerance.
* [ ] isEmpty()/isSquare(): return expressions directly (`return rows == 0 || columns == 0;`).
* [ ] generateRandomMatrixName(): consider ThreadLocalRandom for simplicity.
*
* 🧪 Tests to add/extend
* [ ] REF invariants: pivot columns strictly increase; entries below pivots ~ 0; zero rows at bottom.
* [ ] RREF uniqueness: compare RREFs from different elimination paths → identical under tolerance.
* [ ] equalsExact corner cases: ±0.0, NaN vs NaN, infinities, random matrices.
* [ ] isInvertible() tolerance: near-singular matrices where |det| ≈ 0 under scale.
* [ ] inverse() small cases: 1×1, 2×2, and a singular matrix that should throw.
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
public class Matrix {

	private int rows, columns;
	private String name;
	private boolean savedToDisk, readonly;

	/**
	 * Set to {@code @JsonIgnore} so that Jackson uses {@code getMatrix()} and {@code setMatrix()}
	 * to get/set the {@code double[][] matrix}.
	 */
	@JsonIgnore
	private double[][] matrix;

	private static final Logger log = LoggerFactory.getLogger(Matrix.class);

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
		this.name = generateRandomMatrixName();

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

		if (rows < 0 || columns < 0) {
			throw new IllegalArgumentException("Rows or columns are negative: rows = " + rows + ", columns = " + columns + ".");
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

	/**
	 * Generates a random name for a matrix with length {@code RANDOM_MATRIX_NAME_LENGTH} consisting
	 * of random capital letters.
	 *
	 * @return		the generated name
	 */
	public String generateRandomMatrixName() {

		Random random = new Random();
		StringBuilder generatedName = new StringBuilder(RANDOM_MATRIX_NAME_LENGTH);

		for (int i = 0; i < RANDOM_MATRIX_NAME_LENGTH; i++) {
			generatedName.append((char) ('A' + random.nextInt(26)));
		}

		return generatedName.toString();

	}

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

		final int rows = this.getRows();
		final int columns = this.getColumns();

		if (this.readonly) {
			log.warn("An attempt was made to write to a read-only matrix in {}.", LogUtils.methodAtDepth(3));
			throw new UnsupportedOperationException("The values of a read-only matrix can not be set.");
		}
		if (row < 0 || row >= rows || column < 0 || column >= columns) {
			log.warn("An attempt was made to write a value to a matrix with invalid indices" +
					 "in {}: [{}, {}] index is out of bounds. Matrix size is [{}, {}]",
					 LogUtils.methodAtDepth(3), row, column, rows, columns);
			throw new IndexOutOfBoundsException(String.format("Invalid index: [%d, %d]. The index does not fall within the size of the matrix [%d, %d].",
															  row, column, rows, columns));
		}

		this.matrix[row][column] = value;
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

		final int rows = this.getRows();
		final int columns = this.getColumns();

		if (row < 0 || row >= rows || column < 0 || column >= columns) {
			log.warn("An attempt was made to read a value from a matrix with invalid indices" +
					 "in {}: [{}, {}] index is out of bounds. Matrix size is [{}, {}]",
					 LogUtils.methodAtDepth(4), row, column, rows, columns);
			throw new IndexOutOfBoundsException(String.format("Invalid index: [%d, %d]. The index does not fall within the size of the matrix [%d, %d].",
															  row, column, rows, columns));
		}

		double value = this.matrix[row][column];
		log.trace("Retrieved element {} from matrix[{}][{}].", value, row, column);
		return value;

	}

	/**
	 * Retrieves the number of rows in the matrix.
	 *
	 * @return		the number of rows
	 */
	public int getRows() {

		return this.rows;

	}

	/**
	 * Retrieves the number of columns in the matrix.
	 *
	 * @return		the number of columns
	 */
	public int getColumns() {

		return this.columns;
	}

	/**
	 * Sets the name of the matrix to the given string.
	 *
	 * @param name		the name of the matrix
	 */
	public void setName(String name) {

	    this.name = Objects.requireNonNull(name, "Matrix name cannot be null.");

	}

	/**
	 * Returns a string containing the name of the matrix.
	 *
	 * @return		the name of the matrix
	 */
	public String getName() {

		return this.name;

	}

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
	public boolean isReadOnly() {

		return this.readonly;

	}

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
	public boolean isSavedToDisk() {

		return this.savedToDisk;

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

	    if (this.readonly) {
	        log.warn("Attempt to set matrix data on read-only matrix {}.", this.name);
	        throw new UnsupportedOperationException("Cannot modify a read-only matrix.");
	    }

		Objects.requireNonNull(source, "Source array must not be null.");

	    if (source.length == 0 || source[0].length == 0) {
	        throw new IllegalArgumentException("Matrix must have at least one row and one column.");
	    }

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

		return MatrixUtils.toShortString(this);

	}

	/**
	 * Returns a pretty representation of matrix values.
	 *
	 * @return		a pretty representation of the matrix
	 */
	public String toPrettyString() {

		return MatrixUtils.toPrettyString(this);

	}

	/**
	 * Tests if the matrix is empty (either 0 x 0, 0 x n, or n x 0).
	 *
	 * @return		{@code true} if {@code rows == 0 || columns == 0}, otherwise {@code false}
	 */
	@JsonIgnore
	public boolean isEmpty() {

		if (this.rows == 0 || this.columns == 0) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Tests if the matrix is square (n x n).
	 *
	 * @return		{@code true} if the matrix is square, {@code false} otherwise
	 */
	@JsonIgnore
	public boolean isSquare() {

		if (this.rows == this.columns) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Tests if the matrix is invertible {@code det(matrix) != 0}.
	 *
	 * @return		{@code true} if the matrix is invertible, {@code false} otherwise
	 */
	@JsonIgnore
	public boolean isInvertible() {

		final double det = determinant();
		final double maxValue = getMaxValue();

		if (!nearlyZero(det, maxValue)) {
			return true;
		} else {
			return false;
		}

	}

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
				if (bucketIndex(this.matrix[i][j]) != bucketIndex(matrix.matrix[i][j])) {
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
	 * @implNote 		Normalizing -0.0 to +0.0 and using the IEEE 754 bit layout in
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
	            long bits = (this.matrix[i][j] == 0.0 ? 0 : Double.doubleToLongBits(this.matrix[i][j]));
	            int elementHash = (int) (bits ^ (bits >>> 32));
				seed = (seed * 31) + elementHash;
			}
		}
		return (int) (seed ^ (seed >>> 32));

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
	public long bucketIndex(double value) {

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

		final int rows = this.getRows();
		final int columns = this.getColumns();

		Objects.requireNonNull(addend, "Addend must not be null.");

		if (this.isEmpty() || addend.isEmpty()) {
			throw new IllegalArgumentException("Cannot add empty matrices.");
		}
		if ((this.getRows() != addend.getRows()) || (this.getColumns() != addend.getColumns())) {
			throw new IllegalArgumentException(
				String.format("Matrix addends must be the same size: matrix %s is [%d, %d], matrix %s is [%d, %d].",
					this.getName(), this.getRows(), this.getColumns(),
					addend.getName(), addend.getRows(), addend.getColumns()));
		}

		double[][] elements = new double[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				elements[i][j] = this.getValue(i, j) + addend.getValue(i, j);
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

		final int rows = this.getRows();
		final int columns = this.getColumns();

		Objects.requireNonNull(subtrahend, "Subtrahend must not be null.");

		if (this.isEmpty() || subtrahend.isEmpty()) {
			throw new IllegalArgumentException("Cannot subtract empty matrices.");
		}
		if ((this.getRows() != subtrahend.getRows()) || (this.getColumns() != subtrahend.getColumns())) {
			throw new IllegalArgumentException(
				String.format("Matrices must be the same size: matrix %s is [%d, %d], matrix %s is [%d, %d].",
					this.getName(), this.getRows(), this.getColumns(),
					subtrahend.getName(), subtrahend.getRows(), subtrahend.getColumns()));
		}

		double[][] elements = new double[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				elements[i][j] = this.getValue(i, j) - subtrahend.getValue(i, j);
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

		final int rows = this.getRows();
		final int columns = factor.getColumns();
		final int shared = this.getColumns(); // == factor.getRows()

		Objects.requireNonNull(factor, "Factor must not be null.");

		if (this.isEmpty() || factor.isEmpty()) {
			throw new IllegalArgumentException("Cannot multiply empty matrices.");
		}
		if (this.getColumns() != factor.getRows()) {
			throw new IllegalArgumentException(
				String.format("The columns of the first factor and the rows of the second factor are not equal: " +
					"matrix %s is [%d, %d], matrix %s is [%d, %d].",
					this.getName(), this.getRows(), this.getColumns(),
					factor.getName(), factor.getRows(), factor.getColumns()));
		}

		double[][] elements = new double[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				double sum = 0.0;
				for (int k = 0; k < shared; k++) {
					sum += this.getValue(i, k) * factor.getValue(k, j);

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

		final int rows = this.getRows();
		final int columns = this.columns;

		if (this.isEmpty()) {
			throw new IllegalArgumentException("Cannot multiply an empty matrix by a scalar.");
		}

		double[][] elements = new double[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				elements[i][j] = this.getValue(i, j) * scalar;
			}
		}

		Matrix product = new Matrix(elements, this.getName() + "*" + scalar);
		log.debug("Multiplied matrix {} by the scalar {} to get matrix {}.", this, scalar, product);
		return product;

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
	private int sign(int row, int column) {

		return ((row + column) % 2) == 0 ? 1 : -1;

	}

	/**
	 * Calculates the cofactor of the matrix element at {@code [row, column]}.
	 *
	 * @param row			the row to be removed
	 * @param column		the column to be removed
	 * @return				the cofactor of the matrix element
	 */
	private double cofactor(int row, int column) {

		Matrix minorMatrix = minorMatrix(row, column);
		double minor = calculateDeterminantRecursively(minorMatrix);
		double cofactor = sign(row, column) * minor;

		return cofactor;

	}

	/**
	 * Calculates the matrix of cofactors for the receiver matrix.
	 *
	 * @return		the matrix of cofactors
	 */
	public Matrix cofactors() {

		LogUtils.logMethodEntry(log);

		final int rows = this.getRows();
		final int columns = this.getColumns();

		Matrix cofactors = new Matrix(rows, columns, "C(" + this.getName() + ")", false);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				double cof = cofactor(i, j);
				cofactors.setValue(i, j, cof);
			}
		}

		log.debug("Cofactor matrix {} calculated from matrix {}.", cofactors, this);
		return cofactors;

	}

	/**
	 * Generates a matrix which is the minor matrix of the receiver matrix at {@code [row, column]}.
	 * <p>
	 * The minor matrix is constructed by taking the receiver matrix and removing {@code row} and {@code column}.
	 * </p>
	 *
	 * @param row			the row to remove
	 * @param column		the column to remove
	 * @return				the minor matrix
	 */
	public Matrix minorMatrix(int row, int column) {

		LogUtils.logMethodEntry(log);

		final int rows = this.getRows();
		final int columns = this.getColumns();

	    if (row < 0 || row >= rows) {
	        throw new IndexOutOfBoundsException("row " + row + " out of bounds [0..." + (rows - 1) + "]");
	    }
	    if (column < 0 || column >= columns) {
	        throw new IndexOutOfBoundsException("column " + column + " out of bounds [0..." + (columns - 1) + "]");
	    }
	    if (rows <= 1 || columns <= 1) {
	        throw new IllegalArgumentException("Minor is undefined for matrices smaller than 2 x 2.");
	    }

		Matrix minorMatrix = new Matrix(rows - 1, columns - 1, "MINOR(" + this.getName() + ")", false);

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
	            minorMatrix.setValue(newRow, newColumn, this.getValue(i, j));
	            newColumn++;
	        }
	        newRow++;
	    }

	    log.debug("Minor matrix {} calculated from {} at [{}, {}]", minorMatrix, this, row, column);
	    return minorMatrix;

	}

	/**
	 * Calculates the determinant of a matrix recursively.
	 * <p>
	 * {@code det(A) = Sum(j = 1...n) ((-1)^(1 + j)) * a(1, j) * minor(1, j)}
	 * </p>
	 *
	 * @param matrix		the matrix for which to find the determinant
	 * @return				the calculated determinant
	 */
	private double calculateDeterminantRecursively(Matrix matrix) {

		final int rows = matrix.getRows();
		final int columns = matrix.getColumns();

		if (rows == 1) {
			return matrix.getValue(0, 0);
		}
		if (rows == 2) {
			return (matrix.getValue(0, 0) * matrix.getValue(1, 1)) - (matrix.getValue(0, 1) * matrix.getValue(1, 0));
		}

		double det = 0.0;
		for (int j = 0; j < columns; j++) {
			Matrix minorMatrix = matrix.minorMatrix(0, j);
			det += sign(0, j) * matrix.getValue(0, j) * calculateDeterminantRecursively(minorMatrix);
		}

		return det;

	}

	/**
	 * Calls calculateDeterminantRecursively() after performing some checks.
	 *
	 * @return			the calculated determinant
	 */
	public double determinant() {

		LogUtils.logMethodEntry(log);

		if (!this.isSquare()) {
			throw new IllegalArgumentException("Determinant is undefined for non-square matrices.");
		}
		if (this.isEmpty()) {
			throw new IllegalArgumentException("Determinant is undefined for empty matrices.");
		}

		double det = calculateDeterminantRecursively(this);
		log.debug("Determinant of {} found for matrix {}.", det, this);
		return det;

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

		Matrix result = new Matrix(columns, rows, "T(" + this.getName() + ")", false);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				result.setValue(j, i, this.getValue(i, j));
			}
		}

		log.debug("Matrix {} transposed to {}.", this, result);
		return result;

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
	 * Mutates the array parameter such that two rows of values are swapped
	 *
	 * @param source		the array to be changed
	 * @param rowFrom		the source row
	 * @param rowTo			the destination row
	 */
	private static void swapRows(double[][] source, int rowFrom, int rowTo) {

		double[] temp;

		temp = source[rowTo];
		source[rowTo] = source[rowFrom];
		source[rowFrom] = temp;

	}

	/**
	 * Determines whether {@code value} is close enough to zero to effectively be zero.
	 * <p>
	 * A scale parameter is specified to determine if the value is close enough to zero
	 * for a particular operation.
	 * </p>
	 *
	 * @param value			the value to test
	 * @param scale			a scale factor
	 * @return				{@code true} if the value is nearly zero, {@code false} otherwise
	 */
	private static boolean nearlyZero(double value, double scale) {

	    return Math.abs(value) <= ABSOLUTE_EPSILON + (RELATIVE_EPSILON * scale);

	}

	/**
	 * Finds the largest element in the receiver Matrix.
	 *
	 * @return		the largest element
	 */
	private double getMaxValue() {

		LogUtils.logMethodEntry(log);

		final int rows = this.getRows();
		final int columns = this.getColumns();
		double maxValue = 0.0;

	    for (int i = 0; i < rows; i++) {
	    	for (int j = 0; j < columns; j++) {
	    		maxValue = Math.max(maxValue, Math.abs(this.getValue(i, j)));
	    	}
	    }

	    log.debug("Found the maximum value of {} from matrix {}.", maxValue, this);
	    return maxValue;

	}

	/**
	 * Calculates the row echelon form (REF) of the receiver Matrix and returns
	 * the REF as a new Matrix object.
	 *
	 * @return		the REF of the matrix
	 * @implNote	Algorithm and refinements provided by ChatGPT (OpenAI)
	 */
	public Matrix rowEchelonForm() {

		LogUtils.logMethodEntry(log);

		final int rows = this.getRows();
		final int columns = this.getColumns();
		int nextRow = 0;
		double[][] newValues = toArrayCopy();

		// Get a good scale value from the matrix
		double maxValue = getMaxValue();
	    if (maxValue == 0.0) {
	    	return new Matrix(newValues, "REF(" + getName() + ")");
	    }

	    // Walk left to right over the columns placing pivots top-down
		for (int j = 0; j < columns; j++) {
			if (nextRow >= rows) {
				break;
			}
			// Choose a pivot row such that the pivot contains the largest value in the column
			int pivotRow = -1;
			double bestAbs = 0.0;
			for (int i = nextRow; i < rows; i++) {
				double valueAbs = Math.abs(newValues[i][j]);
				if (valueAbs > bestAbs) {
					bestAbs = valueAbs;
					pivotRow = i;
				}
			}
			// If there is no useable pivot in this column
			if (pivotRow == -1 || nearlyZero(bestAbs, maxValue)) {
				continue;
			}
			// Perform partial pivoting by swapping the pivot row up - this avoids round-off
			// errors with the floating-point arithmetic
			if (pivotRow != nextRow) {
				swapRows(newValues, pivotRow, nextRow);
			}
			// Eliminate the values below the pivot
			double pivot = newValues[nextRow][j];
			for (int i = nextRow + 1; i < rows; i++) {
				// Skip processing the row if the element is very close to 0.0
	            if (nearlyZero(newValues[i][j], bestAbs)) {
	            	continue;
	            }
	            double factor = newValues[i][j] / pivot;
	            for (int k = j; k < columns; k++) {
	                double updated = newValues[i][k] - (factor * newValues[nextRow][k]);
	                // Clamp to 0.0 if the value of updated is very close to 0.0
	                if (nearlyZero(updated, maxValue)) {
	                	updated = 0.0;
	                }
		            newValues[i][k] = updated;
	            }
	        }
			nextRow++;
		}

		Matrix ref = new Matrix(newValues, "REF(" + this.getName() + ")");
		log.debug("Calculated REF matrix {} from matrix {}.", ref, this);
		return ref;

	}

	/**
	 * Calculates the reduced row echelon form (RREF) of the receiver Matrix and returns
	 * the RREF as a new Matrix object.
	 *
	 * @return		the RREF of the matrix
	 * @implNote	Algorithm and refinements provided by ChatGPT (OpenAI)
	 */
	public Matrix reducedRowEchelonForm() {

		LogUtils.logMethodEntry(log);

		// Start with a Matrix in row echelon form
		Matrix ref = this.rowEchelonForm();
		final int rows = ref.getRows();
		final int columns = ref.getColumns();
		double[][] newValues = ref.getMatrix();

		// Get a good scale value from the row echelon form of the matrix
		double maxValue = ref.getMaxValue();
	    if (maxValue == 0.0) {
	    	return new Matrix(newValues, "RREF(" + getName() + ")");
	    }

	    // Build a list of pivots (first non-zero element in each row)
		record Pivot(int row, int column) {};
		List<Pivot> pivots = new ArrayList<>();
	    for (int i = 0; i < rows; i++) {
	    	int tempColumn = -1;
	    	for (int j = 0; j < columns; j++) {
	    		if (!nearlyZero(newValues[i][j], maxValue)) {
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
	    	double pivot = newValues[row][column];
	    	if (!nearlyZero(pivot, maxValue) && Math.abs(pivot - 1.0) > ABSOLUTE_EPSILON) {
	    		for (int j = column; j < columns; j++) {
	    			double factor = newValues[row][j] / pivot;
	    			// Clamp value to 0.0 in case of very small round-off errors
	    			newValues[row][j] = nearlyZero(factor, maxValue) ? 0.0 : factor;
	    		}
	    	}
	    	// Clamp value to 1.0 in case of very small round-off errors
	    	newValues[row][column] = 1.0;
	    	// Eliminate above so that the elements above the pivot are 0
	    	for (int i = 0; i < row; i++) {
	    		double factor = newValues[i][column];
	    		if (nearlyZero(factor, maxValue)) {
	    			continue;
	    		}
	    		for (int j = column; j < columns; j++) {
	    			double updated = newValues[i][j] - factor * newValues[row][j];
	    			newValues[i][j] = nearlyZero(updated, maxValue) ? 0.0 : updated;
	    		}
	    	}
	    }

		Matrix rref = new Matrix(newValues, "RREF(" + this.getName() + ")");
		log.debug("Calculated RREF matrix {} from matrix {}.", rref, this);
		return rref;

	}

}
