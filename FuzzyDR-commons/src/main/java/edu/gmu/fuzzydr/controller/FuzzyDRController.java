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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.gmu.fuzzydr.collectives.Agent;
import edu.gmu.fuzzydr.collectives.Resource;
import edu.gmu.fuzzydr.constitutional.ADICO;
import edu.gmu.fuzzydr.loaders.AgentLoader;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.network.Edge;
import sim.field.network.Network;

public class FuzzyDRController extends SimState{

	private static final long serialVersionUID = 1L;
	public static int UID = 0;
	
	public static ArrayList<Agent> masterList_Agents = new ArrayList<Agent>();
    public static HashMap<Integer, Agent> masterMap_ActiveAgents = new HashMap<Integer, Agent>();
	
	//public static ArrayList<Resource> masterList_Resources = new ArrayList<Resource>();
	public static Resource commons;
	
	// TODO: might need a dynamic list of ADICOs... list? Map?
	public static ADICO adico_1;  // TODO: move this to Agent class? maybe have each agent own an ADICO, and it get updated after fuzzyDR
	
    //public static int agentPopulation = 1;
    //public static int agentInitialEnergy = 10;
    
    //public static int resourceCarryingCapacity = 10;
    
    public int countExpired = 0;    // agents who have expired due to no remaining energy, initialized to no deaths.
	
	 /** Default constructor. */
    public FuzzyDRController() { super(0); };
	
