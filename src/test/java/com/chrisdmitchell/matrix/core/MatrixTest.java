package com.chrisdmitchell.matrix.core;

import org.junit.jupiter.api.Test;

import com.chrisdmitchell.matrix.model.Matrix;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.DoubleUnaryOperator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static com.chrisdmitchell.matrix.util.Constants.Formatting.*;
import static com.chrisdmitchell.matrix.util.Constants.Numeric.*;

public class MatrixTest {
	
	private static void assertMatrixEqualsExact(Matrix expected, Matrix actual) {

	    int expectedRows = expected.getRows();
	    int expectedCols = expected.getColumns();

		assertEquals(expectedRows, actual.getRows(), "row count");
		assertEquals(expectedCols, actual.getColumns(), "column count");

		int errors = 0;
		StringBuilder errorReport = new StringBuilder();
		
        for (int i = 0; i < expectedRows; i++) {
            for (int j = 0; j < expectedCols; j++) {
                double expectedValue = expected.getValue(i, j);
                double actualValue = actual.getValue(i, j);
                if (Double.compare(expectedValue, actualValue) != 0) {
                	if (errors == 0) errorReport.append("Matrix inequalities:\n");
                	errorReport.append(String.format("At [%d, %d]: expected = %s, actual = %s%n",
                									 i, j,
                									 MATRIX_FORMAT.format(expectedValue),
                									 MATRIX_FORMAT.format(actualValue)));
                	errors++;
                }
            }
        }
        if (errors > 0) fail(errorReport.toString());
	
	}
	
	private static void assertMatrixEqualsExact(double[][] expected, Matrix actual) {

		assertMatrixEqualsExact(new Matrix (expected, "EXPECTED"), actual);
	
	}

	private static void assertMatrixEqualsApprox(Matrix expected, Matrix actual) {
		
	    int expectedRows = expected.getRows();
	    int expectedCols = expected.getColumns();
	    
		assertEquals(expectedRows, actual.getRows(), "row count");
		assertEquals(expectedCols, actual.getColumns(), "column count");
		
		int errors = 0;
		StringBuilder errorReport = new StringBuilder();
		
        for (int i = 0; i < expectedRows; i++) {
            for (int j = 0; j < expectedCols; j++) {
                double expectedValue = expected.getValue(i, j);
                double actualValue = actual.getValue(i, j);
                double difference = Math.abs(expectedValue - actualValue);
                if (difference > EPSILON) {
                	if (errors == 0) errorReport.append("Matrix inequalities (±").append(EPSILON).append("):\n");
                	errorReport.append(String.format("At [%d, %d]: expected = %s, actual = %s, |Δ| = %s%n",
							 i, j,
							 MATRIX_FORMAT.format(expectedValue),
							 MATRIX_FORMAT.format(actualValue),
							 MATRIX_FORMAT.format(difference)));
                	errors++;
                }
            }
        }
        if (errors > 0) fail(errorReport.toString());
        
	}
/*	
	private static void assertMatrixEqualsApprox(double[][] expected, Matrix actual) {

		assertMatrixEqualsApprox(new Matrix (expected, "EXPECTED"), actual);
	
	}
*/
	
	@Nested
	@DisplayName("Matrix.add()")
	class AddTests {
		
		Matrix twoByTwoSequential, twoByTwoOther, twoByTwoSum;
		Matrix threeByThreeSequential, threeByThreeOther, threeByThreeSum;
		
		@BeforeEach
		void setUp() {
			
			twoByTwoSequential = new Matrix(new double[][] {{1, 2},{3, 4}});
			twoByTwoOther = new Matrix(new double[][] {{5, 6},{7, 8}});
			twoByTwoSum = new Matrix(new double[][] {{6, 8},{10, 12}});

			threeByThreeSequential = new Matrix(new double[][] {{1, 2, 3},{4, 5, 6},{7, 8, 9}});
			threeByThreeOther = new Matrix(new double[][] {{10, 11, 12},{13, 14, 15},{16, 17, 18}});
			threeByThreeSum = new Matrix(new double[][] {{11, 13, 15},{17, 19, 21},{23, 25, 27}});
					
		}
		
		@Test
		@DisplayName("adds two 2 x 2 matrices")
		void add2x2() {
			
			Matrix sum = twoByTwoSequential.add(twoByTwoOther);
			
		    assertMatrixEqualsExact(twoByTwoSum, sum);
			
		}
		
		@Test
		@DisplayName("adds two 3 x 3 matrices")
		void add3x3() {
	
		    Matrix sum = threeByThreeSequential.add(threeByThreeOther);
	
		    assertMatrixEqualsExact(threeByThreeSum, sum);
			
		}
		
		@Test
		@DisplayName("throws on different sizes")
		void addThrows() {

			try {
		        twoByTwoSequential.add(threeByThreeSequential);
		        fail("Expected IllegalArgumentException to be thrown");
		    } catch (IllegalArgumentException e) {
		        // Expected
		    }
			
		}
		
		@Test
		@DisplayName("checks whether the operands are changed")
		void addChanged() {

			twoByTwoSequential.add(twoByTwoOther);
	
		    assertMatrixEqualsExact(new double[][] {{1, 2},{3, 4}}, twoByTwoSequential);
		    assertMatrixEqualsExact(new double[][] {{5, 6},{7, 8}}, twoByTwoOther);

		}
	}
	
