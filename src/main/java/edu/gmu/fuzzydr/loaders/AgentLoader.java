package edu.gmu.fuzzydr.loaders;

import java.io.IOException;

import edu.gmu.fuzzydr.collectives.Agent;
import edu.gmu.fuzzydr.controller.Config;
import edu.gmu.fuzzydr.controller.FuzzyDRController;

public class AgentLoader {

	/**
	 * Instantiate an agent and add them to the master list.
	 * @throws IOException
	 */
	public void loadAgents() throws IOException {
		for (int i = 1; i <= Config.agentPopulation; i++) {
			Agent a = new Agent();
			
			FuzzyDRController.masterList_Agents.add(a);
		}
	}
	
}
