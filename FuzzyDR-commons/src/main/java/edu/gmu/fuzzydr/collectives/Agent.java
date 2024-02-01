package edu.gmu.fuzzydr.collectives;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionPieceWiseLinear;
import net.sourceforge.jFuzzyLogic.plot.JFuzzyChart;
import net.sourceforge.jFuzzyLogic.rule.LinguisticTerm;
import net.sourceforge.jFuzzyLogic.rule.Variable;

import edu.gmu.fuzzydr.controller.Config;
import edu.gmu.fuzzydr.controller.FCLCodeGenerator;
import edu.gmu.fuzzydr.controller.FuzzyDRController;
import edu.gmu.fuzzydr.controller.SimUtil;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.network.Edge;
import sim.field.network.Network;
import sim.util.Bag;

public class Agent implements Steppable { //, Stoppable {

	private static final long serialVersionUID = 1L;

	public FuzzyDRController fuzzyDRController;
	
	private Stoppable stopper;
	
	// Agent attributes
	private int agentID;
	private double energy;
	private double agreement;
	
	private double avgNeighborEnergy;
	
	private FIS fis;
	private FunctionBlock functionBlock;
	private Variable selfEnergyVar, neighborEnergyVar, agreementVar; 
	
	
	private double locX;
	private double locY;
	
	//private double energyConsumption = Config.agentEnergyLossPerStep;       // Energy units lost per time step. Customization by Agent type/ENUM possible in Config. 
	private double consumptionTarget;	// agent's policy for consumption (either aligned to institution, or own-strategy).
	
	public List<Agent> neighbors = new ArrayList<>();
	
	private boolean isDead = false;
	
	// TODO: need some way to operationalize ADICO and strategies/norms... e.g., how much to consume as dictated by institution
	
	
	// Default constructor.
	public Agent() {
		
		// generate a random UID.
		this.agentID = SimUtil.generateUID();
		
		// agent's energy level is initialized.
		//this.energy = Config.agentInitialEnergy;			// all population starts at the same level.
		this.energy = Config.agentInitialEnergy * Config.RANDOM_GENERATOR.nextDouble();		// population starts at random level below initial max.
		// TODO: new initialization scenario here that can spike a condition for a few agents (e.g., ID's 0 - 10) 
		
		// default agreement.
		// TODO: make this more deliberate based on persona or other data about the agent.
		// TODO: consider making these initial opinions based on the scenario or edge case being tested... e.g., normally distributed around agreement/embrace institution.
		this.setAgreement(Config.RANDOM_GENERATOR.nextDouble());
		// TODO: at instantiation, set consumption target variable to ADICO, and call internal variable vs ADICO. As agreement checks happen, update internal back to ADICO or continue to modify
		
		// default consumption.
		this.setConsumptionTarget(Config.consumptionLevel);		// initialized to ADICO consumption, but can be overridden with an agent's own-strategy.
		
		
		// ------- !!! Set up Fuzzy Inference System for the agent. -------

		// load generic FCL as a template for Agents.
		String fclString = Config.genericAgentFCLPath;  // to be modified as necessary as Agent archetypes are explored.
		FCLCodeGenerator codeGenerator = new FCLCodeGenerator(fclString);
		fis = codeGenerator.loadFCL();
				
		functionBlock = fis.getFunctionBlock("agent");
        selfEnergyVar = functionBlock.getVariable("selfEnergy");
        neighborEnergyVar = functionBlock.getVariable("neighborEnergy");
        agreementVar = functionBlock.getVariable("agreement");
		
        //DEBUG: System.out.println("Printing out the FIS for agent: " + this.agentID);
		//DEBUG: System.out.println(fis);
		
        //DEBUG: JFuzzyChart.get().chart(functionBlock);
	}