	@Nested
	@DisplayName("Matrix.subtract()")
	class SubtractTests {
		
		Matrix twoByTwoSequential, twoByTwoOther, twoByTwoDifference;
		Matrix threeByThreeSequential, threeByThreeOther, threeByThreeDifference;
		
		@BeforeEach
		void setUp() {
			
			twoByTwoSequential = new Matrix(new double[][] {{1, 2},{3, 4}});
			twoByTwoOther = new Matrix(new double[][] {{5, 6},{7, 8}});
			twoByTwoDifference = new Matrix(new double[][] {{6, 8},{10, 12}});

			threeByThreeSequential = new Matrix(new double[][] {{1, 2, 3},{4, 5, 6},{7, 8, 9}});
			threeByThreeOther = new Matrix(new double[][] {{10, 11, 12},{13, 14, 15},{16, 17, 18}});
			threeByThreeDifference = new Matrix(new double[][] {{11, 13, 15},{17, 19, 21},{23, 25, 27}});
					
		}
		
		@Test
		@DisplayName("subtracts two 2 x 2 matrices")
		void subtract2x2() {
			
			Matrix difference = twoByTwoDifference.subtract(twoByTwoSequential);
			
			assertMatrixEqualsExact(twoByTwoOther, difference);
			
		}
		
		@Test
		@DisplayName("subtracts two 3 x 3 matrices")
		void subtract3x3() {
			
			Matrix difference = threeByThreeDifference.subtract(threeByThreeSequential);
			
			assertMatrixEqualsExact(threeByThreeOther, difference);
			
		}
		
		@Test
		@DisplayName("throws on different sizes")
		void subtractThrows() {

			try {
		        twoByTwoSequential.subtract(threeByThreeSequential);
		        fail("Expected IllegalArgumentException to be thrown");
		    } catch (IllegalArgumentException e) {
		        // Expected
		    }
			
		}
		
		@Test
		@DisplayName("checks whether the operands are changed")
		void subtractChanged() {

			twoByTwoDifference.subtract(twoByTwoSequential);
			
			assertMatrixEqualsExact(new double[][] {{6, 8},{10, 12}}, twoByTwoDifference);
		    assertMatrixEqualsExact(new double[][] {{1, 2},{3, 4}}, twoByTwoSequential);

		}
		
	}
	
	@Nested
	@DisplayName("Matrix.multiply()")
	class MultiplyTests {
		
		Matrix twoByTwoSequential, twoByTwoOther, twoByTwoProduct;
		Matrix threeByThreeSequential, threeByThreeOther, threeByThreeProduct;
		double scalarTwo, scalarFour;
		Matrix twoByTwoScalarProduct, threeByThreeScalarProduct;
		
		@BeforeEach
		void setUp() {
			
			twoByTwoSequential = new Matrix(new double[][] {{1, 2},{3, 4}});
			twoByTwoOther = new Matrix(new double[][] {{5, 6},{7, 8}});
			twoByTwoProduct = new Matrix(new double[][] {{19, 22},{43, 50}});
			scalarTwo = 2.0;
			twoByTwoScalarProduct = new Matrix(new double[][] {{2, 4},{6, 8}});

			threeByThreeSequential = new Matrix(new double[][] {{1, 2, 3},{4, 5, 6},{7, 8, 9}});
			threeByThreeOther = new Matrix(new double[][] {{10, 11, 12},{13, 14, 15},{16, 17, 18}});
			threeByThreeProduct = new Matrix(new double[][] {{84, 90, 96},{201, 216, 231},{318, 342, 366}});
			scalarFour = 4.0;
			threeByThreeScalarProduct = new Matrix(new double[][] {{4, 8, 12},{16, 20, 24},{28, 32, 36}});
					
		}
		
		@Test
		@DisplayName("multiplies two 2 x 2 matrices")
		void multiply2x2() {
			
			Matrix product = twoByTwoSequential.multiply(twoByTwoOther);
			
			assertMatrixEqualsExact(twoByTwoProduct, product);
			
		}
		
		@Test
		@DisplayName("multiplies two 3 x 3 matrices")
		void multiply3x3() {
			
			Matrix product = threeByThreeSequential.multiply(threeByThreeOther);
			
			assertMatrixEqualsExact(threeByThreeProduct, product);

		}

		@Test
		@DisplayName("multiplies a 2 x 2 matrix with a scalar")
		void multiply2x2Scalar() {
			
			Matrix product = twoByTwoSequential.multiply(scalarTwo);
			
			assertMatrixEqualsExact(twoByTwoScalarProduct, product);
			
		}
		
		@Test
		@DisplayName("multiplies a 3 x 3 matrix with a scalar")
		void multiply3x3Scalar() {
			
			Matrix product = threeByThreeSequential.multiply(scalarFour);
			
			assertMatrixEqualsExact(threeByThreeScalarProduct, product);
			
		}
		
		@Test
		@DisplayName("throws on different sizes")
		void multipliesThrows() {

			try {
		        twoByTwoSequential.multiply(threeByThreeSequential);
		        fail("Expected IllegalArgumentException to be thrown");
		    } catch (IllegalArgumentException e) {
		        // Expected
		    }
			
		}
		
