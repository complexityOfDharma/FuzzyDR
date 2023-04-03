/*
 * FuzzyDR: An agent-based model of institutions and application of fuzzy logic for modeling
 * individual deontic reasoning. A simple commons model is presented. The research aim is to
 * investigate the ability for agents to adapt their ability to deliberate their compliance
 * with their institutional environment, both institutions-in-form and institutions-in-use.  
 * 
 * @author Brant Horio (2023). George Mason University
 * 
 */

package edu.gmu.fuzzydr.controller;

import java.io.IOException;
import java.util.ArrayList;

import edu.gmu.fuzzydr.collectives.Agent;
import edu.gmu.fuzzydr.collectives.Resource;
import edu.gmu.fuzzydr.constitutional.ADICO;
import edu.gmu.fuzzydr.loaders.AgentLoader;
import sim.engine.SimState;
import sim.engine.Steppable;

public class FuzzyDRController extends SimState{

	private static final long serialVersionUID = 1L;
	public static int UID = 0;
	
	public static ArrayList<Agent> masterList_Agents = new ArrayList<Agent>();
    //public static HashMap<Integer, Agent> masterMap_Agents = new HashMap<Integer, Agent>();
	
	//public static ArrayList<Resource> masterList_Resources = new ArrayList<Resource>();
	public static Resource commons;
	
	public static ADICO adico_1;  // TODO: move this to Agent class? maybe have each agent own an ADICO, and it get updated after fuzzyDR
	
    //public static int agentPopulation = 1;
    //public static int agentInitialEnergy = 10;
    
    //public static int resourceCarryingCapacity = 10;
    
    public int countExpired = 0;    // agents who have expired due to no remaining energy.
	
	 /** Default constructor. */
    public FuzzyDRController() { super(0); };
	
    public FuzzyDRController(long seed) throws IOException {
    	super(seed);
    	
    	System.out.println("\n\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    	System.out.println("FuzzyDR: AGENT-BASED INSTITUTIONAL MODELING AND FUZZY DEONTIC REASONING");
    	System.out.println("Exploring Adaptive Agent Reasoning within Institutional Environments");
    	System.out.println("");
    	System.out.println("An Applicaton to a Simple Model of the Commons");
    	System.out.println("");
    	System.out.println("@author: Brant Horio, George Mason University, 2023");
    	System.out.println("");
    	//System.out.println("Starting simulation --- Scenario: ");    // + ModelConfigUtil.scenario + " ___");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        
    	initialize();
    }
    
    
    private void initialize() throws IOException {
    	
    	// clear out any necessary collections.
    	masterList_Agents.clear();
    	//masterMap_Agents.clear();
    	//masterList_Resources.clear();
    	
    	// clear out output collections
    	
    	// instantiate model objects and add them to the schedule.
    	instantiatePopulation();
    	
    	// instantiate the global resource and add it to the schedule.
    	instantiateResource();
    	
    	// any other global initializations (e.g., ADICO rule assignments)
    	instantiateInstitutions();
    	
    }
    
    private void instantiatePopulation() throws IOException {
    
    	AgentLoader aL = new AgentLoader();
    	aL.loadAgents();
    	
    	// loop through agents and assign their initial starting state.
    	
		
    	System.out.println("... agent instantiation complete: " + masterList_Agents.size() + " agent(s).");
    	
    }
    
    private void instantiateResource() throws IOException {
    	
    	//Resource r = new Resource(resourceCarryingCapacity);
    	//masterList_Resources.add(r);    	
    	
    	commons = new Resource(Config.resourceCarryingCapacity);
    	
    	System.out.println("... instantiation of the commons complete: " + commons.getResourceLevel() + " total energy units.");
    }
    
    private void instantiateInstitutions() throws IOException {
    	
    	adico_1 = new ADICO(null, null, null, null, 1, null, null, null, 0, null, 0);
    	
    	// TODO: consider adding each instantiated ADICO statement to a master list to loop over at a later time.
    	
    	DEBUG: System.out.println("\nInstitution: adico1 -- draw resources of " + adico_1.I_quantity + " with every time step.\n");
    	
    }
    
    @SuppressWarnings("serial")
	public void start() {
		super.start();
		
		System.out.println("\nSimulation run starting ...\n");
		
		// loop through agents and add them to the schedule.
		for (Agent a : masterList_Agents) {
			schedule.scheduleRepeating(a, 0, 1.0);
		}
		
		// add the commons to the schedule.
		schedule.scheduleRepeating(commons, 1, 1.0);
		//for (Resource r : masterList_Resources) {
		//	schedule.scheduleRepeating(r, 1, 1.0);
		//}
		
		// set conditions to terminate simulation run.
		schedule.scheduleRepeating(0, 3, new Steppable() {
			public void step(SimState state) {
				
				countExpired = 0;
				
				// update system level counts
				for (Agent a : masterList_Agents) {
					if (a.getEnergy() == 0) {
						countExpired++;
					}
				}
				
				// system print statements for overall model statistics
				
				if (countExpired == Config.agentPopulation) {
					System.out.println("");
					System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                    System.out.println("Simulation terminated... agent population has expired.");
                    state.kill();
				}
			}
		});
		
		
    }
    
    public void finish() {
		super.finish();
		
		// TODO: generate the final output file of results.
	}
	
    
	/**
	 * Simulation main.
	 * @param args
	 */
	public static void main(String[] args) {
		doLoop(FuzzyDRController.class, args);
		System.exit(0);
		
	}
    
	
}