	@Override
	public void step(SimState state) {
		
		FuzzyDRController fuzzyDR = (FuzzyDRController) state;
		
		// decrement agent energy level for this time step.
		decrementEnergyLevels(this.energy);
		
		// ------- !!! Harvest and update self-state and world-state of common pool. -------
				
		// If possible, conduct a harvest.
		double _resourceLevel = fuzzyDR.commons.getResourceLevel();
		double _remaining = _resourceLevel;   // the amount remaining in the common pool after agent's harvest.
		
		// based on experiment runs with or without fuzzyDR
		if (Config.isFuzzyRun) {
			// active fuzzyDR:  use fuzzyDR to assess agreement with current action policy (either institution or self).
			
			// TODO: evaluate agreement levels.
			// TODO: do it in a way that allows me to measure agreement-with-ADICO, noting that agent may be in 'agreement' but w.r.t. self-policy and not ADICO.
			
			// ------- !!! Evaluation of agreement with the institution (or current behavioral). -------
			
			// update and evaluate decision making.
			double _agree = this.evaluateCompliance();
			//DEBUG: System.out.println("Agent: " + this.getAgentID() + " is evaluating their agreement level as: " + _agree);
			//DEBUG: JFuzzyChart.get().chart(agreementVar, agreementVar.getDefuzzifier(), true); 
						
			// TODO: Based on agreement (or disagreement), run methods to determine action.
			
			// < insert action logic here to decide on new harvest target >
			// < insert method here for harvest( ... parameterized for the new action ... ).
			
		} else {
			// not active fuzzyDR: assumes uniform compliance with the institution and agent will target consumption to match institution prescription.
			double _target = fuzzyDR.adico_1.getI_quantity();  // the amount to harvest via the ADICO policy
		
			_remaining = harvest(_resourceLevel, this.energy, _target);
		}
		
		
		// after the agent's harvest, update the common pool resource level.
		updateCommonPoolLevels(state, _remaining);
		
		//DEBUG: System.out.println("Agent: " + getAgentID() + ", energy level is: " + getEnergy());
		
		
		// ------- !!! Assess the final outcomes, reassess beliefs, and consider modification of membership functions. -------
		if (Config.isFuzzyRun) {
			
			//  < insert logic here to evaluate potential modification of membership functions. > 
			
		}
		
		// ------- !!! Log outcomes -------
		// Log file entry and if applicable, record final entry for agent before they are removed from system.
		FuzzyDRController.logEntries.add(this.generateLogEntry(state));
					
		// ------- !!! AGENT TERMINATION CONDITION: Remove agent if self-energy is depleted. -------
		// Assuming a 'wasteland' model, agents die off if no energy remains (energy is not allowed to go negative.
		if (this.getEnergy() <= 0) {
			
			// avoid null pointer exception
			if (stopper != null) {
				this.cleanup(state);   // remove references to this agent.
				
				stopper.stop();   // take agent off schedule.
			}
		}
	}
	
	public double harvest(double resourceLevel, double energyLevel, double harvestTarget) {
		
		// TODO: !!!!! harvestTarget should be agent's decided consumption... either per ADICO, or if rejected in earlier time steps, some overridden consumption level.
		
		double _harvested = 0;  // the amount that was ultimately harvested (or not) by the agent.
		// compute the expected remaining if a harvest was conducted.
		//double _remaining = FuzzyDRController.commons.getResourceLevel() - this.energyConsumption;
		//double _remaining = resourceLevel - this.energyConsumption;
		double _remaining = resourceLevel - harvestTarget;
				
		//DEBUG: System.out.println("The expected remaining resources if Agent harvests is: " + _remaining);
		
		// evaluate if harvest if possible (expected remaining is positive quantity), and update agent and commons
		//if (FuzzyDRController.commons.getResourceLevel() > 0) {
		
		
		// TODO: here's where we should invoke the fuzzy evaluation and navigate rest of the harvesting decision based on outcomes.
		
		
		if (_remaining > 0) {      // implies that after harvest, the commons is not totally depleted after harvesting what is prescribed by the ADICO
			
			// TODO:  might need several more nested if's to invoke fuzzy evaluation for how much to harvest, vs only if can't get target
		
			// decrement the commons by the successful harvest.
			//FuzzyDRController.commons.setResourceLevel(FuzzyDRController.commons.getResourceLevel() - this.energyConsumption);
			//FuzzyDRController.commons.setResourceLevel(_remaining);
			
			//fuzzyDR.commons.setResourceLevel(_remaining);
			
			// update Agent's energy levels by the successful harvest.
			//double _energyGain = this.getEnergy() + this.energyConsumption; 
			//double _energyGain = this.getEnergy() + harvestTarget;   
			//this.setEnergy(_energyGain);
			
			_harvested = harvestTarget;  //TODO: note that this just means we can harvest according to the ADICO, but not necessarily what is NEEDED.
			updateEnergyFromHarvest(this.energy, _harvested);  // update Agent's energy level from whatever they were able to harvest.
			
			// return the remaining resource level after the successful harvest to update the pool resource level.
			//DEBUG: System.out.println("... Decision to harvest. Harvested: " + this.energyConsumption + ", energy gained is:" + this.energyConsumption);
			return _remaining;
			
		} else {
			
			_remaining = resourceLevel;   // implies that if the agent's ADICO amount to harvest is not available, this agent is selfish and takes all that remains.
			
			//DEBUG: System.out.println("... Decision to *** NOT *** harvest. Not enough remaining resources.");
			return _remaining;
		}
	}
	