		@Test
		@DisplayName("checks whether the operands are changed")
		void multiplyChanged() {

			twoByTwoSequential.multiply(twoByTwoOther);
			
			assertMatrixEqualsExact(new double[][] {{1, 2},{3, 4}}, twoByTwoSequential);
		    assertMatrixEqualsExact(new double[][] {{5, 6},{7, 8}}, twoByTwoOther);

		}
		
	}
	
	@Nested
	@DisplayName("Matrix.minorMatrix()")
	class MinorMatrixTests {
		
		Matrix threeByThreeSequential, twoByTwoMinorr0c0, twoByTwoMinorr1c1, twoByTwoMinorr2c2;
		
		@BeforeEach
		void setUp() {
			
			threeByThreeSequential = new Matrix(new double[][] {{1, 2, 3},{4, 5, 6},{7, 8, 9}});
			twoByTwoMinorr0c0 = new Matrix(new double[][] {{5, 6},{8, 9}});
			twoByTwoMinorr1c1 = new Matrix(new double[][] {{1, 3},{7, 9}});
			twoByTwoMinorr2c2 = new Matrix(new double[][] {{1, 2},{4, 5}});
					
		}
		
		@Test
		@DisplayName("creates a minor 2 x 2 matrix from a 3 x 3 matrix at [0, 0]")
		void minor2x2From3x3r0c0() {

			Matrix minorMatrix = threeByThreeSequential.minorMatrix(0, 0);
			
			assertMatrixEqualsExact(twoByTwoMinorr0c0, minorMatrix);

		}

		@Test
		@DisplayName("creates a minor 2 x 2 matrix from a 3 x 3 matrix at [1, 1]")
		void minor2x2From3x3r1c1() {

			Matrix minorMatrix = threeByThreeSequential.minorMatrix(1, 1);
			
			assertMatrixEqualsExact(twoByTwoMinorr1c1, minorMatrix);

		}

		@Test
		@DisplayName("creates a minor 2 x 2 matrix from a 3 x 3 matrix at [2, 2]")
		void minor2x2From3x3r2c2() {

			Matrix minorMatrix = threeByThreeSequential.minorMatrix(2, 2);
			
			assertMatrixEqualsExact(twoByTwoMinorr2c2, minorMatrix);

		}
	}
	
	@Nested
	@DisplayName("Matrix.deteminant()")
	class DetTests {

		Matrix twoByTwoSequential, twoByTwoOther;
		double twoSeqDet, twoOthDet;
		Matrix threeByThreeSequential, threeByThreeOther;
		double threeSeqDet, threeOthDet;
		
		@BeforeEach
		void setUp() {

			twoByTwoSequential = new Matrix(new double[][] {{1, 2},{3, 4}});
			twoSeqDet = -2.0;
			twoByTwoOther = new Matrix(new double[][] {{8, -1},{5, 6}});
			twoOthDet = 53.0;
			threeByThreeSequential = new Matrix(new double[][] {{1, 2, 3},{4, 5, 6},{7, 8, 9}});
			threeSeqDet = 0.0;
			threeByThreeOther = new Matrix(new double[][] {{1, 8, -3},{5, 0, 7},{-4, 9, 2}});
			threeOthDet = -502.0;
			
		}
		
		@Test
		@DisplayName("finds the determinant of a 2 x 2 matrix")
		void twoDet1() {
			
			double det = twoByTwoSequential.determinant();
			
			assertEquals(twoSeqDet, det);
			
		}

		@Test
		@DisplayName("finds the determinant of another 2 x 2 matrix")
		void twoDet2() {
			
			double det = twoByTwoOther.determinant();
			
			assertEquals(twoOthDet, det);
			
		}
		
		@Test
		@DisplayName("finds the determinant of a 3 x 3 matrix")
		void threeDet1() {
			
			double det = threeByThreeSequential.determinant();
			
			assertEquals(threeSeqDet, det);
			
		}
		
		@Test
		@DisplayName("finds the determinant of another 2 x 2 matrix")
		void threeDet2() {
			
			double det = threeByThreeOther.determinant();
			
			assertEquals(threeOthDet, det);
			
		}
	}
	
	@Nested
	@DisplayName("Matrix.cofactors()")
	class CofactorsMatrixTest {
		
		Matrix twoByTwoSequential, twoByTwoCofactors;
		Matrix threeByThreeSequential, threeByThreeCofactors;
		
		@BeforeEach
		void setUp() {
			
			twoByTwoSequential = new Matrix(new double[][] {{1, 2},{3, 4}});
			twoByTwoCofactors = new Matrix(new double[][] {{4, -3},{-2, 1}});
			threeByThreeSequential = new Matrix(new double[][] {{1, 2, 3},{4, 5, 6},{7, 8, 9}});
			threeByThreeCofactors = new Matrix(new double[][] {{-3, 6, -3},{6, -12, 6},{-3, 6, -3}});
					
		}

		@Test
		@DisplayName("creates a 2 x 2 matrix of cofactors for a 2 x 2 matrix")
		void cof2x2() {
			
			Matrix cofactors = twoByTwoSequential.cofactors();
			
			assertMatrixEqualsExact(twoByTwoCofactors, cofactors);
			
		}
		
		@Test
		@DisplayName("creates a 3 x 3 matrix of cofactors for a 3 x 3 matrix")
		void cof3x3() {
			
			Matrix cofactors = threeByThreeSequential.cofactors();
			
			assertMatrixEqualsExact(threeByThreeCofactors, cofactors);
			
		}
	}
	
