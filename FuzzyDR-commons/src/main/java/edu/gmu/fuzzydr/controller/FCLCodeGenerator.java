package edu.gmu.fuzzydr.controller;

import net.sourceforge.jFuzzyLogic.*;
import java.io.*;

public class FCLCodeGenerator {

    private String fclString;

    public FCLCodeGenerator(String fclString) {
        this.fclString = fclString;
    }

    public FIS loadFCL() {
    	//try (InputStream is = new ByteArrayInputStream(fclString.getBytes())) {
    	try (InputStream fclFileInputStream = getClass().getResourceAsStream(this.fclString)) {
            return FIS.load(fclFileInputStream, true);
            
        } catch (IOException e) {
            throw new RuntimeException("Error loading FCL from string.", e);
        }
    }
    
    // Additional methods to manipulate and generate FCL string as needed...
}
