package edu.gmu.fuzzydr.controller;

public class Config {

	public static final int RANDOM_SEED = 12345;
    public static ec.util.MersenneTwisterFast RANDOM_GENERATOR = new ec.util.MersenneTwisterFast(RANDOM_SEED);
	
    public final static String SIM_NAME = "Fuzzy Deontic Reasoning";
    
    public final static int agentPopulation = 1;
    public final static double agentInitialEnergy = 10;
    
    public final static double agentEnergyLossPerStep = 1;
    
    public final static double resourceCarryingCapacity = 10;
    public final static double commonsRegrowthRate = 0.1;
    
    
}