	@Nested
	@DisplayName("Matrix.transpose()")
	class TransposeTests {
		
		Matrix twoByTwoSequential, twoByTwoTranspose;
		Matrix threeByThreeSequential, threeByThreeTranspose;
		
		@BeforeEach
		void setUp() {
			
			twoByTwoSequential = new Matrix(new double[][] {{1, 2},{3, 4}});
			twoByTwoTranspose = new Matrix(new double[][] {{1, 3},{2, 4}});
			threeByThreeSequential = new Matrix(new double[][] {{1, 2, 3},{4, 5, 6},{7, 8, 9}});
			threeByThreeTranspose = new Matrix(new double[][] {{1, 4, 7},{2, 5, 8},{3, 6, 9}});
					
		}
		
		@Test
		@DisplayName("transpose of a 2 x 2 matrix")
		void trans2x2() {
			
			Matrix transpose = twoByTwoSequential.transpose();
			
			assertMatrixEqualsExact(twoByTwoTranspose, transpose);
			
		}
		
		@Test
		@DisplayName("transpose of a 3 x 3 matrix")
		void trans3x3() {
			
			Matrix transpose = threeByThreeSequential.transpose();
			
			assertMatrixEqualsExact(threeByThreeTranspose, transpose);
			
		}
	}
	
	@Nested
	@DisplayName("Matrix.inverse()")
	class InverseTests {
		
		Matrix twoByTwoSequential, twoByTwoInverse;
		Matrix threeByThreeSequential, threeByThreeInverse, threeByThreeSingular;
		
		@BeforeEach
		void setUp() {
			
			twoByTwoSequential = new Matrix(new double[][] {{1, 2},{3, 4}});
			twoByTwoInverse = new Matrix(new double[][] {{-2, 1},{1.5, -0.5}});
			threeByThreeSequential = new Matrix(new double[][] {{1, -2, 3},{-4, 5, 6},{7, 8, -9}});
			threeByThreeInverse = new Matrix(new double[][] {{0.3039215686, -0.0196078431, 0.0882352941},
															 {-0.0196078431, 0.0980392157, 0.0588235294},
															 {0.2189542484, 0.0718954248, 0.0098039216}});
			threeByThreeSingular = new Matrix(new double[][] {{1, 2, 3},{4, 5, 6},{7, 8, 9}});
			
		}
		
		@Test
		@DisplayName("inverse of a 2 x 2 matrix")
		void inv2x2() {
			
			Matrix inverse = twoByTwoSequential.inverse();
			
			assertMatrixEqualsApprox(twoByTwoInverse, inverse);
			
		}
		
		@Test
		@DisplayName("inverse of a 3 x 3 matrix")
		void inv3x3() {

			Matrix inverse = threeByThreeSequential.inverse();
			
			assertMatrixEqualsApprox(threeByThreeInverse, inverse);

		}
		
		@Test
		@DisplayName("throws on singular matrix")
		void invThrows() {

			try {
		        threeByThreeSingular.inverse();
		        fail("Expected IllegalArgumentException to be thrown");
		    } catch (IllegalArgumentException e) {
		        // Expected
		    }
			
		}
	}
	
	@Nested
	@DisplayName("Matrix.rowEchelonForm()")
	class REFTests {
		
		Matrix twoByTwoAugmented, twoByTwoREF;
		Matrix threeByThreeAugmented, threeByThreeREF;
		
		@BeforeEach
		void setUp() {
			
			twoByTwoAugmented = new Matrix(new double[][] {{1, 2, 3},{4, 5, 6}});
			twoByTwoREF = new Matrix(new double[][] {{4, 5, 6},{0, 0.75, 1.5}});
			threeByThreeAugmented = new Matrix(new double[][] {{1, -2, 3, 10},{-4, 5, 6, 11},{7, 8, -9, 12}});
			threeByThreeREF = new Matrix(new double[][] {{7, 8, -9, 12},
														 {0, 9.5714285714, 0.8571428571, 17.8571428571},
														 {0, 0, 4.5671641791, 14.1492537313}});
			
		}

		@Test
		@DisplayName("row echelon form of a 2 x 3 matrix")
		void ref2x2() {
			
			Matrix ref = twoByTwoAugmented.rowEchelonForm();
			
			assertMatrixEqualsApprox(twoByTwoREF, ref);
			
		}
		
		@Test
		@DisplayName("row echelon form of a 3 x 4 matrix")
		void ref3x3() {
			
			Matrix ref = threeByThreeAugmented.rowEchelonForm();

			assertMatrixEqualsApprox(threeByThreeREF, ref);

		}
	}
	
	@Nested
	@DisplayName("Matrix.reducedRowEchelonForm()")
	class RREFTests {

		Matrix twoByTwoAugmented, twoByTwoRREF;
		Matrix threeByThreeAugmented, threeByThreeRREF;
		
		@BeforeEach
		void setUp() {
			
			twoByTwoAugmented = new Matrix(new double[][] {{1, 2, 3},{4, 5, 6}});
			twoByTwoRREF = new Matrix(new double[][] {{1, 0, -1}, {0, 1, 2}});
			threeByThreeAugmented = new Matrix(new double[][] {{1, -2, 3, 10},{-4, 5, 6, 11},{7, 8, -9, 12}});
			threeByThreeRREF = new Matrix(new double[][] {{1, 0, 0, 3.8823529412},
														 {0, 1, 0, 1.5882352941},
														 {0, 0, 1, 3.0980392157}});
			
		}

