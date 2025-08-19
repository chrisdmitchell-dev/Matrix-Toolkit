package com.chrisdmitchell.matrix.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chrisdmitchell.matrix.model.Matrix;
import com.chrisdmitchell.matrix.util.LogUtils;

import static com.chrisdmitchell.matrix.util.Constants.Files.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;



public class FileIO {
	
	private static final ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();
	private static final ObjectReader reader = new ObjectMapper().readerFor(Matrix.class);
	
	private static final Logger log = LoggerFactory.getLogger(FileIO.class);
	
	/**
	 * Calls {@code saveMatrix(matrix, matrix.getName())} so that the default filename is
	 * the name of the matrix.
	 * 
	 * @param matrix		the matrix to save
	 * @return				{@code true} if {@code saveMatrix()} returns {@code true},
	 * 						{@code false} otherwise
	 */
	public static boolean saveMatrix(Matrix matrix) {
		
		return saveMatrix(matrix, matrix.getName());
		
	}
	
	/**
	 * Saves the specified matrix as a json to the given filename.
	 * 
	 * @param matrix		the matrix to save
	 * @param filename		the filename to save the matrix to
	 * @return				{@code true} if save was successful, {@code false} otherwise
	 */
	public static boolean saveMatrix(Matrix matrix, String filename) {
	
		LogUtils.logMethodEntry(log);
	
		if (!ensureMatrixDirectoryExists()) {
			return false;
		}
		Path path = FILE_DIRECTORY.resolve(filename);
		
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path)) {
            writer.writeValue(bufferedWriter, matrix);
            matrix.setSavedToDisk(true);
            log.info("Matrix {} saved to {}.", matrix.getName(), path);
            return true;
        } catch (IOException e) {
            log.error("Failed to save matrix {} to file {}: {}", matrix.getName(), path, e.getMessage());
            return false;
        }
	}
	
	/**
	 * Loads the matrix from a json file.
	 * 
	 * @param filename		the filename from which to load the matrix
	 * @return				the loaded matrix
	 */
	public static Matrix loadMatrix(String filename) {

		LogUtils.logMethodEntry(log);
		
		Matrix matrix = null;
		
		Path path = FILE_DIRECTORY.resolve(filename);
		
		if (!Files.exists(path)) {
		    System.out.printf("File %s not found.%n", path);
		    log.warn("User tried to load a matrix from file {} which does not exist.", path);
		} else {
			try (BufferedReader bufferedReader = Files.newBufferedReader(path)) {
				matrix = reader.readValue(bufferedReader);
				matrix.setSavedToDisk(true);
				log.info("Matrix {} loaded from file {}.", matrix.getName(), path);
			} catch (IOException e) {
				matrix = null;
				log.error("Failed to read file {}: {}", path, e.getMessage());
			}
		}
		return matrix;

	}
	
	/**
	 * Checks to see if the {@code FILE_DIRECTORY} exists and creates it if it doesn't.
	 * 
	 * @return		{@code true} if the directory exists or was created successfully,
	 * 				{@code false} otherwise
	 */
	public static boolean ensureMatrixDirectoryExists() {
	
		LogUtils.logMethodEntry(log);
	
	    if (Files.exists(FILE_DIRECTORY)) {
	        if (!Files.isDirectory(FILE_DIRECTORY)) {
	            System.out.print("The directory for saving and loading files does not exist\n" +
	            				 "as a directory. Please remove the file called " + FILE_DIRECTORY + "\n" +
	            				 "to continue.\n");
	            log.error("Attempting to save file to subdirectory {}. Another file exists with the same name.", FILE_DIRECTORY);
	            return false;
	        }
	        log.info("Matrix directory {} for saving and loading files found.", FILE_DIRECTORY);
	        return true;
	    }
	
	    try {
	        Files.createDirectories(FILE_DIRECTORY);
	        log.info("Matrix directory {} created for saving and loading files.", FILE_DIRECTORY);
	        return true;
	    } catch (IOException e) {
	        System.out.println("Could not create '" + FILE_DIRECTORY + "' directory: " + e.getMessage());
	        log.error("Could not create subdirectory {}. Unable to save or load matrices: {}", FILE_DIRECTORY, e.getMessage());
	        return false;
	    }
	}

	/**
	 * Generates a {@code Map<String, String>} containing the filename and matrix name as key-value pairs.
	 * 
	 * @return		a {@code TreeMap<>} with the filename and matrix name
	 */
	public static Map<String, String> listDirectoryContentsWithNames() {

		LogUtils.logMethodEntry(log);
		
		Map<String, String> filesWithMatrixName = new TreeMap<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(FILE_DIRECTORY)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                	filesWithMatrixName.put(entry.getFileName().toString(), getMatrixNameFromFile(entry));
                }
            }
            log.info("Successfully read the list of files from {}", FILE_DIRECTORY);
        } catch (NoSuchFileException e) {
        	System.out.printf("The directory %s does not exist.%n", FILE_DIRECTORY);
        	log.warn("No such directory {}.", FILE_DIRECTORY);
        } catch (IOException e) {
            System.err.printf("Error reading directory %s: %s%n", FILE_DIRECTORY, e.getMessage());
            log.error("Error reading directory {}: {}", FILE_DIRECTORY, e.getMessage());
        }
        return filesWithMatrixName;
	}
	
	/**
	 * Uses Jackson to read the matrix name from the given file specified as a {@code Path}.
	 * 
	 * @param path		the {@code Path} of the file
	 * @return			the name of the matrix store in the file
	 */
	public static String getMatrixNameFromFile(Path path) {
		
		LogUtils.logMethodEntry(log);
		
        ObjectMapper mapper = new ObjectMapper();
        try {
            String content = Files.readString(path);
            JsonNode root = mapper.readTree(content);
            JsonNode nameNode = root.get("name");
            if (nameNode != null) {
                return nameNode.asText();
            }
        } catch (IOException e) {
            System.err.printf("Failed to extract field from file %s: %s%n", path, e.getMessage());
            log.error("Failed to extract 'name' field from file {}: {}", path, e.getMessage());
        }

        return null;
	}

}
