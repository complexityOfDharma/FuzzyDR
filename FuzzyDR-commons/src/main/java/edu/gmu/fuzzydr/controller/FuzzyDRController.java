/*
 * FuzzyDR: An agent-based model of institutions and application of fuzzy logic for modeling
 * individual deontic reasoning. A simple commons model is presented. The research aim is to
 * investigate the ability for agents to adapt their ability to deliberate their compliance
 * with their institutional environment, both institutions-in-form and institutions-in-use.  
 * 
 * @author Brant Horio (2024). George Mason University
 * 
 */

package edu.gmu.fuzzydr.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYSeries;

import edu.gmu.fuzzydr.collectives.Agent;
import edu.gmu.fuzzydr.collectives.Resource;
import edu.gmu.fuzzydr.constitutional.ADICO;
import edu.gmu.fuzzydr.loaders.AgentLoader;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.continuous.Continuous2D;
import sim.field.network.Edge;
import sim.field.network.Network;
import sim.util.Double2D;

public class FuzzyDRController extends SimState{

	private static final long serialVersionUID = 1L;
	public static int UID = 0;
	private int batchRunID;
	
	public Continuous2D world = new Continuous2D(1.0, Config.WIDTH, Config.HEIGHT);
	public Network network = new Network(false);
	
	public static ArrayList<Agent> masterList_Agents = new ArrayList<Agent>();
    public static HashMap<Integer, Agent> masterMap_ActiveAgents = new HashMap<Integer, Agent>();
	
	//public static ArrayList<Resource> masterList_Resources = new ArrayList<Resource>();
	//public static Resource commons;
	public Resource commons;
	
	// TODO: might need a dynamic list of ADICOs... list? Map?
	//public static ADICO adico_1;  // TODO: move this to Agent class? maybe have each agent own an ADICO, and it get updated after fuzzyDR
	public ADICO adico_1;  // TODO: move this to Agent class? maybe have each agent own an ADICO, and it get updated after fuzzyDR
	
    // *** now being defined in Config... can delete
	//public static int agentPopulation = 1;
    //public static int agentInitialEnergy = 10;
    //public static int resourceCarryingCapacity = 10;
    
    public int countExpired = 0;    // agents who have expired due to no remaining energy, initialized to no deaths.
    
    public XYSeries resourcePoolLevels;
    
    private SimulationLogger logger;
    public static List<String> logEntries = new ArrayList<>();
    
    
	 /** Default constructor. */
    public FuzzyDRController() { 
    	super(Config.RANDOM_SEED); 
    };
	
