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
	 * Prints the determinant of a matrix.
	 * 
	 * @param matrix		the matrix for which the determinant was calculated
	 * @param det			the resulting determinant
	 */
	public static void printDeterminant(Matrix matrix, double det) {
		
		LogUtils.logMethodEntry(log);	
		
		System.out.printf("det(%s) = %s%n", matrix.getName(), DECIMAL_FORMAT.format(det));
		
		log.info("Printed the determinant {} for matrix {}.", DECIMAL_FORMAT.format(det), matrix.getName());
		
	}

}
