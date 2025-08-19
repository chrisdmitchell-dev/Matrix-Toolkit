package com.chrisdmitchell.matrix.model;

/**
 * The {@code Command} record allows the method soliciting user input to return
 * a structured object containing the command's {@code Action} enum and the 
 * processed number of arguments.
 * 
 * @author Chris Mitchell
 */
public record Command(Action action, String[] args) {}
