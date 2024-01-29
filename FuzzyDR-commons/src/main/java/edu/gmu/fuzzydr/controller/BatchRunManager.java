package edu.gmu.fuzzydr.controller;

import java.io.IOException;

public class BatchRunManager {

    public static void runBatchExperiments(String[] args) throws IOException {
        
        // Loop through each parameter range
        for (int pop = Config.agentPopulationRange[0]; pop <= Config.agentPopulationRange[1]; pop += Config.agentPopulationRange[2]) {
            for (int K = Config.resourceCarryingCapacityRange[0]; K <= Config.resourceCarryingCapacityRange[1]; K += Config.resourceCarryingCapacityRange[2]) {
                // Set the current config
                Config.agentPopulation = pop;
                Config.resourceCarryingCapacity = K;

                // Run the simulation
                runSimulationWithCurrentConfig(args);
            }
        }
    }

    private static void runSimulationWithCurrentConfig(String[] args) throws IOException {
        FuzzyDRController simulation = new FuzzyDRController(System.currentTimeMillis());
        simulation.start();
        
        // while simulation has more steps to execute, continue to step through the schedule and all associated step() methods.
        while (simulation.schedule.step(simulation)) {
        	
        }
        simulation.finish();
    }

    
}