		@Test
		@DisplayName("reduced row echelon form of a 2 x 3 matrix")
		void rref2x2() {
			
			Matrix rref = twoByTwoAugmented.reducedRowEchelonForm();
			
			assertMatrixEqualsApprox(twoByTwoRREF, rref);

		}
		
		@Test
		@DisplayName("reduced row echelon form of a 3 x 4 matrix")
		void rref3x3() {
			
			Matrix rref = threeByThreeAugmented.reducedRowEchelonForm();
			
			assertMatrixEqualsApprox(threeByThreeRREF, rref);

		}
	}
	
	@Nested
	@DisplayName("Matrix.rank() and Matrix.nullity()")
	class RankTests {
		
		Matrix twoByTwoSequential, threeByThreeSequential;
		int twoRank, twoNullity, threeRank, threeNullity;
		
		@BeforeEach
		void setUp() {
			
			twoByTwoSequential = new Matrix(new double[][] {{1, 2},{3, 4}});
			twoRank = 2;
			twoNullity = 0;
			threeByThreeSequential = new Matrix(new double[][] {{1, 2, 3},{4, 5, 6},{7, 8, 9}});
			threeRank = 2;
			threeNullity = 1;
			
		}
		
		@Test
		@DisplayName("rank and nullity of a 2 x 2 matrix")
		void rank2x2() {
			
			int rank = twoByTwoSequential.rank();
			int nullity = twoByTwoSequential.nullity();
			int columns = twoByTwoSequential.getColumns();
			
			assertEquals(twoRank, rank);
			assertEquals(twoNullity, nullity);
			assertEquals(rank + nullity, columns);
			
		}

		@Test
		@DisplayName("rank and nullity of a 3 x 3 matrix")
		void rank3x3() {
			
			int rank = threeByThreeSequential.rank();
			int nullity = threeByThreeSequential.nullity();
			int columns = threeByThreeSequential.getColumns();
			
			assertEquals(threeRank, rank);
			assertEquals(threeNullity, nullity);
			assertEquals(rank + nullity, columns);
			
		}
		
	}
	
	@Nested
	@DisplayName("Matrix.checkIndex()")
	class IndexTests {
		
		Matrix threeByThreeSequential;
		
		@BeforeEach
		void setUp() {
			
			threeByThreeSequential = new Matrix(new double[][] {{1, 2, 3},{4, 5, 6},{7, 8, 9}});
			
		}

		@Test
		@DisplayName("check for exception with indices [0, 0]")
		void indexr0c0() {
			
			try {
				threeByThreeSequential.getValue(0, 0);
		    } catch (IndexOutOfBoundsException e) {
		        fail("IndexOutOfBoundsException thrown");
		    }
			
		}
		
		@Test
		@DisplayName("check for exception with indices [rows - 1, columns - 1]")
		void indexrminus1cminus1() {
			
			try {
				threeByThreeSequential.getValue(threeByThreeSequential.getRows() - 1, threeByThreeSequential.getColumns() - 1);
		    } catch (IndexOutOfBoundsException e) {
		        fail("IndexOutOfBoundsException thrown");
		    }
			
		}
		
		@Test
		@DisplayName("check for exception with indices [-1, 0]")
		void indexrminus1c0() {

			try {
				threeByThreeSequential.getValue(-1, 0);
		        fail("Expected IndexOutOfBoundsException thrown");
			} catch (IndexOutOfBoundsException e) {
				// Expected
		    }
			
		}
		
		@Test
		@DisplayName("check for exception with indices [0, -1]")
		void indexr0cminus1() {

			try {
				threeByThreeSequential.getValue(0, -1);
		        fail("Expected IndexOutOfBoundsException thrown");
			} catch (IndexOutOfBoundsException e) {
				// Expected
		    }
			
		}
		
		@Test
		@DisplayName("check for exception with indices [rows, 0]")
		void indexrowsc0() {

			try {
				threeByThreeSequential.getValue(threeByThreeSequential.getRows(), 0);
		        fail("Expected IndexOutOfBoundsException thrown");
			} catch (IndexOutOfBoundsException e) {
				// Expected
		    }
			
		}
		
		@Test
		@DisplayName("check for exception with indices [0, columns]")
		void indexr0columns() {

			try {
				threeByThreeSequential.getValue(0, threeByThreeSequential.getColumns());
		        fail("Expected IndexOutOfBoundsException thrown");
			} catch (IndexOutOfBoundsException e) {
				// Expected
		    }
			
		}

	}
	
	@Nested
	@DisplayName("Matrix.trace()")
	class TraceTests {
		
		Matrix threeByThreeSequential, fourByFourSequential;
		double threeTrace, fourTrace;
		
		@BeforeEach
		void setUp() {
			
			threeByThreeSequential = new Matrix(new double[][] {{1, 2, 3},{4, 5, 6},{7, 8, 9}});
			threeTrace = 15;
			fourByFourSequential = new Matrix(new double[][] {{1, 2, 3, 4},{5, 6, 7, 8},{9, 10, 11, 12},{13, 14, 15, 16}});
			fourTrace = 34;
			
		}

		@Test
		@DisplayName("trace of a 3 x 3 matrix")
		void trace3x3() {
			
			double trace = threeByThreeSequential.trace();
			
			assertEquals(threeTrace, trace);

		}