	public double calcAvgNeighborEnergy() {
		
		double _sum = 0;
		double _avg = 0;
		
		for (Agent a : this.neighbors) {
			_sum += a.getEnergy();
		}
		
		_avg = _sum / this.neighbors.size();
		return _avg;
	}
	
	
	public void assessDeltaParameters() {
		
		//TODO: run method here for internal delta calculation
		
		//TODO: run method here for external delta calculation
		
		//TODO: include state variables for delta parameters?
		
		
	}
	
	//TODO: either make this method evaluate agreement for every delta parameter, or create 3 separate methods.
	/**
	 * Evaluate fuzzy rules and obtain output.
	 * @param selfEnergy
	 * @param neighborEnergy
	 * @return
	 */
    public double evaluateAgreement(double selfEnergy, double neighborEnergy) {
        selfEnergyVar.setValue(selfEnergy);
        neighborEnergyVar.setValue(neighborEnergy);
        functionBlock.evaluate();
        return agreementVar.getValue();
    }
	
    // TODO: pull in the input parameters and convert them appropriately for feeding into the delta internal parameter FIS. 
    public double evaluateDelta_i() {
    	double _agreement = 0;
    	
    	return _agreement;
    }
    
    // TODO: pull in the input parameters and convert them appropriately for feeding into the delta external parameter FIS.
    public double evaluateDelta_e() {
    	double _agreement = 0;
    	
    	return _agreement;
    }
    
    // TODO: pull in the input parameters and convert them appropriately for feeding into the delta orElse parameter FIS.
    public double evaluateDelta_o() {
    	double _agreement = 0;
    	
    	return _agreement;
    }
    
 // TODO: pull in the input parameters and convert them appropriately for feeding into the delta orElse parameter FIS.
    public double evaluateDelta_tree() {
    	double _agreement = 0;
    	
    	return _agreement;
    }
    
	public double evaluateCompliance() {
		
		//TODO: invoke fuzzyDR here and draw from current state deltas
		// something about looking at the current energy level, and then seeing if harvest via ADICO is 'good' or 'not'
		
		
		// TODO:  need to normalize the energy levels into 10.
		double _selfEnergy = (this.getEnergy()) / Config.agentInitialEnergy;    // as a percentage of agent energy max.
		double _neighborEnergy = (this.calcAvgNeighborEnergy()) / Config.agentInitialEnergy;  // as a percentage of agent energy max.
		
		//DEBUG: System.out.println("_selfEnergy = " + _selfEnergy + ", _neighborEnergy" + _neighborEnergy);
		
		//fis.setVariable("selfEnergy", _selfEnergy);
		//fis.setVariable("neighborEnergy", _neighborEnergy);
		//fis.evaluate();
		
		//double _agreement = agreementVar.getValue();
		//double _agreement = fis.getVariable("agreement").getValue();
		//return _agreement;
		
		double _agreement = evaluateAgreement(_selfEnergy, _neighborEnergy);
		return _agreement;
		
		
		// TODO:  this is the roll against agreementValue logic here.... !!!
		
		// TODO: !!!!!!!!!!!!!!!!!!!!!!!!! roll and now act 
		// can call newRuleGenertaion() from here maybe if comes up as non-comply.
		
	}
	
	
	public void decideAction() {
		
		// TODO: put in logic here that translates agreement levels to harvest levels (e.g., imitation, to meet need exactly for survival, etc)
		// or... it could be new action innovation. Does not need to be fancy. Maybe just 'imitate' or 'exactly fufill need.'
	}
	
	
	public void newRuleGeneration() {
		
		// TODO:  In case the agent decides to break with institution... need logic here to develop new rules for them to follow
		
	}
	
	
	/**
	 * Decrement the agent's current energy level by the energy loss per time step that is specified in the Config file.
	 * @param e
	 */
	public void decrementEnergyLevels(double e) {
		this.setEnergy(e - Config.agentEnergyLossPerStep);
		
		//if (state.schedule.getSteps() > 0) { this.setEnergy(e - energyConsumption); }
	}
	
