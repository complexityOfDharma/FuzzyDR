package edu.gmu.fuzzydr.controller;

import java.io.IOException;
import java.util.ArrayList;

import edu.gmu.fuzzydr.collectives.Agent;
import edu.gmu.fuzzydr.collectives.ValidationAgent;
import edu.gmu.fuzzydr.constitutional.ADICO;
import edu.gmu.fuzzydr.loaders.AgentLoader;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;

public class ValidationController extends SimState{
	
	private static final long serialVersionUID = 1L;
	public static int UID = 0;
	
	public ArrayList<ValidationAgent> masterList_Agents = new ArrayList<ValidationAgent>();

	public ADICO adico_1;  // TODO: move this to Agent class? maybe have each agent own an ADICO, and it get updated after fuzzyDR
	   
	/** Default constructor. */
	public ValidationController() { 
	    super(Config.RANDOM_SEED); 
	};
	
	
	public ValidationController(long seed) throws IOException {
	    super(seed);
	   	System.out.println(">>> starting lvl 0 validation run ...\n");
	       
	    initialize();
	    	
	}
	    
	private void initialize() throws IOException {
	    	
		// clear out any necessary collections.
		masterList_Agents.clear();
		
		// instantiate model objects and add them to the schedule.
		instantiatePopulation();
		    	
		// any other global initializations (e.g., ADICO rule assignments).
		instantiateInstitutions();
	    	
	}
	    
	private void instantiatePopulation() throws IOException {
	    
		
		for (int i = 1; i <= Config.agentPopulation; i++) {
			ValidationAgent va = new ValidationAgent();
				
			// add agent to the master list for end of run statistics methods and logging.
			this.masterList_Agents.add(va);
			System.out.println("... instantiated new agent:" + va.getAgentID() + ",   archetype:" + va.getArchetype() + ",   energy:" + va.getEnergy() + ",   consumption target:" + va.getConsumptionTarget());
		}
		    	
	    System.out.println("\n>>> agent instantiation complete: " + masterList_Agents.size() + " agent(s).");
	}
	    
	    
	@SuppressWarnings("unused")
	private void instantiateInstitutions() throws IOException {
	    	
		adico_1 = new ADICO(1, null, null, null, null, 500, null, null, null, 0, null, 0);
	    	
	    DEBUG: System.out.println("\n>>> institution instantiated: ADICO -- 'draw resources of " + adico_1.I_quantity + " with every time step.'\n");
	}
	    
	
	    
	    @SuppressWarnings("serial")
		public void start() {
			super.start();
			
			System.out.println("\nSimulation run starting ...\n");
			
			// loop through agents and add them to the schedule.
			for (ValidationAgent va : masterList_Agents) {
				
				// Schedule each agent.
				schedule.scheduleRepeating(va, 0, 1.0);
			
			}
			
			// Schedule a one-time event to check for simulation termination condition, at time specified by first argument.
		    schedule.scheduleRepeating(0, 1, (Steppable) new Steppable() {
		        public void step(SimState state) {
		        	
		        	/*
		        	for (ValidationAgent va : masterList_Agents) {
			        	if (va.getAgentID() == 0) {
				    		DEBUG: System.out.println("\nLevel_0 Validation --- Scenario 1:");
				    		DEBUG: System.out.println("\n... For AgentID=0:");
				    		DEBUG: System.out.println("... >>> delta_i evaluation --- input: " + va.getDelta_i_in() + "   --->   out: " + va.getAgreement_delta_i() + ".");
				    	}
		        	}
		        	*/
			        	
		        	
		        	if (schedule.getTime() >= 0) {
						System.out.println("\nSimulation time reached at timeStep:" + schedule.getTime() + " >>> Terminating simulation.");
						state.finish();
					}
			        	
			            //System.out.println("Simulation time reached at timeStep:" + schedule.getTime() + " >>> Terminating simulation.");
			            //state.finish();  // Ensure this method properly terminates the simulation.
			            // Alternatively, if you directly manage the simulation loop, set a flag to stop the loop.
		        	//}
		        }
		    });
		
			// set conditions to terminate simulation run.
			/*
		    schedule.scheduleRepeating(0, 3, new Steppable() {
				public void step(SimState state) {
					
					if (schedule.getTime() >= 2) {
						System.out.println("Simulation terminated.");
						state.kill();
					}
				}
				
			});
			*/
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
		doLoop(ValidationController.class, args);
		System.exit(0);
			
	}
}