    public FuzzyDRController(long seed) throws IOException {
    	super(seed);
    	
    	System.out.println("\n\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    	System.out.println("FuzzyDR-commons: AGENT-BASED INSTITUTIONAL MODELING AND FUZZY DEONTIC REASONING");
    	System.out.println("Exploring Adaptive Agent Reasoning within Institutional Environments");
    	System.out.println("");
    	System.out.println("An Applicaton to a Simple Model of the Commons");
    	System.out.println("");
    	System.out.println("@author: Brant Horio, George Mason University, 2023");
    	System.out.println("");
    	//System.out.println("Starting simulation --- Scenario: ");    // + ModelConfigUtil.scenario + " ___");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        
    	initialize();
    	
    	

    }
    
    
    private void initialize() throws IOException {
    	
    	// clear out any necessary collections.
    	masterList_Agents.clear();
    	masterMap_ActiveAgents.clear();
    	//masterList_Resources.clear();
    	
    	// clear out output collections
    	
    	// instantiate model objects and add them to the schedule.
    	instantiatePopulation();
    	
    	// instantiate the global resource and add it to the schedule.
    	instantiateResource();
    	
    	// any other global initializations (e.g., ADICO rule assignments)
    	instantiateInstitutions();
    	
    	//TODO: do small world network connections
    	
    	
    	
    }
    
    private void instantiatePopulation() throws IOException {
    
    	AgentLoader aL = new AgentLoader();
    	aL.loadAgents();
    	
    	// TODO: loop through agents and assign their initial starting state?
    	
		System.out.println("... agent instantiation complete: " + masterList_Agents.size() + " agent(s).");
    	
    }
    
    private void instantiateResource() throws IOException {
    	
    	//Resource r = new Resource(resourceCarryingCapacity);
    	//masterList_Resources.add(r);    	
    	
    	commons = new Resource(Config.resourceCarryingCapacity);
    	
    	System.out.println("... instantiation of the commons complete: " + commons.getResourceLevel() + " total energy units.");
    }
    
    private void instantiateInstitutions() throws IOException {
    	
    	adico_1 = new ADICO(1, null, null, null, null, 1, null, null, null, 0, null, 0);
    	
    	// TODO: consider adding each instantiated ADICO statement to a master list to loop over at a later time.
    	
    	//DEBUG: System.out.println("\nInstitution: adico1 -- draw resources of " + adico_1.I_quantity + " with every time step.\n");
    	
    }
    
    private void smallWorldNetworkConnect() throws IOException {
    	
    	//TODO: !!! NOTE --- this looks like it won't reference Agent collections, and thus not meaningfully link the agents.
    	
    	Network network = new Network(false);  // 'false' for undirected network
    	
    	//TODO: !!! If we remove an agent from the simulation, what are implications to the network and must that be updated/cleared too.
    	
    	// create the network
    	for (int i = 0; i < Config.agentPopulation; i++) {
    		for (int j = i + 1; j < i + Config.avgNumNeighbors / 2; j++) {
    			int neighbor = j % Config.agentPopulation;
    			network.addEdge(i, neighbor, network);
    		}
    	}
    	
    	// rewire the network
    	// TODO: should this be pulled out as a separate method so we can rewire during the run and make the network connections adaptive
    	// TODO: is there a way to link rewiring probability to agent's current state, and preferred links to similar agents? Maybe loop over only agents with same archetype characteristics.
    	
    	for (int i = 0; i < Config.agentPopulation; i++) {
    		for (Object edgeObj : network.getEdgesOut(i)) {
    			Edge edge = (Edge) edgeObj;
    			if (random.nextDouble() < Config.rewiringProb) {
    				int newNeighbor = random.nextInt(Config.agentPopulation);
    				network.addEdge(i, newNeighbor, network);
    				network.removeEdge(edge);
    			}
    		}
    	}
    }
    
    
    @SuppressWarnings("serial")
	public void start() {
		super.start();
		
		System.out.println("\nSimulation run starting ...\n");
		
		// loop through agents and add them to the schedule.
		for (Agent a : masterList_Agents) {
			
			// Schedule each agent.
			// Preferred (thread-safe) procedure using the Stoppable object returned by scheduleRepeating(), allowing later calls to the stop() method on that Stoppable object.
			Stoppable s = schedule.scheduleRepeating(a, 0, 1.0);
			
			// Store corresponding Stoppable object as instance variable within the Agent object. Inside Agent's step() method, stop() invoked on "death" condition.
			a.setStoppable(s);
		}
		
		// add the commons to the schedule.
		schedule.scheduleRepeating(commons, 1, 1.0);
		//for (Resource r : masterList_Resources) {
		//	schedule.scheduleRepeating(r, 1, 1.0);
		//}
		
		// set conditions to terminate simulation run.
		schedule.scheduleRepeating(0, 3, new Steppable() {
			public void step(SimState state) {
				
				// countExpired = 0; // shouldn't this be initialized at the beginning and outside of the Step()?
				
				// TODO: this is insufficient to track losses and expired agents. Need to remove them from masterList if they die.
				// update system level counts
				//for (Agent a : masterList_Agents) {
				
				// TODO: can't I just kill off agents in the Agent's step method if they drop to 0 energy, remove references, and use a check here solely on the Map size()?
				// then I don't even need the countExpired counter.
				
				
				///--for (Map.Entry<Integer, Agent> entry : masterMap_ActiveAgents.entrySet()) {
					
				///--	Agent a = entry.getValue();
					
					//DEBUG: System.out.println("In the Map ForEach loop and currently on agent " + a.getAgentID() + ".");
					
				///--	if (a.isDead()) {
				///--		countExpired++;
						
						
						//masterList_Agents.remove(a);
						//DEBUG: System.out.println("updated master list of agents is of size: " + masterList_Agents.size());
				///--	}
				
					
					/*
					if (a.getEnergy() == 0) {
						countExpired++;
						
						// TODO: verify this is correct.
						// TODO: a.cleanup();
						// maybe add the agent to an "active" list that is used to loop over, with the masterList being one to use for population stats.
					}
					*/
			///--}
				
				// system print statements for overall model statistics
				
				int expired = Config.agentPopulation - masterMap_ActiveAgents.size();
				//DEBUG: System.out.println("countExpired = " + countExpired);
				DEBUG: System.out.println("End of time step " + schedule.getSteps() + ": countExpired = " + expired + "\n");
				
				
				//if (countExpired == Config.agentPopulation) {
				if (expired == Config.agentPopulation) {
					System.out.println("");
					System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                    System.out.println("Simulation terminated... " + expired + " of " + masterList_Agents.size() + " agents have expired and were removed from the simulation after " + (int) schedule.getTime() + " time steps.");
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