	/**
	 * Update the agent's energy level based on the amount of the successful harvest.
	 * @param e
	 * @param harvested
	 */
	public void updateEnergyFromHarvest(double e, double harvested) {
		
		// update Agent's energy levels by the successful harvest.
		//double _energyGain = this.getEnergy() + this.energyConsumption; 
		
		double _energyGain = this.getEnergy() + harvested;   
		this.setEnergy(_energyGain);
	}
	
	/**
	 * Update the common pool resource level w.r.t. the amount consumed by the agent during this time step.
	 * @param state
	 * @param newLevel
	 */
	public void updateCommonPoolLevels(SimState state, double newLevel) {
		FuzzyDRController fuzzyDR = (FuzzyDRController) state;
		
		fuzzyDR.commons.setResourceLevel(newLevel);
		
	}
	
    // Modify the fuzzy set parameters for the 'low' term of selfEnergy variable.
    /*
	public void modifySelfEnergyLow(double a, double b, double c) {
        FuzzySet fuzzySetLow = selfEnergyVar.getLinguisticTerm("low").getTerm();
        fuzzySetLow.setPoint(0, a);
        fuzzySetLow.setPoint(1, b);
        fuzzySetLow.setPoint(2, c);
    }
    */
	
	/**
	 * For dynamic adjustment of fuzzy membership sets during simulation runtime.
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @param z1
	 * @param z2
	 */
    public void modifySelfEnergyLow(double x1, double x2, double y1, double y2, double z1, double z2) {
        
    	// TODO: add a new argument that allows a String input for the linguistic term to use
    	
    	//DEBUG: JFuzzyChart.get().chart(functionBlock);
    	
    	LinguisticTerm low = selfEnergyVar.getLinguisticTerm("low");
        
        MembershipFunctionPieceWiseLinear mf = (MembershipFunctionPieceWiseLinear) low.getMembershipFunction();
        
        /*
        double a1 = mf.getParameter(0);
        double a2 = mf.getParameter(1);
        double b1 = mf.getParameter(2);
        double b2 = mf.getParameter(3);
        double c1 = mf.getParameter(4);
        double c2 = mf.getParameter(5);
		*/
        
        //DEBUG: System.out.println("membership function low: (" + a1 + "," + a2 + ") (" + b1 + "," + b2 + ") (" + c1 + "," + c2 + ")");
        
        mf.setParameter(0, x1);
        mf.setParameter(1, x2);
        mf.setParameter(2, y1);
        mf.setParameter(3, y2);
        mf.setParameter(4, z1);
        mf.setParameter(5, z2);
        
        low.setMembershipFunction(mf);
        
        /*
        a1 = mf.getParameter(0);
        a2 = mf.getParameter(1);
        b1 = mf.getParameter(2);
        b2 = mf.getParameter(3);
        c1 = mf.getParameter(4);
        c2 = mf.getParameter(5);
        */
        
        //DEBUG: System.out.println("membership function low: (" + a1 + "," + a2 + ") (" + b1 + "," + b2 + ") (" + c1 + "," + c2 + ")");
        
        //DEBUG: JFuzzyChart.get().chart(functionBlock);
        
    }

