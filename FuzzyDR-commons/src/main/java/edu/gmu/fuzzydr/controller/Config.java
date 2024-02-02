package edu.gmu.fuzzydr.controller;

public class Config {

	public static final int RANDOM_SEED = 12345;
    public static ec.util.MersenneTwisterFast RANDOM_GENERATOR = new ec.util.MersenneTwisterFast(RANDOM_SEED);
	
    public static final int WIDTH = 600;
    public static final int HEIGHT = 600;
    
    public final static String SIM_NAME = "Fuzzy Deontic Reasoning";
    
    public final static int terminationStepCount = 2000;
    
    public static int agentPopulation = 100;
    public final static double agentInitialEnergy = 100;
    
    // TODO: change this energy loss per step according to some ENUM specification of agent types (e.g., selfish, cooperative, etc.).
    public static int agentEnergyLossPerStep = 1;
    public final static int consumptionLevel = 1;
    
    public static int resourceCarryingCapacity = 1200;
    public final static double commonsRegrowthRate = 0.3;
    
    // w.r.t. small world network parameters
    public final static int initNeighbors = 2;
    public final static double rewiringProb = 0.05;  //0.8;
    
    // fuzzy logic
    
    public static boolean isFuzzyDRforALL = false;										// if fuzzy inference is active or not for model runs.
    
    // TODO: these FCLs may be customized to personas, e.g., 'Conformist_delta_i.fcl,' 'Skeptic_delta_i.fcl'
    public static final String genericAgentFCLPath = "/generic_agent.fcl"; 			// specified with leading '/' for path absolute relative to the classpath root.
    public static final String delta_i_FCLPath = "/delta_i.fcl";   					// specified with leading '/' for path absolute relative to the classpath root.
    public static final String delta_e_FCLPath = "/delta_e.fcl";   					// specified with leading '/' for path absolute relative to the classpath root.
    public static final String delta_o_FCLPath = "/delta_o.fcl";   					// specified with leading '/' for path absolute relative to the classpath root.
    public static final String delta_tree_FCLPath = "/delta_tree.fcl";   			// specified with leading '/' for path absolute relative to the classpath root.
    
    // experiment scenarios
    // NOTE: to run custom scenarios, make isFuzzyDRforALL = false, and then specify what scenario experiment you want to test with scenarioID.
    
    // ------- !!! Scenario descriptions. -------
    // Scenario 1 - delta_i: 'maintain advantage'
    // Scenario 2 - delta_i: 'desperation'
    // Scenario 3 - delta_e: 
    // Scenario 4 - delta_e: 
    // Scenario 5 - delta_o:
	// Scenario 6 - delta_o:
	// Scenario 7 - delta_i + delta_e: 
	// Scenario 8 - delta_i + delta_e + delta_o: 
    public static final int scenarioID = 1;			// specify the kind of Agent 0 customization you want to do experiments with.
    
    
    // batch run parameters
    public static int batchRunID = 0;
    public static boolean isBatchRun = true;
    //public static int[] resourceCarryingCapacityRange = new int[] { 5000, 10000, 1000 };	// { start, stop, step size }.
    //public static double[] commonsRegrowthRateRange = new double[] { 0.05, 0.1, 0.05 };		// { start, stop, step size }.
    //public static int[] consumptionLevelRange = new int[] { 1, 10, 5 };						// { start, stop, step size }.
    //public static int[] agentEnergyLossPerStepRange = new int[] { 1, 5, 1};					// { start, stop, step size }.
    
    public static int[] agentPopulationRange = new int[] { 100, 100, 1 };					// { start, stop, step size }.
    public static int[] resourceCarryingCapacityRange = new int[] { 1000, 1000, 1 };		// { start, stop, step size }.
    public static double[] commonsRegrowthRateRange = new double[] { 0.3, 0.3, 0 };			// { start, stop, step size }.
    public static int[] agentEnergyLossPerStepRange = new int[] { 5, 5, 0};					// { start, stop, step size }.
    public static int[] consumptionLevelRange = new int[] { 10, 10, 0 };					// { start, stop, step size }.
    
}