		@Test
		@DisplayName("trace of a 4 x 4 matrix")
		void trace4x4() {
			
			double trace = fourByFourSequential.trace();
			
			assertEquals(fourTrace, trace);

		}

	}
	
	@Nested
	@DisplayName("Matrix.l1Norm()")
	class L1Tests {
		
		Matrix threeByThreeSequential, fourByFourSequential;
		double threeL1, fourL1;
		
		@BeforeEach
		void setUp() {
			
			threeByThreeSequential = new Matrix(new double[][] {{1, 2, 3},{4, 5, 6},{7, 8, 9}});
			threeL1 = 18;
			fourByFourSequential = new Matrix(new double[][] {{1, 2, 3, 4},{5, 6, 7, 8},{9, 10, 11, 12},{13, 14, 15, 16}});
			fourL1 = 40;
			
		}
		
		@Test
		@DisplayName("l1 norm of a 3 x 3 matrix")
		void l13x3() {
			
			double l1Norm = threeByThreeSequential.l1Norm();
			
			assertEquals(l1Norm, threeL1);
			
		}

		@Test
		@DisplayName("l1 norm of a 4 x 4 matrix")
		void l14x4() {
			
			double l1Norm = fourByFourSequential.l1Norm();
			
			assertEquals(l1Norm, fourL1);
			
		}
	}
	
	@Nested
	@DisplayName("Matrix.infinityNorm()")
	class InfinityTests {
		
		Matrix threeByThreeSequential, fourByFourSequential;
		double threeInfinity, fourInfinity;
		
		@BeforeEach
		void setUp() {
			
			threeByThreeSequential = new Matrix(new double[][] {{1, 2, 3},{4, 5, 6},{7, 8, 9}});
			threeInfinity = 24;
			fourByFourSequential = new Matrix(new double[][] {{1, 2, 3, 4},{5, 6, 7, 8},{9, 10, 11, 12},{13, 14, 15, 16}});
			fourInfinity = 58;
			
		}
		
		@Test
		@DisplayName("infinity norm of a 3 x 3 matrix")
		void infinity3x3() {
			
			double infinityNorm = threeByThreeSequential.infinityNorm();
			
			assertEquals(infinityNorm, threeInfinity);
			
		}

		@Test
		@DisplayName("infinity norm of a 4 x 4 matrix")
		void infinity4x4() {
			
			double infinityNorm = fourByFourSequential.infinityNorm();
			
			assertEquals(infinityNorm, fourInfinity);
			
		}
	}
	
	@Nested
	@DisplayName("Matrix.frobeniusNorm()")
	class FrobeniusTests {
		
		Matrix threeByThreeSequential, fourByFourSequential;
		double threeFrobenius, fourFrobenius;
		
		@BeforeEach
		void setUp() {
			
			threeByThreeSequential = new Matrix(new double[][] {{1, 2, 3},{4, 5, 6},{7, 8, 9}});
			threeFrobenius = 16.8819430161;
			fourByFourSequential = new Matrix(new double[][] {{1, 2, 3, 4},{5, 6, 7, 8},{9, 10, 11, 12},{13, 14, 15, 16}});
			fourFrobenius = 38.6781592116;
			
		}
		
		@Test
		@DisplayName("frobenius norm of a 3 x 3 matrix")
		void frobenius3x3() {
			
			double frobeniusNorm = threeByThreeSequential.frobeniusNorm();
			
			assertEquals(frobeniusNorm, threeFrobenius, EPSILON, "frobenius within epsilon");

		}

		@Test
		@DisplayName("frobenius norm of a 4 x 4 matrix")
		void frobenius4x4() {
			
			double frobeniusNorm = fourByFourSequential.frobeniusNorm();
			
			assertEquals(frobeniusNorm, fourFrobenius, EPSILON, "frobenius within epsilon");

		}

	}
	
	@Nested
	@DisplayName("Matrix.hadamard()")
	class HadamardTests {
		
		Matrix twoByTwoSequential, twoByTwoOther, twoByTwoHadamard;
		Matrix threeByThreeSequential, threeByThreeOther, threeByThreeHadamard;
		
		@BeforeEach
		void setUp() {
			
			twoByTwoSequential = new Matrix(new double[][] {{1, 2},{3, 4}});
			twoByTwoOther = new Matrix(new double[][] {{5, 6},{7, 8}});
			twoByTwoHadamard = new Matrix(new double[][] {{5, 12},{21, 32}});

			threeByThreeSequential = new Matrix(new double[][] {{1, 2, 3},{4, 5, 6},{7, 8, 9}});
			threeByThreeOther = new Matrix(new double[][] {{10, 11, 12},{13, 14, 15},{16, 17, 18}});
			threeByThreeHadamard = new Matrix(new double[][] {{10, 22, 36},{52, 70, 90},{112, 136, 162}});
					
		}
		
		@Test
		@DisplayName("hadamard product of two 2 x 2 matrices")
		void hadamard2x2() {
			
			Matrix hadamard = twoByTwoSequential.hadamard(twoByTwoOther);
			
			assertMatrixEqualsExact(twoByTwoHadamard, hadamard);
			
		}
		
		@Test
		@DisplayName("hadamard product of two 3 x 3 matrices")
		void hadamard3x3() {
			
			Matrix hadamard = threeByThreeSequential.hadamard(threeByThreeOther);
			
			assertMatrixEqualsExact(threeByThreeHadamard, hadamard);

		}
		