    	//mf.setPoint(0, a1, a2);  // Update coordinates of point 0
    	//mf.setPoint(1, b1, b2);  // Update coordinates of point 1
    	//mf.setPoint(2, c1, c2);  // Update coordinates of point 2

    /**
     * Method for cleaning up dead agents.
     * @param state
     */
    public void cleanup(SimState state) {
		
		FuzzyDRController fuzzyDR = (FuzzyDRController) state;
		
		this.setDead(true);
		
		// Set any object references to null (e.g., myObject = null;)
		// remove the agent from the HashMap of active agents (still alive).
		FuzzyDRController.masterMap_ActiveAgents.remove(this.agentID);
		
		//DEBUG: System.out.println("Agent " + this.agentID + " is in process of cleanup... neighbors list cleanup in progress. Current list size is: " + this.neighbors.size());
		// loop through this agent's neighbors list, find the connections, and go through each of their neighbor lists and remove this agent.
		for (Agent neighbor : this.neighbors) {
			//DEBUG: System.out.println("... neighbor: " + neighbor.agentID + " Current neighbors list size is: " + neighbor.neighbors.size());
			neighbor.neighbors.remove(this);
			//DEBUG: System.out.println("... neighbor: " + neighbor.agentID + " updated neighbors list size is: " + neighbor.neighbors.size());
		}
		
		this.neighbors.clear();
		//DEBUG: System.out.println("Agent " + this.agentID + " is in process of cleanup... neighbors lists purged of agent reference. Verifying neighbors list is size: " + this.neighbors.size());
		
		// clear the agent from visualization objects.
		fuzzyDR.world.remove(this);
		removeAgentEdges(this, fuzzyDR.network);
		
		
		// TODO: set up debug runs that use a much larger population of agents, limit the resource to 1, and see if population dies off.
		//DEBUG: System.out.println("Agent " + this.agentID + " has been cleaned up and removed from the simulation. Remaining active agents are: " + FuzzyDRController.masterMap_ActiveAgents.size());
	}

	public void removeAgentEdges(Agent agent, Network network) {
	    Bag edges = network.getEdgesOut(agent);
	    for(int i = 0; i < edges.numObjs; i++) {
	        Edge edge = (Edge) edges.objs[i];
	        network.removeEdge(edge);
	    }
	    edges = network.getEdgesIn(agent);
	    for(int i = 0; i < edges.numObjs; i++) {
	        Edge edge = (Edge) edges.objs[i];
	        network.removeEdge(edge);
	    }
	}

	public String generateLogEntry(SimState state) {
		FuzzyDRController fuzzyDR = (FuzzyDRController) state;
		
		double e;
		if (energy < 0) {
			e = 0;
		} else {
			e = energy;
		}
		
        //return fuzzyDR.schedule.getSteps() + "," + agentID + "," + e + "," + agreement;
        //return fuzzyDR.schedule.getSteps() + "," + getAgentID() + "," + e + "," + getAgreement();
		return Config.batchRunID + "," + fuzzyDR.schedule.getSteps() + "," + agentID + "," + e + "," + fuzzyDR.commons.getResourceLevel();
    }
	
	public void setStoppable(Stoppable s) {
		this.stopper = s;
	}
	
	public int getAgentID() { 
		return this.agentID; 
	}

	public double getLocX() {
		return locX;
	}

	public void setLocX(double locX) {
		this.locX = locX;
	}

	public double getLocY() {
		return locY;
	}

	public void setLocY(double locY) {
		this.locY = locY;
	}

	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public double getAgreement() {
		return agreement;
	}

	public void setAgreement(double agreement) {
		this.agreement = agreement;
	}

	public double getConsumptionTarget() {
		return consumptionTarget;
	}

	public void setConsumptionTarget(double consumptionTarget) {
		this.consumptionTarget = consumptionTarget;
	}

	public boolean isDead() {
		return isDead;
	}

	public void setDead(boolean isDead) {
		this.isDead = isDead;
	}

}