    public FuzzyDRController(long seed) throws IOException {
    	super(seed);
    	resourcePoolLevels = new XYSeries("Resource Pool Levels");
    	
    	System.out.println("running one-parameter FuzzyDRController constructor...");
    	
    	System.out.println("\n\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    	System.out.println("FuzzyDR-commons: AGENT-BASED INSTITUTIONAL MODELING AND FUZZY DEONTIC REASONING");
    	System.out.println("Exploring Adaptive Agent Reasoning within Institutional Environments");
    	System.out.println("");
    	System.out.println("An Applicaton to a Simple Model of the Commons");
    	System.out.println("");
    	System.out.println("@author: Brant Horio, George Mason University, 2024");
    	System.out.println("");
    	System.out.println("Starting simulation --- Scenario: " + Config.scenarioID);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        
    	initialize();
    	
    	// setup the Logger.
    	this.logger = new SimulationLogger("src/main/resources/sim_log_singleRun.csv");		// single run.
    }
    
    /**
     * Constructor.
     * @param seed
     * @throws IOException
     */
    public FuzzyDRController(long seed, String logFilename) throws IOException {
    	super(seed);
    	
    	resourcePoolLevels = new XYSeries("Resource Pool Levels");
    	
    	System.out.println("\n\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    	System.out.println("FuzzyDR-commons: AGENT-BASED INSTITUTIONAL MODELING AND FUZZY DEONTIC REASONING");
    	System.out.println("Exploring Adaptive Agent Reasoning within Institutional Environments");
    	System.out.println("");
    	System.out.println("An Applicaton to a Simple Model of the Commons");
    	System.out.println("");
    	System.out.println("@author: Brant Horio, George Mason University, 2024");
    	System.out.println("");
    	System.out.println("Starting simulation --- Scenario: " + Config.scenarioID);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        
    	initialize();
    	
    	// setup the Logger.
    	if (Config.isBatchRun) {
    		this.logger = new SimulationLogger(logFilename);		// dynamic filenames for batched runs.
    	} else {
    		this.logger = new SimulationLogger("src/main/resources/sim_log_singleRun.csv");		// single run.
    	}
    
    }
    
    
    private void initialize() throws IOException {
    	
    	// clear out world.
    	world.clear();
    	
    	// clear out any necessary collections.
    	masterList_Agents.clear();
    	masterMap_ActiveAgents.clear();
    	//masterList_Resources.clear();
    	
    	// clear out output collections
    	logEntries.clear();
    	
    	// instantiate model objects and add them to the schedule.
    	instantiatePopulation();
    	
    	// instantiate the global resource and add it to the schedule.
    	instantiateResource();
    	
    	// any other global initializations (e.g., ADICO rule assignments).
    	instantiateInstitutions();
    	
    	// connect all agents via small world network.
    	// TODO: should some agent personas have less than a standard number of initial connections? e.g., some more social, others more private.
    	buildSmallWorldNetwork(masterList_Agents, Config.initNeighbors, Config.rewiringProb);
    	
    	// DEBUG for checking small world networks
    	/*
    	for (Agent a : masterList_Agents) {
    		System.out.println("  Network build complete: agent " + a.getAgentID() + ": neighbors list size of " + a.neighbors.size());
    		for (Agent aaa : a.neighbors) {
    			System.out.println("     neighbor: " + aaa.getAgentID());
    		}
    	}
    	*/
    }
    
    private void instantiatePopulation() throws IOException {
    
    	AgentLoader aL = new AgentLoader();
    	aL.loadAgents();
    	
    	// TODO: loop through agents and assign their initial starting state? (add it to the loop below)... personas, etc.
    	
    	// Update the agent specifics.
    	for (Agent a : masterList_Agents) {
    		
    		//give the agent a random location in the world.
    		double locX = world.getWidth() * Config.RANDOM_GENERATOR.nextDouble();
    		double locY = world.getHeight() * Config.RANDOM_GENERATOR.nextDouble();
    		
    		a.setLocX(locX);
    		a.setLocY(locY);
    		
    		Double2D location = new Double2D(
    				a.getLocX(),
    				a.getLocY());
    		
    		world.setObjectLocation(a, location);
			
			//DEBUG: System.out.println("Agent " + a.getAgentID() + " location in world is: " + location.toString());
			
			// TODO: do this more deliberately
	    	// set up default agreement.
	    	//a.setAgreement(Config.RANDOM_GENERATOR.nextDouble() * 0.5);
	    	
	    	//DEBUG: System.out.println();
	    	//DEBUG: System.out.println("Agent " + a.getAgentID() + " initial agreement level: " + a.getAgreement());
	    	
	    	// TODO: set up persona and based on that, assign default agreement.
    	}
    	
    	System.out.println("... agent instantiation complete: " + masterList_Agents.size() + " agent(s).");
    }
    
    private void instantiateResource() throws IOException {
    	
    	//Resource r = new Resource(resourceCarryingCapacity);
    	//masterList_Resources.add(r);    	
    	
    	commons = new Resource(Config.resourceCarryingCapacity);
    	
    	System.out.println("... instantiation of the commons complete: " + commons.getResourceLevel() + " total energy units.");
    }
    
    private void instantiateInstitutions() throws IOException {
    	
    	double _consumptionLevel = Config.consumptionLevel;
    	
    	adico_1 = new ADICO(1, null, null, null, null, _consumptionLevel, null, null, null, 0, null, 0);
    	
    	// TODO: consider adding each instantiated ADICO statement to a master list to loop over at a later time.
    	
    	//DEBUG: System.out.println("\nInstitution: adico1 -- draw resources of " + adico_1.I_quantity + " with every time step.\n");
    }
    
    /**
     * Small world network building via Watts-Strogatz. Starting with creating a regular ring lattice where each node is connected to its k nearest neighbors.
     * Re-wiring each edge with a probability p, which involves replacing it with a new edge that connects the node to a randomly chosen node.
     * @param agents
     * @param initialNeighbors
     * @param rewireProbability
     * @throws IOException
     */
    public void buildSmallWorldNetwork(List<Agent> agents, int initialNeighbors, double rewireProbability) throws IOException {
        
    	//TODO: !!! If we remove an agent from the simulation, what are implications to the network and must that be updated/cleared too.
    	
    	int numAgents = agents.size();
    	
        // Start with a ring lattice where each agent is connected to its initialNeighbors nearest neighbors
        for (int i = 0; i < numAgents; i++) {
            Agent current = agents.get(i);
            for (int j = 1; j <= initialNeighbors / 2; j++) {
                Agent neighbor1 = agents.get((i + j) % numAgents);
                Agent neighbor2 = agents.get((i - j + numAgents) % numAgents);
                current.neighbors.add(neighbor1);
                current.neighbors.add(neighbor2);
            }
        }

        // Rewire the network with the given probability
        for (int i = 0; i < numAgents; i++) {
            Agent current = agents.get(i);
	
            for (int j = 1; j <= initialNeighbors / 2; j++) {
                if (random.nextDouble() < rewireProbability) {
                    int newNeighborIndex = random.nextInt(numAgents);
                    Agent newNeighbor = agents.get(newNeighborIndex);
	
                    // Ensure the new neighbor isn't the current agent and isn't already a neighbor
                    while (newNeighbor == current || current.neighbors.contains(newNeighbor)) {
                        newNeighborIndex = random.nextInt(numAgents);
                        newNeighbor = agents.get(newNeighborIndex);
                    }
	
                    // Replace the old neighbor with the new one
                    Agent oldNeighbor = agents.get((i + j) % numAgents);
                    current.neighbors.remove(oldNeighbor);
                    oldNeighbor.neighbors.remove(current); // Remove current from old neighbor's list
                    current.neighbors.add(newNeighbor);
                    newNeighbor.neighbors.add(current); // Add current to new neighbor's list
                }
            }
        }
        
        // add the agent neighbors to the Network object to track links for the visualization.
        for (Agent agent1 : agents) {
            for (Agent agent2 : agent1.neighbors) {
                if (!network.getEdgesOut(agent1).contains(agent2)) {
                    network.addEdge(agent1, agent2, null);
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
				
				// update plot data for during runtime.
				resourcePoolLevels.add(schedule.getSteps(), commons.resourceLevel);
				
				// end of time step population-level stats.
				int _remainingAgents = masterMap_ActiveAgents.size();
				int _expired = Config.agentPopulation - _remainingAgents;
				
				double _sumEnergy = 0;
				double _sumAgreement = 0;
				double _avgEnergy;
				double _avgAgreement;
				for (Agent a : masterMap_ActiveAgents.values()) {
					_sumEnergy = _sumEnergy + a.getEnergy();
					_sumAgreement = _sumAgreement + a.getAgreeemnt_institution();
				}
				
				_avgEnergy = _sumEnergy / _remainingAgents;
				_avgAgreement = _sumAgreement / _remainingAgents;
				
				DEBUG: System.out.println("End of time step " + schedule.getSteps() + ": populationRemaining = " + _remainingAgents + " : countExpired = " + _expired);
				DEBUG: System.out.println("... for remaining population, avgerage energy = " + _avgEnergy + ", and average institutional agreement = " + _avgAgreement + "\n");
				
				if (_expired == Config.agentPopulation || schedule.getTime() >= Config.terminationStepCount) {
				    System.out.println("");
				    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
				    if (_expired == Config.agentPopulation) {
				        System.out.println("Simulation terminated... All agents have expired and were removed from the simulation after " + (int) schedule.getTime() + " time steps.");
				    } else {
				        System.out.println("Simulation terminated... The time step limit of " + Config.terminationStepCount + " has been reached.");
				    }
				    
				    logger.logEntries(logEntries);
				    
				    state.kill();
				}
			}
		});
    }
    
    public void finish() {
		super.finish();
	}
	
	/**
	 * Simulation main.
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		//doLoop(FuzzyDRController.class, args);
		//System.exit(0);
		
		if (Config.isBatchRun) {
		    BatchRunManager.runBatchExperiments(args);
		} else {
		    doLoop(FuzzyDRController.class, args);
		}
		
		System.exit(0);
	}
	
}
