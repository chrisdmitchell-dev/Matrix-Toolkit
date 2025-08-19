package com.chrisdmitchell.matrix.util;

import org.slf4j.Logger;

import com.chrisdmitchell.matrix.model.LogSeverity;
import com.chrisdmitchell.matrix.model.Matrix;

/**
 * A class of utility methods for use with slf4j and logback logging.
 * 
 * @author Chris Mitchell
 */
public class LogUtils {
	
	/**
	 * Logs a short, single line representation of the matrix using slf4j at the specified level.
	 * 
	 * @param log		the Logger object to write to
	 * @param level		the severity of the message to log
	 * @param message	the string that should precede the matrix output
	 * @param matrix	the matrix to be represented in the log entry
	 */
	public static void logMatrix(Logger log, LogSeverity severity, String message, Matrix matrix) {
		
		String content = MatrixUtils.toShortString(matrix);
	
	    switch (severity) {
	        case TRACE -> log.trace("{}: {}", message, content);
	        case DEBUG -> log.debug("{}: {}", message, content);
	        case INFO  -> log.info("{}: {}", message, content);
	        case WARN  -> log.warn("{}: {}", message, content);
	        case ERROR -> log.error("{}: {}", message, content);
	    }
				
	}
	
	/**
	 * Logs a matrix at the {@code DEBUG} level. Equivalent to calling {@code logMatrix()}
	 * with {@code LogSeverity.DEBUG}.
	 */
	public static void logMatrix(Logger log, String message, Matrix matrix) {
		
		logMatrix(log, LogSeverity.DEBUG, message, matrix);
		
	}
	
	/**
	 * Returns the {@code classname.methodname()} of the calling class and method at
	 * the specified depth.
	 * 
	 * @param depth		the depth of the stack for which to return the class and method;
	 * 					a value of 3 returns the {@code classname.methodname()} of the calling
	 * 					method; a value of 4 returns the {@code classname.methodname()} of the
	 * 					caller of the calling method 
	 * @return			a string containing the class and method name as {@code class.method()}
	 */
	public static String methodAtDepth(int depth) {

		StackTraceElement element = Thread.currentThread().getStackTrace()[depth];
		return element.getClassName() + "." + element.getMethodName() + "()";
		
	}

	/**
	 * Returns the {@code classname.methodname(filename, line number)} of the calling class
	 * and method at the specified depth.
	 * 
	 * @param depth		The depth of the stack for which to return the information;
	 * 					a value of 3 returns the {@code classname.methodname(filename, line number)}
	 * 					of the calling method; a value of 4 returns the
	 * 					{@code classname.methodname(filename, line number)} of the caller of the
	 * 					calling method
	 * @return			a string containing the class, method, filename, and line
	 * 					number as {@code classname.methodname(filename, line number)}.
	 */
	public static String methodWithLocationAtDepth(int depth) {
		
	    StackTraceElement element = Thread.currentThread().getStackTrace()[depth];
	    return String.format("%s.%s(%s:%d)",
	        element.getClassName(),
	        element.getMethodName(),
	        element.getFileName(),
	        element.getLineNumber());
	    
	}
	
	/**
	 * Logs at the {@code DEBUG} level the calling method and caller of the calling method.
	 * 
	 * @param log		the Logger object to log to
	 */
	public static void logMethodEntry(Logger log)
	{
		
	    log.debug("{} called from {}.", methodAtDepth(3), methodWithLocationAtDepth(4));
	    
	}
	
	/**
	 * Returns a formatted call stack containing the package, class, method, file, and line
	 * number of each calling method.
	 * 
	 * @return		the string containing the stack trace 
	 */
    public static String fullStackTrace() {
    	
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        
        StringBuilder stackTraceString = new StringBuilder();
        for (int i = 3; i < stack.length; i++) {
            StackTraceElement e = stack[i];
            stackTraceString.append(String.format("    at %s.%s(%s:%d)%n",
            	e.getClassName(),
                e.getMethodName(),
                e.getFileName(),
                e.getLineNumber()));
        }
        return stackTraceString.toString();
        
    }
	
}
