package edu.gmu.fuzzydr.controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class SimulationLogger {
    private final String filePath;

    public SimulationLogger(String filePath) {
        this.filePath = filePath;
        
        // Initialize the log file with headers, overwriting any existing file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false))) {
        	
        	// headers
            writer.write("Step, AgentID, Energy, Agreement\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void logEntries(List<String> entries) {
        
    	// Append entries to the log file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            for (String entry : entries) {
                writer.write(entry + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
