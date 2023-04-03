package edu.gmu.fuzzydr.collectives;

import edu.gmu.fuzzydr.controller.Config;
import edu.gmu.fuzzydr.controller.FuzzyDRController;
import edu.gmu.fuzzydr.controller.SimUtil;
import sim.engine.SimState;
import sim.engine.Steppable;

public class Agent implements Steppable {

	private static final long serialVersionUID = 1L;

	public FuzzyDRController fuzzyDRController;
	
	// Agent attributes
	private int agentID;
	private double energy;
	private double energyConsumption = 1;       // energy units lost per time step.
	
	// TODO: need some way to operationalize ADICO and strategies/norms... e.g., how much to consume as dictate by institution
	
	
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
				
		DEBUG: System.out.println("Agent: " + getAgentID() + ", energy level is: " + getEnergy());
		
	}
	
	public void harvest(double e, double harvestTarget) {
		
		// compute the expected remaining if a harvest was conducted.
		double _remaining = FuzzyDRController.commons.getResourceLevel() - this.energyConsumption;
		
		DEBUG: System.out.println("The expected remaining resources if Agent harvests is: " + _remaining);
		
		// evaluate if harvest if possible (expected remaining is positive quantity), and update agent and commons
		//if (FuzzyDRController.commons.getResourceLevel() > 0) {
		
		if (_remaining > 0) {      // after harvest, the commons is not totally depleted.
		
			// decrement the commons by the successful harvest.
			//FuzzyDRController.commons.setResourceLevel(FuzzyDRController.commons.getResourceLevel() - this.energyConsumption);
			FuzzyDRController.commons.setResourceLevel(_remaining);
			
			// update Agent's energy levels by the successful harvest.
			double _energyGain = this.getEnergy() + this.energyConsumption; 
			this.setEnergy(_energyGain);
			
			DEBUG: System.out.println("... Decision to harvest. Harvested: " + this.energyConsumption + ", energy gained is:" + this.energyConsumption);
		} else {
			DEBUG: System.out.println("... Decision to *** NOT *** harvest. Not enough remaining resources.");
		}
	}
	
	
	public void updateEnergyLevels(double e) {
		this.setEnergy(e - Config.agentEnergyLossPerStep);
		
		//if (state.schedule.getSteps() > 0) {
		//	this.setEnergy(e - energyConsumption);
		//}
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
	
	
	
}
