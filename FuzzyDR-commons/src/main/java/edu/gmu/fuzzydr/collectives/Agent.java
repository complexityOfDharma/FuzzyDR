package edu.gmu.fuzzydr.collectives;

import edu.gmu.fuzzydr.controller.Config;
import edu.gmu.fuzzydr.controller.FuzzyDRController;
import edu.gmu.fuzzydr.controller.SimUtil;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;

public class Agent implements Steppable { //, Stoppable {

	private static final long serialVersionUID = 1L;

	public FuzzyDRController fuzzyDRController;
	
	private Stoppable stopper;
	
	// Agent attributes
	private int agentID;
	private double energy;
	
	private double energyConsumption = Config.agentEnergyLossPerStep;       // Energy units lost per time step. Customization by Agent type/ENUM possible in Config. 
	
	private boolean isDead = false;
	
	// TODO: need some way to operationalize ADICO and strategies/norms... e.g., how much to consume as dictated by institution
	
	
	// Default constructor.
	public Agent() {
		
		// generate a random UID.
		this.agentID = SimUtil.generateUID();
		
		// agent's energy level is initialized.
		this.energy = Config.agentInitialEnergy;
		
	}

	@Override
	public void step(SimState state) {
		
		// decrement agent energy level for this time step.
		updateEnergyLevels(this.energy);
		
		// if possible, conduct a harvest.
		harvest(this.energy, FuzzyDRController.adico_1.getI_quantity());
				
		//DEBUG: System.out.println("Agent: " + getAgentID() + ", energy level is: " + getEnergy());
		
		// STOPPING CONDITION:
		// Given energy update at start of step and accounting for any possible harvest afterward, determine if agent should be removed.
		if (this.getEnergy() <= 0) {
			
			// avoid null pointer exception
			if (stopper != null) {
				this.cleanup();   // remove references to this agent.
				
				//TODO: determine if the stop() method removes agents totally from memory, since keep a list of all agents was meant for stats keeping purposes, and active map for looping over those not dead.
				//TODO: if the above is true and agents are removed, one possible adjustment is to just turn off the stopper.stop()?
				stopper.stop();   // take agent off schedule.
				
			}
		}
	}
	
	public void harvest(double e, double harvestTarget) {
		
		// compute the expected remaining if a harvest was conducted.
		double _remaining = FuzzyDRController.commons.getResourceLevel() - this.energyConsumption;
		
		//DEBUG: System.out.println("The expected remaining resources if Agent harvests is: " + _remaining);
		
		// evaluate if harvest if possible (expected remaining is positive quantity), and update agent and commons
		//if (FuzzyDRController.commons.getResourceLevel() > 0) {
		
		if (_remaining > 0) {      // after harvest, the commons is not totally depleted.
		
			// decrement the commons by the successful harvest.
			//FuzzyDRController.commons.setResourceLevel(FuzzyDRController.commons.getResourceLevel() - this.energyConsumption);
			FuzzyDRController.commons.setResourceLevel(_remaining);
			
			// update Agent's energy levels by the successful harvest.
			double _energyGain = this.getEnergy() + this.energyConsumption; 
			this.setEnergy(_energyGain);
			
			//DEBUG: System.out.println("... Decision to harvest. Harvested: " + this.energyConsumption + ", energy gained is:" + this.energyConsumption);
		} else {
			//DEBUG: System.out.println("... Decision to *** NOT *** harvest. Not enough remaining resources.");
		}
	}
	
	
	public void assessDeltaParameter() {
		
		//TODO: run method here for internal delta calculation
		
		//TODO: run method here for external delta calculation
		
		//TODO: include state variables for delta parameters?
		
		
	}
	
	
	public void evalCompliance() {
		
		//TODO: invoke fuzzyDR here and draw from current state deltas
		
	}
	
	
	public void updateEnergyLevels(double e) {
		this.setEnergy(e - Config.agentEnergyLossPerStep);
		
		//if (state.schedule.getSteps() > 0) {
		//	this.setEnergy(e - energyConsumption);
		//}
	}
	
	public void cleanup() {
		
		this.setDead(true);
		
		// Set any object references to null (e.g., myObject = null;)
		// remove the agent from the HashMap of active agents (still alive).
		FuzzyDRController.masterMap_ActiveAgents.remove(this.agentID);
		
		// TODO: set up debug runs that use a much larger population of agents, limit the resource to 1, and see if population dies off.
		DEBUG: System.out.println("Agent " + this.agentID + " has been cleaned up and removed from the simulation. Remaining active agents are: " + FuzzyDRController.masterMap_ActiveAgents.size());
	}

	public void setStoppable(Stoppable s) {
		this.stopper = s;
	}
	
	public int getAgentID() { 
		return this.agentID; 
	}

	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public boolean isDead() {
		return isDead;
	}

	public void setDead(boolean isDead) {
		this.isDead = isDead;
	}

}
