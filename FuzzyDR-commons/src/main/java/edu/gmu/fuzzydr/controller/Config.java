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
    public final static double consumptionLevel = 1;
    
    public static int resourceCarryingCapacity = 1000; //1333;
    public final static double commonsRegrowthRate = 0.3;
    
    // w.r.t. small world network parameters
    public final static int initNeighbors = 5; //3;
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
    public static boolean isScenarioRun = true;					// if true, trigger experiment conditions parameterization for selected Scenario. If false, model runs to termination with no Scenario parameterization.
    public static boolean isExperimentalControlRun = false;		// if true, Control case for Scenario Run, with test agents parameterized for Scenario, but no fuzzyDR Agent's Step(). If false, active fuzzyDR for test agents.
    
    // NOTE: to run custom scenarios, make isFuzzyDRforALL = false, and then specify what scenario experiment you want to test with scenarioID.
    
    // ------- !!! Scenario descriptions. -------
    // ('0') - no scenario - default parameterization
    // ('1') - Scenario 1 - delta_i: 'maintain status quo'
    // ('2') - Scenario 2 - delta_i: 'dire straits'
    // ('3') - Scenario 3 - delta_e: 'behind the pack'
    // ('4') - Scenario 4 - delta_e: 'king of the hill'
    // ('5') - Scenario 5 - delta_o: 'on the brink'
	// ('6') - Scenario 6 - delta_o: 'safe bet'
	// ('7') - Scenario 7 - full fuzzyDR: 'the good life'
	// ('8') - Scenario 8 - full fuzzyDR: 'off-ramp'
    // ('9') - Scenario 9 - full fuzzyDR: 'conflicted tradeoff'
    // ('10') - Scenario 10 - full fuzzyDR for all agents: 'hello fuzzy world'
    // !!! TODO: is there a scenario run exclusively in overlapping regions of membership sets.
    public static final int scenarioID = 7;			// specify the kind of Agent 0 customization you want to do experiments with.
    public static final int subScenarioID = 4; 		// if running Scenario 7, Scenario 8, Scenario 9, or Scenario 10, activate only delta_i (ID=1), only delta_e (ID=2), only delta_o (ID=3), or delta_tree (ID=4).
    
    // batch run parameters
    public static int batchRunID = 0;
    public static boolean isBatchRun = false;
    //public static int[] resourceCarryingCapacityRange = new int[] { 5000, 10000, 1000 };	// { start, stop, step size }.
    //public static double[] commonsRegrowthRateRange = new double[] { 0.05, 0.1, 0.05 };		// { start, stop, step size }.
    //public static int[] consumptionLevelRange = new int[] { 1, 10, 5 };						// { start, stop, step size }.
    //public static int[] agentEnergyLossPerStepRange = new int[] { 1, 5, 1};					// { start, stop, step size }.
    
    public static int[] agentPopulationRange = new int[] { 100, 100, 1 };					// { start, stop, step size }.
    public static int[] resourceCarryingCapacityRange = new int[] { 1000, 1000, 1 };		// { start, stop, step size }.
    public static double[] commonsRegrowthRateRange = new double[] { 0.3, 0.3, 0 };			// { start, stop, step size }.
    public static int[] agentEnergyLossPerStepRange = new int[] { 5, 5, 0};					// { start, stop, step size }.
    public static int[] consumptionLevelRange = new int[] { 10, 10, 0 };					// { start, stop, step size }.
    
    // TODO: loop over ADICO sanction quantities... !!!
    
}
