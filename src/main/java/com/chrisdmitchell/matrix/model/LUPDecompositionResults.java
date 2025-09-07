package com.chrisdmitchell.matrix.model;

public record LUPDecompositionResults(double[][] luPacked, int[] pivot, int parity, Double determinant, int rows, int columns) {
	
	public double[][] l() {
		
		int k = Math.min(rows, columns);
		
		double[][] lowerTriangular = new double[rows][k];
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < k; j++) {
				if (j > i) {
					lowerTriangular[i][j] = 0; 
				} else if (j == i) {
					lowerTriangular[i][j] = 1;
				} else {
					lowerTriangular[i][j] = luPacked[i][j];
				}
			}
		}
		
		return lowerTriangular;
		
	}
	
	public double[][] u() {

		int k = Math.min(rows, columns);

		double[][] upperTriangular = new double[k][columns];
		
		for (int i = 0; i < k; i++) {
			for (int j = 0; j < columns; j++) {
				if (j >= i) {
					upperTriangular[i][j] = luPacked[i][j]; 
				} else {
					upperTriangular[i][j] = 0;
				}
			}
		}
		
		return upperTriangular;
		
	}
	
	public double[][] permutations() {
		
		double[][] permutations = new double[rows][columns];
		
		for (int i = 0; i < pivot.length; i++) {
			for (int j = 0; j < columns; j++) {
				permutations[i][j] = (pivot[i] == j) ? 1 : 0; 
			}
		}
		
		return permutations;
		
	}
}
