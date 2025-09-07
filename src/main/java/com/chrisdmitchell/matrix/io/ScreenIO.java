package com.chrisdmitchell.matrix.io;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chrisdmitchell.matrix.model.Matrix;
import com.chrisdmitchell.matrix.util.LogUtils;

import static com.chrisdmitchell.matrix.util.Constants.Files.*;
import static com.chrisdmitchell.matrix.util.Constants.Formatting.*;

public class ScreenIO {
	
	private static final Logger log = LoggerFactory.getLogger(ScreenIO.class);

	/**
	 * Prints the matrix using its {@code toString()} method.
	 * 
	 * @param matrix		the matrix to print
	 */
	public static void printMatrix(Matrix matrix) {
		
		LogUtils.logMethodEntry(log);
		
		System.out.println(matrix);
		
		LogUtils.logMatrix(log, "Printed", matrix);
	}
	
	/**
	 * Prints the matrix as a pretty string.
	 * 
	 * @param matrix		the matrix to print
	 */
	public static void prettyPrintMatrix(Matrix matrix) {
		
		LogUtils.logMethodEntry(log);

		System.out.println(matrix.toPrettyString());
		
		LogUtils.logMatrix(log, "Pretty printed", matrix);
		
	}
	
	/**
	 * Prints all matrices stored in the supplied {@code Map<String, String>} using
	 * the {@code toString()} method for the {@code Matrix} object.
	 * 
	 * @param matrices		the map containing the matrices to be printed stored as
	 * 						values along with the matrix name as key
	 */
	public static void printAllMatrices(Map<String, Matrix> matrices) {

		LogUtils.logMethodEntry(log);
		
		matrices.values().stream()
	    				 .map(Matrix::toShortString)
	    				 .forEach(System.out::println);
		
		log.info("Printed all matrices in memory.");
	}
	
	/**
	 * Prints all files and matrix names contained within the {@code FILE_DIRECTORY}
	 * directory.
	 * 
	 * @param fileMap		a {@code Map<String, String>} containing the key-value
	 * 						pair of matrix name-filename 
	 */
	public static void printDirectoryListingWithNames(Map<String, String> fileMap) {
		
		LogUtils.logMethodEntry(log);
		
		for (Map.Entry<String, String> entry : fileMap.entrySet()) {
		    System.out.printf("%s contains matrix %s.%n", entry.getKey(), entry.getValue());
		}
		
		log.info("Printed all files in directory {}.", FILE_DIRECTORY);

	}

	/**
	 * Prints the scalar value associated with the matrix.
	 * <p>
	 * The output is {@code label(MATRIX_NAME) = (SCALAR_FORMAT)}
	 * </p>
	 * 
	 * @param label				the label of the associated scalar value
	 * @param matrixName		the name of the matrix
	 * @param value				the double value to be printed
	 */
	public static void printScalarValues(String label, String matrixName, double value) {
		
		LogUtils.logMethodEntry(log);
		
		System.out.printf("%s(%s) = %s%n,", label.strip(), matrixName, SCALAR_FORMAT.format(value));
		
		log.info("Printed {}({}) = {}.", label, matrixName, SCALAR_FORMAT.format(value));
		
	}
	
	public static boolean printLUandSave(Matrix matrix, Matrix l, Matrix u, Matrix permutations) {
		
		LogUtils.logMethodEntry(log);

		System.out.println("Perm(A) * A = L(A) * U(A)");
		System.out.println("Where:");
		System.out.println(permutations);
		System.out.println(matrix);
		System.out.println(l);
		System.out.println(u);
		
		return ConsoleUI.getYorN("Would you like to save these results to memory", "y");
		
	}
}
