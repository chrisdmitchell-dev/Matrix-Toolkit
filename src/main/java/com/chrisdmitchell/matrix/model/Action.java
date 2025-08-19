package com.chrisdmitchell.matrix.model;

import static com.chrisdmitchell.matrix.util.Constants.UI.*;

/**
 * An enumeration containing the commands that can be given at the prompt.
 * 
 * @author Chris Mitchell
 */
public enum Action {

	ADD("add MATRIX_NAME1 MATRIX_NAME2", new String[] {
		"Adds MATRIX_NAME1 to MATRIX_NAME2 and returns the sum."
		}, new int[] { 2 }),
	CLEAR("clear MATRIX_NAME", new String[] {
		"Clears the matrix MATRIX_NAME from memory."
		}, new int[] { 1 }),
	COFACTORS("cofactors MATRIX_NAME", new String[] {
		"Finds the matrix of cofactors for MATRIX_NAME."
		}, new int[] { 1 }),
	DETERMINANT("determinant MATRIX_NAME", new String[] {
		"Calculates the determinant of matrix MATRIX_NAME.",
		"You can enter 'determinant' or 'det.'"
		}, new int[] { 1 }),
	EXIT("exit, quit, q", new String[] {
		"Exits the program.",
		"You can enter 'exit,' 'quit,' or just 'q.'"
		}, new int[] { 0 }),
	HELP("help [COMMAND]", new String[] {
		"Prints help for using the given COMMAND. If no COMMAND is",
		"specified, it prints generalized help for all commands."
		}, new int[] { 0, 1 } ),
	INPUT("input MATRIX_NAME", new String[] {
		"Use the keyboard to input a new matrix called MATRIX_NAME."
		}, new int[] { 1 }),
	INVERSE("inverse MATRIX_NAME", new String[] {
		"Finds the inverse of MATRIX_NAME provided that it is invertible."
		}, new int[] { 1 }),
	LIST("list", new String[] {
		"Get a list of files containing matrices."
		}, new int[] { 0 }),
	MULTIPLY("multiply MATRIX_NAME1 (MATRIX_NAME2|SCALAR)", new String[] {
		"Multiplies MATRIX_NAME1 by either the MATRIX_NAME2 or SCALAR."
		}, new int[] { 2 }),
	LOAD("load MATRIX_NAME [from FILENAME]", new String[] {
		"Loads the matrix stored in FILENAME into matrix MATRIX_NAME.",
		"This file must have been generated using the 'save' command.",
		"There is no need to append an extension to the FILENAME.",
		"If no FILENAME is specified, the MATRIX_NAME will be used",
		"as a FILENAME."
		}, new int[] { 1, 2 }),
	PRINT("print [MATRIX_NAME]", new String[] {
		"Prints the value of MATRIX_NAME.",
		"If MATRIX_NAME is not given, the program will print a list",
		"of all of the matrices stored in memory along with their size."
		}, new int[] { 0, 1 }),
	SAVE("save MATRIX_NAME [to FILENAME]", new String[] {
		"Saves the given matrix MATRIX_NAME to the file FILENAME.",
		"There is no need to append an extension to the FILENAME.",
		"If no FILENAME is specified, the MATRIX_NAME will be used",
		"as a FILENAME."
		}, new int[] { 1, 2 }),
	SET("set MATRIX_NAME to ACTION (ARGS)", new String[] {
		"Sets the matrix MATRIX_NAME to the result of the ACTION command",
		"and appropriate arguments, e.g., 'set A to add B C.'",
		"ACTION must be one of 'add,' 'subtract,' 'multiply,'",
		"'cofactors,' 'transpose,' 'inverse,'"
		}, new int[] { 3, 4, 5 }),
	SUBTRACT("subtract MATRIX_NAME1 MATRIX_NAME2", new String[] {
		"Subtracts MATRIX_NAME2 from MATRIX_NAME1 and returns the difference."
		}, new int[] { 2 }),
	TRANSPOSE("transpose MATRIX_NAME", new String[] {
		"Finds the transpose of MATRIX_NAME."
		}, new int[] { 1 });
	
    private final String display;
    private final String[] helpLines;
    private final int[] numberOfArguments;

    /**
     * A constructor allowing for the creation of an {@code Action} object containing
     * the enum for the given command, the help to be printed for that command, and the
     * number of arguments expected for that command.
     * 
     * @param display				the enum value of the {@code Action}
     * @param helpLines				the help for that command as a string[] of lines
     * @param numberOfArguments		the valid number of arguments for that command
     */
    Action(String display, String[] helpLines, int[] numberOfArguments) {
        this.display = display;
        this.helpLines = helpLines;
        this.numberOfArguments = numberOfArguments;
    }
    
    /**
     * Returns the number of arguments allowed for a given action.
     * 
     * @return		the number of arguments
     */
    public int[] getNumberOfArguments() {
    	
    	return numberOfArguments;
    	
    }

    /**
     * Prints the help for a given command as found in {@code this.helpLines}.
     */
    public void printHelp() {

        String indent = " ".repeat(HELP_PADDING);

        if (helpLines.length == 0) {
            System.out.printf("%-" + HELP_PADDING + "s%n", display);
            return;
        }
        System.out.printf("%-" + HELP_PADDING + "s%s%n", display, helpLines[0]);
        for (int i = 1; i < helpLines.length; i++) {
            System.out.println(indent + helpLines[i]);
        }
        
    }
    
    /**
     * Prints all commands along with the help strings.
     */
    public void printAllHelp() {
    	
        for (Action action : values()) {
            action.printHelp();
        }
        
    }
}
