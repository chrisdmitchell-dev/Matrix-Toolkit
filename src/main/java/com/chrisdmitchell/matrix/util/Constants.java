package com.chrisdmitchell.matrix.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Locale;

/**
 * A class containing all of the constants used by the program. The constants are split into
 * subclasses grouping them by general usage.
 */
public class Constants {
	
	/** Prevent the class from being instantiated. */
	private Constants() {};
	
	/** Constants related to formatted output for double values. */
	public static final class Formatting {

		/** Prevent the class from being instantiated. */
		private Formatting() {};
		
		public static final int PRECISION = 3;
		public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0." + "0".repeat(PRECISION));
		public static final Locale LOCALE = Locale.ROOT;

	}
	
	/** Constants related to file I/O. */
	public static final class Files {
		
		/** Prevent the class from being instantiated. */
		private Files() {};
		
		public static final String FILE_EXTENSION = ".json";
		public static final Path FILE_DIRECTORY = Path.of("matrices");
		public static final Charset CHARSET = StandardCharsets.UTF_8;
	      
	}
	
	/** Constants related to collecting user input. */
	public static final class UI {
		
		/** Prevent the class from being instantiated. */
		private UI() {};
		
		public static final String PROMPT = "mt> ";
		public static final int HELP_PADDING = 30;

		
	}
	
	/** Constants to be used by numeric methods. */
	public static final class Numeric {
		
		/** Prevent the class from being instantiated. */
		private Numeric() {};
		
		/** The tolerance used for approximate comparisons. Must be greater than {@code 0.0}. */
		public static final double EPSILON = 1e-10;
		
		public static final double ABSOLUTE_EPSILON = 1e-12;
		public static final double RELATIVE_EPSILON = 1e-12;

		/** The length of a randomly generated matrix name */
		public static final int RANDOM_MATRIX_NAME_LENGTH = 3;
		
		public static final long NaN_SENTINEL = Long.MAX_VALUE - 1;
		public static final long POSITIVE_INFINITY_SENTINEL = Long.MAX_VALUE;
		public static final long NEGATIVE_INFINITY_SENTINEL = Long.MIN_VALUE;

	}

}
