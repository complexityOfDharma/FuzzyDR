package edu.gmu.fuzzydr.controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class SimulationLogger {
    private final String filePath;
    private final String headers;

    /*
    public SimulationLogger(String filePath) {
        this.filePath = filePath;
        
        // Initialize the log file with headers, overwriting any existing file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false))) {
        	
        	// headers
            writer.write("Run, Step, AgentID, Energy-Level, Common-Pool-Levels, agent0_delta_i, agent0_delta_e, agent0_delta_o, agent0_agreement\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */

    // Adjusted constructor to accept headers
    public SimulationLogger(String filePath, String headers) {
        this.filePath = filePath;
        this.headers = headers;

        // Initialize the log file with specified headers, overwriting any existing file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false))) {
            writer.write(headers + "\n");
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
    
    /*
    public synchronized void logPopulationStats(int step, double avgEnergy, double avgAgreement) {
        String entry = String.format("%d, AVG, AVG, %f, %f\n", step, avgEnergy, avgAgreement);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(entry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */
}
