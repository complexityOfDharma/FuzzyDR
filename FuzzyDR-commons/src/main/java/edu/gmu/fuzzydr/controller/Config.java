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
    public static int agentEnergyLossPerStep = 5;
    public final static int consumptionLevel = 10;
    
    public static int resourceCarryingCapacity = 5000;
    public final static double commonsRegrowthRate = 0.3;
    
    // w.r.t. small world network parameters
    public final static int initNeighbors = 2;
    public final static double rewiringProb = 0.05;  //0.8;
    
    // fuzzy logic files.
    public static final String genericAgentFCLPath = "/generic_agent.fcl"; 			// specified with leading '/' for path absolute relative to the classpath root.
    public static final String delta_i_FCLPath = "/delta_i.fcl";   					// specified with leading '/' for path absolute relative to the classpath root.
    public static final String delta_e_FCLPath = "/delta_e.fcl";   					// specified with leading '/' for path absolute relative to the classpath root.
    public static final String delta_o_FCLPath = "/delta_o.fcl";   					// specified with leading '/' for path absolute relative to the classpath root.
    public static final String delta_tree_FCLPath = "/delta_tree.fcl";   			// specified with leading '/' for path absolute relative to the classpath root.
    
    // batch run parameters
    public static int batchRunID = 0;
    public static boolean isBatchRun = true;
    //public static int[] resourceCarryingCapacityRange = new int[] { 5000, 10000, 1000 };	// { start, stop, step size }.
    //public static double[] commonsRegrowthRateRange = new double[] { 0.05, 0.1, 0.05 };		// { start, stop, step size }.
    //public static int[] consumptionLevelRange = new int[] { 1, 10, 5 };						// { start, stop, step size }.
    //public static int[] agentEnergyLossPerStepRange = new int[] { 1, 5, 1};					// { start, stop, step size }.
    
    public static int[] agentPopulationRange = new int[] { 100, 101, 1 };					// { start, stop, step size }.
    public static int[] resourceCarryingCapacityRange = new int[] { 1000, 2000, 1000 };		// { start, stop, step size }.
    public static double[] commonsRegrowthRateRange = new double[] { 0.3, 0.3, 0 };			// { start, stop, step size }.
    public static int[] agentEnergyLossPerStepRange = new int[] { 5, 5, 0};					// { start, stop, step size }.
    public static int[] consumptionLevelRange = new int[] { 10, 10, 0 };					// { start, stop, step size }.
    
}
