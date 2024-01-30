package edu.gmu.fuzzydr.controller;

import java.io.IOException;

public class BatchRunManager {
	
	//private static int batchRunID = 0;

    public static void runBatchExperiments(String[] args) throws IOException {
        
        // Loop through each parameter range
        for (int pop = Config.agentPopulationRange[0]; pop <= Config.agentPopulationRange[1]; pop += Config.agentPopulationRange[2]) {
            for (int K = Config.resourceCarryingCapacityRange[0]; K <= Config.resourceCarryingCapacityRange[1]; K += Config.resourceCarryingCapacityRange[2]) {
                // Set the current config
                Config.agentPopulation = pop;
                Config.resourceCarryingCapacity = K;

                // Run the simulation
                runSimulationWithCurrentConfig(args);
                
                // increment batch run ID
                Config.batchRunID++;
            }
        }
    }

    private static void runSimulationWithCurrentConfig(String[] args) throws IOException {
        String _logFilepath = generateLogFilepath(Config.batchRunID); 
    	
    	FuzzyDRController simulation = new FuzzyDRController(Config.RANDOM_SEED, _logFilepath);
        simulation.start();
        
        // while simulation has more steps to execute, continue to step through the schedule and all associated step() methods.
        while (simulation.schedule.step(simulation)) {
        	
        }
        simulation.finish();
    }

    private static String generateLogFilepath(int batchRunId) {
        int _pop = Config.agentPopulation;
        int _K = Config.resourceCarryingCapacity;
    	
    	
    	String _dir = "src/main/resources/";
        String _fileName = "log_run" + batchRunId + "_n=" + _pop + "_K=" + _K + ".csv";
        
        return _dir + _fileName;
    }
    
}