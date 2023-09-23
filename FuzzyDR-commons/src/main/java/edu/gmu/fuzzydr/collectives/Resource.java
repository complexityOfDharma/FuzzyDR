package edu.gmu.fuzzydr.collectives;

import edu.gmu.fuzzydr.controller.Config;
import edu.gmu.fuzzydr.controller.FuzzyDRController;
import sim.engine.SimState;
import sim.engine.Steppable;

public class Resource implements Steppable{
	
	private static final long serialVersionUID = 1L;
	public double resourceLevel;
	
	/**
	 * Constructor.
	 * @param K (carrying capacity of the commons)
	 */
	public Resource(double K) {
		this.resourceLevel = K;
		
	}
	

	@Override
	public void step(SimState arg0) {
		
		// Resource is replenished by a regrowth rule. Resources are decremented from the Agent class in their Step method.
		regrowth();
		
		DEBUG: System.out.println("Harvest completed and regrowth. Remaining resources are: " + this.getResourceLevel() + "\n");
	}
	
	public void regrowth() {
		
		// Compute the regrowth amount.
		double _growth = (Config.commonsRegrowthRate * FuzzyDRController.commons.getResourceLevel()) * 
				(1 - (FuzzyDRController.commons.getResourceLevel() / Config.resourceCarryingCapacity));
		
		// Update the commons resource levels with the new growth.
		this.setResourceLevel(this.getResourceLevel() + _growth);
		//DEBUG: System.out.println("... Regrowth process completed. Commons are replenished by: " + _growth);
	}
	
	public double getResourceLevel() {
		return this.resourceLevel;
	}

	public void setResourceLevel(double resourceLevel) {
		this.resourceLevel = resourceLevel;
	}
	

		
	
}