		@Test
		@DisplayName("throws on different sizes")
		void hadamardThrows() {

			try {
		        twoByTwoSequential.hadamard(threeByThreeSequential);
		        fail("Expected IllegalArgumentException to be thrown");
		    } catch (IllegalArgumentException e) {
		        // Expected
		    }
			
		}
		
		@Test
		@DisplayName("checks whether the operands are changed")
		void multiplyChanged() {

			twoByTwoSequential.hadamard(twoByTwoOther);
			
			assertMatrixEqualsExact(new double[][] {{1, 2},{3, 4}}, twoByTwoSequential);
		    assertMatrixEqualsExact(new double[][] {{5, 6},{7, 8}}, twoByTwoOther);

		}
		
	}
	
	@Nested
	@DisplayName("Matrix.kronecker()")
	class KroneckerTests {
		
		Matrix twoByTwoSequential, twoByTwoOther, fourByFourKronecker;
		Matrix threeByThreeSequential, sixBySixKronecker;
		
		@BeforeEach
		void setUp() {
			
			twoByTwoSequential = new Matrix(new double[][] {{1, 2},{3, 4}});
			twoByTwoOther = new Matrix(new double[][] {{5, 6},{7, 8}});
			fourByFourKronecker = new Matrix(new double[][] {{5, 6, 10, 12},
															 {7, 8, 14, 16},
															 {15, 18, 20, 24},
															 {21, 24, 28, 32}});

			threeByThreeSequential = new Matrix(new double[][] {{1, 2, 3},{4, 5, 6},{7, 8, 9}});
			sixBySixKronecker = new Matrix(new double[][] {{1, 2, 2, 4, 3, 6},
														   {3, 4, 6, 8, 9, 12},
														   {4, 8, 5, 10, 6, 12},
														   {12, 16, 15, 20, 18, 24},
														   {7, 14, 8, 16, 9, 18},
														   {21, 28, 24, 32, 27, 36}});
					
		}
		
		@Test
		@DisplayName("kronecker product of two 2 x 2 matrices")
		void kronecker2x2() {
			
			Matrix kronecker = twoByTwoSequential.kronecker(twoByTwoOther);
			
			assertMatrixEqualsExact(fourByFourKronecker, kronecker);
			
		}
		
		@Test
		@DisplayName("kronecker product of a 3 x 3 matrix and a 2 x 2 matrix")
		void kronecker3x3() {
			
			Matrix kronecker = threeByThreeSequential.kronecker(twoByTwoSequential);
			
			assertMatrixEqualsExact(sixBySixKronecker, kronecker);

		}
		
		@Test
		@DisplayName("checks whether the operands are changed")
		void multiplyChanged() {

			twoByTwoSequential.kronecker(twoByTwoOther);
			
			assertMatrixEqualsExact(new double[][] {{1, 2},{3, 4}}, twoByTwoSequential);
		    assertMatrixEqualsExact(new double[][] {{5, 6},{7, 8}}, twoByTwoOther);

		}
		
	}
	
	@Nested
	@DisplayName("Matrix.map()")
	class MapTests {
		
		Matrix twoByTwoSequential, twoByTwoApplied1, twoByTwoApplied2;
		Matrix threeByThreeSequential, threeByThreeApplied1, threeByThreeApplied2;
		DoubleUnaryOperator function1 = (x) -> (x * x);
		DoubleUnaryOperator function2 = (x) -> (2 * x);
		DoubleUnaryOperator functionIdentity = (x) -> (x);
		
		
		@BeforeEach
		void setUp() {
			
			twoByTwoSequential = new Matrix(new double[][] {{1, 2},{3, 4}});
			twoByTwoApplied1 = new Matrix(new double[][] {{1, 4},{9, 16}});
			twoByTwoApplied2 = new Matrix(new double[][] {{2, 4},{6, 8}});

			threeByThreeSequential = new Matrix(new double[][] {{1, 2, 3},{4, 5, 6},{7, 8, 9}});
			threeByThreeApplied1 = new Matrix(new double[][] {{1, 4, 9},{16, 25, 36},{49, 64, 81}});
			threeByThreeApplied2 = new Matrix(new double[][] {{2, 4, 6},{8, 10, 12},{14, 16, 18}});
					
		}
		
		@Test
		@DisplayName("applying x * x to a 2 x 2 matrix")
		void mapSquare2x2() {
			
			Matrix applied = twoByTwoSequential.map(function1);
			
			assertMatrixEqualsExact(twoByTwoApplied1, applied);
			
		}
		
		@Test
		@DisplayName("applying 2 * x to a 2 x 2 matrix")
		void mapDouble2x2() {
			
			Matrix applied = twoByTwoSequential.map(function2);
			
			assertMatrixEqualsExact(twoByTwoApplied2, applied);

		}
		
		@Test
		@DisplayName("applying identity function to a 2 x 2 matrix")
		void mapIdentity2x2() {
			
			Matrix same = twoByTwoSequential.map(functionIdentity);
			
			assertMatrixEqualsExact(twoByTwoSequential, same);
			
		}
		
		@Test
		@DisplayName("applying x * x to a 3 x 3 matrix")
		void mapSquare3x3() {
			
			Matrix applied = threeByThreeSequential.map(function1);
			
			assertMatrixEqualsExact(threeByThreeApplied1, applied);
			
		}
		
