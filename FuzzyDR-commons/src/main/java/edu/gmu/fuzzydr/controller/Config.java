package edu.gmu.fuzzydr.controller;

public class Config {

	public static final int RANDOM_SEED = 12345;
    public static ec.util.MersenneTwisterFast RANDOM_GENERATOR = new ec.util.MersenneTwisterFast(RANDOM_SEED);
	
    public static final int WIDTH = 600;
    public static final int HEIGHT = 600;
    
    public final static String SIM_NAME = "Fuzzy Deontic Reasoning";
    
    public final static int agentPopulation = 25;
    public final static double agentInitialEnergy = 10;
    
    // TODO: change this energy loss per step according to some ENUM specification of agent types (e.g., selfish, cooperative, etc.).
    public final static double agentEnergyLossPerStep = 1;
    
    public final static double resourceCarryingCapacity = 100;
    public final static double commonsRegrowthRate = 0.1;
    
    // w.r.t. small world network parameters
    public final static int initNeighbors = 4;
    public final static double rewiringProb = 0.8;
    
}