		@Test
		@DisplayName("applying 2 * x to a 3 x 3 matrix")
		void mapDouble3x3() {
			
			Matrix applied = threeByThreeSequential.map(function2);
			
			assertMatrixEqualsExact(threeByThreeApplied2, applied);

		}

		@Test
		@DisplayName("applying identity function to a 3 x 3 matrix")
		void mapIdentity3x3() {
			
			Matrix same = threeByThreeSequential.map(functionIdentity);
			
			assertMatrixEqualsExact(threeByThreeSequential, same);
			
		}
	}
	
	@Nested
	@DisplayName("Matrix.lu()")
	class LUTests {
	
		Matrix threeByThreeSequential, threeByThreeLUPacked;
		Matrix fourByFourSequential, fourByFourLUPacked;
		
		@BeforeEach
		void setUp() {
			
			threeByThreeSequential = new Matrix(new double[][] {{1, 2, -3},{4, 5, 6},{7, 8, 9}});
			threeByThreeLUPacked = new Matrix(new double[][] {{7, 8, 9},
															  {0.1428571429, 0.8571428571, -4.2857142857},
															  {0.5714285714, 0.5, 3}});
			fourByFourSequential = new Matrix(new double[][] {{1, 2, 3, -4},{5, 6, -7, 8},{9, 10, 11, 12},{13, 14, 15, 16}});
			fourByFourLUPacked = new Matrix(new double[][] {{13, 14, 15, 16},
															{0.0769230769, 0.9230769231, 1.8461538462, -5.2307692308},
															{0.3846153846, 0.6666666667, -14, 5.3333333333},
															{0.6923076923, 0.3333333333, 0, 2.6666666667}});
			
		}
		
		@Test
		@DisplayName("lu decomposition of a 3 x 3 matrix")
		void lu3x3() {
			
			Matrix luPacked = threeByThreeSequential.lu();
			
			assertMatrixEqualsApprox(threeByThreeLUPacked, luPacked);
			
		}
		
		@Test
		@DisplayName("lu decomposition of a 4 x 4 matrix")
		void lu4x4() {
			
			Matrix luPacked = fourByFourSequential.lu();
			
			assertMatrixEqualsApprox(fourByFourLUPacked, luPacked);
			
		}
	}
	
	@Nested
	@DisplayName("Matrix.forwardSubstitution()")
	class ForwardSubTests {
		
		Matrix twoByTwoLowerTri, twoByOneBVector, twoByOneXVector;
		Matrix threeByThreeLowerTri, threeByOneBVector, threeByOneXVector;
		
		@BeforeEach
		void setUp() {
			
			twoByTwoLowerTri = new Matrix(new double[][] {{2, 0},{3, 1}});
			twoByOneBVector = new Matrix(new double[][] {{4},{7}});
			twoByOneXVector = new Matrix(new double[][] {{2},{1}});
			threeByThreeLowerTri = new Matrix(new double[][] {{1, 0, 0},{2, 1, 0},{-1, 3, 1}});
			threeByOneBVector = new Matrix(new double[][] {{2},{5},{-4}});
			threeByOneXVector = new Matrix(new double[][] {{2},{1},{-5}});
			
		}
		
		@Test
		@DisplayName("forward substitution with a 2 x 2 matrix")
		void fs2x2() {
			
			Matrix x = twoByTwoLowerTri.forwardSubstitution(twoByOneBVector);
			
			assertMatrixEqualsApprox(twoByOneXVector, x);
			
		}
		
		@Test
		@DisplayName("forward substitution with a 3 x 3 matrix")
		void fs3x3() {
			
			Matrix x = threeByThreeLowerTri.forwardSubstitution(threeByOneBVector);
			
			assertMatrixEqualsApprox(threeByOneXVector, x);
			
		}
		
	}
	
	@Nested
	@DisplayName("Matrix.backwardSubstitution()")
	class BackwardSubTests {
		
		Matrix twoByTwoUpperTri, twoByOneYVector, twoByOneXVector;
		Matrix threeByThreeUpperTri, threeByOneYVector, threeByOneXVector;
		
		@BeforeEach
		void setUp() {
			
			twoByTwoUpperTri = new Matrix(new double[][] {{4, 2},{0, -3}});
			twoByOneYVector = new Matrix(new double[][] {{10},{-6}});
			twoByOneXVector = new Matrix(new double[][] {{1.5},{2}});
			threeByThreeUpperTri = new Matrix(new double[][] {{3, 1, -1},{0, 2, 4},{0, 0, -2}});
			threeByOneYVector = new Matrix(new double[][] {{2},{1},{-5}});
			threeByOneXVector = new Matrix(new double[][] {{3},{-4.5},{2.5}});
			
		}
		
		@Test
		@DisplayName("backward substitution with a 2 x 2 matrix")
		void fs2x2() {
			
			Matrix x = twoByTwoUpperTri.backwardSubstitution(twoByOneYVector);
			
			assertMatrixEqualsApprox(twoByOneXVector, x);
			
		}
		
		@Test
		@DisplayName("backward substitution with a 3 x 3 matrix")
		void fs3x3() {
			
			Matrix x = threeByThreeUpperTri.backwardSubstitution(threeByOneYVector);
			
			assertMatrixEqualsApprox(threeByOneXVector, x);
			
		}
		
	}
	
}
