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
	
    // Local RNG for this agent.
    private ec.util.MersenneTwisterFast localRNG;
	
	// Agent attributes.
	private int agentID;
	private double energy;
	
	//private double energyConsumption = Config.agentEnergyLossPerStep;       // Energy units lost per time step. Customization by Agent type/ENUM possible in Config. 
	private double consumptionTarget;	// agent's own self-policy for consumption (either aligned to institution, or own-strategy).
	
	private double agreement;
	private double agreeemnt_institution;		// overall p(obey) for given action policy.
	private double agreement_delta_i;			// agreement index [0, 1] for delta parameter: internal.
	private double agreement_delta_e;			// agreement index [0, 1] for delta parameter: external.
	private double agreement_delta_o;			// agreement index [0, 1] for delta parameter: or-else.
	
	public List<Agent> neighbors = new ArrayList<>();
	
	private boolean isDead = false;
	
	// FCL fields and activation for fuzzyDR.
	private boolean isFuzzyDRActivated;			// for localized activation of fuzzyDR for a given agent.
	
	private FIS fis_delta_i;
	private FunctionBlock fb_delta_i;
	private Variable diFIS_in_selfEnergy, diFIS_in_selfConsumption, diFIS_out_agreement;
	
	private FIS fis_delta_e;
	private FunctionBlock fb_delta_e;
	private Variable deFIS_in_networkState, deFIS_in_actionConsensus, deFIS_out_agreement;
	
	private FIS fis_delta_o;
	private FunctionBlock fb_delta_o;
	private Variable doFIS_in_expectedImpact, doFIS_in_sanctionRisk, doFIS_out_agreement;
	
	private FIS fis_delta_tree;
	private FunctionBlock fb_delta_tree;
	private Variable dtreeFIS_in_delta_i, dtreeFIS_in_delta_e, dtreeFIS_in_delta_o, dtreeFIS_out_agreement;
	
	// GUI fields
	private double locX;
	private double locY;
	
	
	// Default constructor.
	public Agent() {
		// generate a random UID.
		this.agentID = SimUtil.generateUID();
		
		// Initialize local RNG with a unique seed based on generated AgentID.
        this.localRNG = new ec.util.MersenneTwisterFast(this.agentID);
        
		// default activation for individualized fuzzyDR.
		this.setFuzzyDRActivated(false);	// all population default to not-fuzzyDR on individual level. Config fuzzyDR-for-all setting can override for full population fuzzyDR runs.
		
		// customize agent(s) based on scenario and fuzzify the appropriate test agents with fuzzyDR parameterization.
		if ((Config.isScenarioRun) && (this.agentID == 0)) {				// given an experiment scenario run, parameterize Agent Zero for fuzzyDR.
			customizeFuzzyAgentForScenario(Config.scenarioID);				// this will initialize the target agent with all the scenario parameters, for either a control run or experiment run --- to be routed appropriately in step().
		} else {
			customizeNonFuzzyPopulationForScenario(Config.scenarioID);		// for either control case or configure rest of population to match desired scenario context.
		}
		
		// ------- !!! Set up Fuzzy Inference System for the agent. -------
		
		// based on experiment runs with or without fuzzyDR (w.r.t. fuzzyDR for all agents, or just this instance). BLUF: only load FCLs if needed for a given agent.
		if ((Config.isFuzzyDRforALL) || (this.isFuzzyDRActivated())) {
	        // --- load FCL for FIS delta parameter internal ---
	     	String _fclString_di = Config.delta_i_FCLPath;
	     	FCLCodeGenerator _codeGenerator = new FCLCodeGenerator(_fclString_di);
	     	fis_delta_i = _codeGenerator.loadFCL();
	     					
	     	fb_delta_i = fis_delta_i.getFunctionBlock("delta_internal");
	     	diFIS_in_selfEnergy = fb_delta_i.getVariable("selfEnergy");
	     	diFIS_in_selfConsumption = fb_delta_i.getVariable("selfConsumption");
	     	diFIS_out_agreement = fb_delta_i.getVariable("delta_i");
	        //DEBUG: JFuzzyChart.get().chart(fb_delta_i);
	        
	     	// --- load FCL for FIS delta parameter external ---
	     	String _fclString_de = Config.delta_e_FCLPath;
	     	_codeGenerator = new FCLCodeGenerator(_fclString_de);		// overwrite local var for loading new FCL on delta parameter external.
	     	fis_delta_e = _codeGenerator.loadFCL();
	     	
	        fb_delta_e = fis_delta_e.getFunctionBlock("delta_external");
	     	deFIS_in_networkState = fb_delta_e.getVariable("networkState");
	     	deFIS_in_actionConsensus = fb_delta_e.getVariable("actionConsensus");
	     	deFIS_out_agreement = fb_delta_e.getVariable("delta_e");
	        //DEBUG: JFuzzyChart.get().chart(fb_delta_e);
	     		     	
	     	// --- load FCL for FIS delta parameter or-Else ---
	     	String _fclString_do = Config.delta_o_FCLPath;
	     	_codeGenerator = new FCLCodeGenerator(_fclString_do);		// overwrite local var for loading new FCL on delta parameter external.
	     	fis_delta_o = _codeGenerator.loadFCL();
	     	
	        fb_delta_o = fis_delta_o.getFunctionBlock("delta_orelse");
	     	doFIS_in_expectedImpact = fb_delta_o.getVariable("expectedImpact");
	     	doFIS_in_sanctionRisk = fb_delta_o.getVariable("sanctionRisk");
	     	doFIS_out_agreement = fb_delta_o.getVariable("delta_o");
	        DEBUG: JFuzzyChart.get().chart(fb_delta_o);
	     	
	     	// --- load FCL for FIS tree to determine overall p(obey) ---
	     	String _fclString_dtree = Config.delta_tree_FCLPath;
	     	_codeGenerator = new FCLCodeGenerator(_fclString_dtree);		// overwrite local var for loading new FCL on delta parameter external.
	     	fis_delta_tree = _codeGenerator.loadFCL();
	     	
	        fb_delta_tree = fis_delta_tree.getFunctionBlock("delta_tree");
	     	dtreeFIS_in_delta_i = fb_delta_tree.getVariable("delta_i");
	     	dtreeFIS_in_delta_e = fb_delta_tree.getVariable("delta_e");
	     	dtreeFIS_in_delta_o = fb_delta_tree.getVariable("delta_o");
	     	dtreeFIS_out_agreement = fb_delta_tree.getVariable("p_obey");
	        DEBUG: JFuzzyChart.get().chart(fb_delta_tree);
	    }
	}
	
	/**
	 * Agent field overrides to meet experiment scenario context and conditions.
	 */
    private void customizeFuzzyAgentForScenario(int scenario) {
    	this.setFuzzyDRActivated(true); 	// Assume we activate this for agentID == 0 in all scenarios for simplicity.
    	
    	// ----- scenario specific settings for the fuzzyDR population -----
        switch (scenario) {
        	case 1:
                // Scenario 1 - delta_i: 'maintain status quo' : sufficient energy, compliant with institution, lean toward maintaining 'embrace institution.'
        		System.out.println("Loading Scenario 1 ('maintain status quo') for active fuzzyDR agentID:" + this.getAgentID() + ".\n");
        		
        		this.energy = 90;										// initialize a high energy state.
        		this.setAgreeemnt_institution(0.9);						// initialize a high agreement level that embraces the current institution.
                this.setConsumptionTarget(Config.consumptionLevel);		// initialize a consumption target that is aligned to institution prescription.
        		
                break;
            case 2:
                // Scenario 2 - delta_i: 'dire straits' : critically low energy levels, compliant with institution, lean toward 'reject institution.'
            	System.out.println("Loading Scenario 2 ('dire straits') for active fuzzyDR agentID:" + this.getAgentID() + ".\n");
            	
            	this.energy = 10;										// initialize a critically low energy state.
            	this.setAgreeemnt_institution(0.9);						// initialize a high agreement level that embraces the current institution.
                this.setConsumptionTarget(Config.consumptionLevel);		// initialize a consumption target that is aligned to institution prescription.
        		
                break;
            case 3:
                // Scenario 3 - delta_e: 'behind the pack' : low energy relative to others, different targets, lean toward 'reject institution.'
            	System.out.println("Loading Scenario 3 ('behind the pack') for active fuzzyDR agentID:" + this.getAgentID() + ".\n");
            	
            	this.energy = 10;										// initialize a critically low energy state.
            	this.setAgreeemnt_institution(0.9);						// initialize a high agreement level that embraces the current institution.
                this.setConsumptionTarget(Config.consumptionLevel);		// initialize a consumption target that is aligned to institution prescription.
        		
            	break;
            case 4:
            	// Scenario 4 - delta_e: 'king of the hill' : high energy relative to others, same targets, lean toward 'embrace institution.'
            	System.out.println("Loading Scenario 4 ('king of the hill') for active fuzzyDR agentID:" + this.getAgentID() + ".\n");
            	
            	this.energy = 90;										// initialize a high energy state.
            	this.setAgreeemnt_institution(0.9);						// initialize a high agreement level that embraces the current institution.
                this.setConsumptionTarget(Config.consumptionLevel);		// initialize a consumption target that is aligned to same as rest of population.
        		
                break;
            case 5:
                // Scenario 5 - delta_o: 'on the brink' : severe sanction, high risk to incur, lean toward 'embrace institution.'
            	System.out.println("Loading Scenario 5 ('on the brink') for active fuzzyDR agentID:" + this.getAgentID() + ".\n");
            	
            	this.energy = 50;										// initialize a medium energy state, to give room to maintain higher consumption levels.
            	this.setAgreeemnt_institution(0.9); 					// initialize high agreement. 
            	
            	// w.r.t. sanction ...
            	// !!! Or-else sanction hard-coded to be 'severe' w.r.t. some arbitrary scale (e.g., 90 out of 100), set in FuzzyDRController after ADICO object instantiation.
            	
            	// w.r.t. high risk ... setting consumption target to be higher than ADICO, putting agent in a high risk position of being sanctioned.
            	this.setConsumptionTarget(8);
            	
            	break;
            case 6:
                // Scenario 6 - delta_o: 'safe bet' : moderate sanction, low risk, lean toward 'ambivalent toward institution.'
            	System.out.println("Loading Scenario 6 ('safe bet') for active fuzzyDR agentID:" + this.getAgentID() + ".\n");
            	
            	this.energy = 50;										// initialize a medium energy state, to give room to maintain higher consumption levels.
            	this.setAgreeemnt_institution(0.9); 					// initialize high agreement. 
            	
            	// w.r.t. sanction ...
            	// !!! Or-else sanction hard-coded to be 'moderate' w.r.t. some arbitrary scale (e.g., 20 out of 100), set in FuzzyDRController after ADICO object instantiation.
            	
            	// w.r.t. risk ... setting consumption target to be set to ADICO, putting agent in a negligible risk position of being sanctioned.
            	this.setConsumptionTarget(Config.consumptionLevel);
            	
            	break;
            case 7:
                // Scenario 7 - full fuzzyDR all deltas : 'the good life' : moderate sanction, low sanction risk, internal and external support institution, or-else ambivalent, lean toward 'embrace institution.'
            	System.out.println("Loading Scenario 7 ('the good life') for active fuzzyDR agentID:" + this.getAgentID() + ".\n");
            	
            	this.energy = 90;										// initialize a high energy state.
        		this.setAgreeemnt_institution(0.9);						// initialize a high agreement level that embraces the current institution.
        		
        		// w.r.t. sanction ...
            	// !!! Or-else sanction hard-coded to be 'moderate' w.r.t. some arbitrary scale (e.g., 20 out of 100), set in FuzzyDRController after ADICO object instantiation.
            	
            	// w.r.t. risk ... setting consumption target to be set to ADICO, putting agent in a negligible risk position of being sanctioned.
            	this.setConsumptionTarget(Config.consumptionLevel);		// initialize a consumption target that is aligned to institution prescription.
        		
            	break;
            case 8:
                // Scenario 8 - full fuzzyDR all deltas : 'off-ramp' : moderate sanction, low sanction risk, internal and external reject institution, or-else ambivalent, lean toward 'reject institution.'
            	System.out.println("Loading Scenario 8 ('off-ramp') for active fuzzyDR agentID:" + this.getAgentID() + ".\n");
            	
            	this.energy = 10;										// initialize a critically low energy state.
            	this.setAgreeemnt_institution(0.9);						// initialize a high agreement level that embraces the current institution.
            	
            	// w.r.t. sanction ...
            	// !!! Or-else sanction hard-coded to be 'moderate' w.r.t. some arbitrary scale (e.g., 20 out of 100), set in FuzzyDRController after ADICO object instantiation.
            	
            	// w.r.t. risk ... setting consumption target to be set to ADICO, putting agent in a negligible risk position of being sanctioned.
            	this.setConsumptionTarget(Config.consumptionLevel);		// initialize a consumption target that is aligned to institution prescription.
        		
            	break;
            case 9:
            	// Scenario 9 - full fuzzyDR all deltas : 'conflicted tradeoff' : severe sanction, high sanction risk, internal and external reject institution, or-else supporting institution, lean toward 'embrace institution.'
            	System.out.println("Loading Scenario 9 ('off-ramp') for active fuzzyDR agentID:" + this.getAgentID() + ".\n");
            	
            	this.energy = 10;										// initialize a critically low energy state.
            	this.setAgreeemnt_institution(0.9);						// initialize a high agreement level that embraces the current institution.
            	
            	// w.r.t. sanction ...
            	// !!! Or-else sanction hard-coded to be 'severe' w.r.t. some arbitrary scale (e.g., 90 out of 100), set in FuzzyDRController after ADICO object instantiation.
            	
            	// w.r.t. high risk ... setting consumption target to be higher than ADICO, putting agent in a high risk position of being sanctioned.
            	this.setConsumptionTarget(5);		// higher than ADICO, but lower than the population.
            	
            	break;
            case 10:
            	// Scenario 10 - fully fuzzyDR all deltas - ALL AGENTS : 'hello fuzzy world' : initialized for collapse, observe impacts from fuzzyDR.
            	System.out.println("Loading Scenario 10 ('hello fuzzy world!') for active fuzzyDR agentID:" + this.getAgentID() + ".\n");
            	
            	
            	break;
            default:
                defaultParameterizationForAgents();
            	System.out.println("Agent " + this.getAgentID() + " has been initialized with default settings as an active fuzzyDR agent.");
            	break;
        }
    }

	/**
	 * Field overrides for rest of population that is not activated with fuzzyDR to meet experiment scenario context and conditions.
	 */
    private void customizeNonFuzzyPopulationForScenario(int scenario) {
    	// ----- scenario specific settings for non-fuzzyDR population -----
    	switch (scenario) {
        	case 1:
                // Scenario 1 - delta_i: 'maintain status quo'
        		defaultParameterizationForAgents();
                break;
            case 2:
                // Scenario 2 - delta_i: 'dire straits'
            	defaultParameterizationForAgents();
                break;
            case 3:
                // Scenario 3 - delta_e: 'behind the pack'
            	this.setEnergy(generateGaussian(90, 5, 80, 100));		// set rest of population to be advantaged.
            	this.setAgreeemnt_institution(0.5);		// doesn't matter for this scenario, but for convenience and clarity, set to around middle.
            	this.setConsumptionTarget(generateGaussian(3, 1, 2, 4));	// initialize a consumption target that is meaningfully different than the fuzzyDR agent.
            	//DEBUG: System.out.println("random consumption target for Scenario 3 for gaussian(3,1,2,4) is: " + this.getConsumptionTarget());
            	break;
            case 4:
                // Scenario 4 - delta_e: 'king of the hill'
            	this.setEnergy(generateGaussian(30, 5, 20, 40));		// set rest of population to be disadvantaged.
            	this.setAgreeemnt_institution(0.5);		// doesn't matter for this scenario, but for convenience and clarity, set to around middle.
            	this.setConsumptionTarget(Config.consumptionLevel);		// initialize a consumption target that is aligned to institution prescription.
        		
            	break;
            case 5:
                // Scenario 5 - delta_o: 'on the brink'
            	defaultParameterizationForAgents();
                break;
            case 6:
                // Scenario 6 - delta_o: 'safe bet'
            	defaultParameterizationForAgents();
                break;
            case 7:
                // Scenario 7 - fuzzyDR for all: 'the good life'
            	this.setEnergy(generateGaussian(30, 5, 20, 40));		// set rest of population to be disadvantaged.
            	this.setAgreeemnt_institution(0.5);		// doesn't matter for this scenario, but for convenience and clarity, set to around middle.
            	this.setConsumptionTarget(Config.consumptionLevel);		// initialize a consumption target that is aligned to institution prescription.
            	
            	break;
            case 8:
                // Scenario 8 - fuzzyDR for all: 'off-ramp'
            	this.setEnergy(generateGaussian(90, 5, 80, 100));		// set rest of population to be advantaged.
            	this.setAgreeemnt_institution(0.5);		// doesn't matter for this scenario, but for convenience and clarity, set to around middle.
            	this.setConsumptionTarget(generateGaussian(3, 1, 2, 4));	// initialize a consumption target that is meaningfully different than the fuzzyDR agent.
            	
            	break;
            case 9:
            	// Scenario 9 - fuzzyDR for all: 'conflicted tradeoff'
            	this.setEnergy(generateGaussian(90, 5, 80, 100));		// set rest of population to be advantaged.
            	this.setAgreeemnt_institution(0.5);		// doesn't matter for this scenario, but for convenience and clarity, set to around middle.
            	this.setConsumptionTarget(8);		// higher than ADICO, but also higher than fuzzyDR agent, setting up conditions for an advantaged network..
            	
            	break;
            default:
            	defaultParameterizationForAgents();
            	//DEBUG: System.out.println("Non-fuzzyDR agent has been initialized with default settings for energy, consumption targets, and institutional agreement.");
            	break;
        }
    }
    
    public void defaultParameterizationForAgents() {
    	// Default scenario or no customization : random energy over entire range, high institutional agreement, consumption targets aligned with institution.
    	this.energy = Config.agentInitialEnergy * this.localRNG.nextDouble();			// all population starts at random level below initial max specified in Config.
    	
    	//TODO: should the default agreement be more random an across the possible range of values?
    	this.setAgreeemnt_institution(generateGaussian(0.9, 0.05, 0.8, 1.0)); 	// Scenario Context: "population-in-high-compliance" : defaults all population to highly cluster around 0.9 mean agreement, within range [0.8, 1.0].
    	
    	this.setConsumptionTarget(Config.consumptionLevel);								// initialized to ADICO consumption, but can be overridden with an agent's own consumption policy as model progresses.
    	
    	//DEBUG: System.out.println("AgentID: " + this.agentID + ", and agreement: " + this.agreement_institution);
    }

	@Override
	public void step(SimState state) {
		FuzzyDRController fuzzyDR = (FuzzyDRController) state;
		
		// decrement agent energy level for this time step.
		//DEBUG: if (this.agentID==0) { System.out.println("Decrementing agent energy levels. Starting energy is: " + this.getEnergy()); }
		decrementEnergyLevels(this.energy);
		//DEBUG: if (this.agentID==0) { System.out.println("... and ending energy is: " + this.getEnergy()); }
		
		// ------- !!! Harvest and update self-state and world-state of common pool. -------
		double _resourceLevel = fuzzyDR.commons.getResourceLevel();			// resource pool level for current simulation state.
		double _remaining = _resourceLevel;   								// local variable to track the amount remaining in the common pool after agent's harvest.
		
		if (Config.isScenarioRun) {
			// running a specified experiment scenario.
			//DEBUG: if (this.agentID==0) { System.out.println("Starting conditional logic for this experiment scenario run."); }
			if (Config.isExperimentalControlRun) {
				// running active fuzzyDR agents, but without the fuzzyDR processes (no delta parameter checks) and assuming uniform compliance with institution.
				_remaining = harvest(_resourceLevel, this.energy, this.getConsumptionTarget());
				//DEBUG: if (this.agentID==0) { System.out.println("... Experimental control run is TRUE --- harvest completed without fuzzyDR methods: remaining pool resources of " + _remaining + "."); }
			} else {
				// running active fuzzyDR agents with all fuzzyDR processes (includes all delta parameter checks and resulting action decisions).
				//DEBUG: if (this.agentID==0) { System.out.println("... Experimental control run is FALSE --- full fuzzyDR methods for harvest actions by test agents."); }
				if ((Config.isFuzzyDRforALL) || (this.isFuzzyDRActivated())) {
					// all active fuzzyDR agents should initiate the fuzzyDR process.
					_remaining = runFuzzyDR(state, Config.scenarioID);		// all fuzzyDR processes, and to include running harvest.
					//DEBUG: if (this.agentID==0) { System.out.println("... ... fuzzyDR is active for this agent --- full fuzzyDR methods for harvest: remaining pool resources of " + _remaining + "."); }
				} else {	// this is for rest of population and not test agents or other agents specified for active fuzzyDR.
					// for the fuzzyDR experiment runs, the rest of the population should follow their consumption targets specified by the scenario parameterization.
					_remaining = harvest(_resourceLevel, this.energy, this.getConsumptionTarget());
					//DEBUG: if (this.agentID==0) { System.out.println("... ... !!! THIS SHOULD NOT COME UP, with Agent_0 having isFuzzyDRActivated, so should not get here."); }
				}
			}
		} else {
			// not running a specified experiment scenario, and this should follow institution prescriptions.
			_remaining = harvest(_resourceLevel, this.energy, this.getConsumptionTarget());
			//DEBUG: if (this.agentID==0) { System.out.println("Starting conditional logic for non-experiment scenario run."); }
		}
		
		/*
		// based on experiment runs with or without fuzzyDR (w.r.t. fuzzyDR for all agents, or just this instance).
		if ((Config.isFuzzyDRforALL) || (this.isFuzzyDRActivated())) {
			
			//TEMP:..............
			double _target = fuzzyDR.adico_1.getI_quantity();  // the amount to harvest via the ADICO policy
			_remaining = harvest(_resourceLevel, this.energy, _target);
			
			
		} else {
			// not active fuzzyDR: assumes uniform compliance with the institution and agent will target consumption to match institution prescription.
			double _target = fuzzyDR.adico_1.getI_quantity();  // the amount to harvest via the ADICO policy
			_remaining = harvest(_resourceLevel, this.energy, _target);
		}
		*/
		
		//DEBUG: System.out.println("Agent: " + getAgentID() + ", energy level is: " + getEnergy());
		
		// after the agent's successful harvest, update the common pool resource level.
		DEBUG: if (this.agentID==0) { System.out.println("... updating common pool levels after harvest. Prior resource levels: " + fuzzyDR.commons.getResourceLevel()); }
		updateCommonPoolLevels(state, _remaining);
		DEBUG: if (this.agentID==0) { System.out.println("... common pool updated to new level: " + fuzzyDR.commons.getResourceLevel()); }
		
		// ------- !!! Dissertation CH.6: Assess the final outcomes, reassess beliefs, and consider modification of membership functions. -------
		if (Config.isFuzzyDRforALL) { // OR only do this for fuzzified test agents.
			//  < insert logic here to evaluate potential modification of membership functions. > 
		}
		
		// TODO: < track agreement trends for institution, with 'reject' state being any consumption_target that is NOT-institution (e.g., agreement with current self-policy may be high, but still divergent from institution) >
		
		// ------- !!! Log outcomes -------
		// Log file entry and if applicable, record final entry for agent before they are removed from system.
		FuzzyDRController.logEntries.add(this.generateLogEntry(state));
					
		// ------- !!! AGENT TERMINATION CONDITION: Remove agent if self-energy is depleted to zero (not allowed to go negative). -------
		if (this.getEnergy() <= 0) {
			// avoid null pointer exception
			if (stopper != null) {
				this.cleanup(state);   // remove references to this agent.
				stopper.stop();   // take agent off schedule.
			}
		}
	}
	
	public double harvest(double resourceLevel, double energyLevel, double harvestTarget) {
		
		double _harvested = 0;										// the amount that will ultimately be harvested (or not) by the agent.
		double _target = harvestTarget;								// default target to input harvest target (prior to check on energy level and max limit).
		
		if ((this.getEnergy() + _target) > Config.agentInitialEnergy) {		// check for if gap between current state and energy max is more than harvestTarget.
			_target = Config.agentInitialEnergy - this.getEnergy();			// gap is smaller than harvestTarget, so only harvest enough to max out.
																			// TODO: allow agents to consume more than max? or ok... BLUF: exceed ADICo, but don't exceed agent energy max (physical ceiling).
		} else {
			_target = harvestTarget;
		}
		
		double _remaining = resourceLevel - _target;			// pro-active projection of what remains if harvest happens. 
		
		if (_remaining > 0) {      	// implies that after proposed harvest, the commons is not totally depleted.
			
			_harvested = _target;
			
			// update Agent's energy levels by the successful harvest.
			updateEnergyFromHarvest(this.energy, _harvested);  // update Agent's energy level from whatever they were able to harvest.
			
			// return the remaining resource level after the successful harvest to update the pool resource level.
			//DEBUG: System.out.println("... Decision to harvest. Harvested: " + this.energyConsumption + ", energy gained is:" + this.energyConsumption);
			return _remaining;
			
		} else {
			_remaining = resourceLevel;   	// implies that if the agent's ADICO amount to harvest is not available, this agent is selfish and takes all that remains.
			//DEBUG: System.out.println("... Decision to *** NOT *** harvest. Not enough remaining resources.");
			return _remaining;
		}
	}
	
    /**
     * FIS evaluation for delta parameter (internal) and return agreement index [0, 1] with respect to delta_i. 
     * @param selfEnergy
     * @param consumuptionAbove
     * @return
     */
    public double evaluateDelta_i(double selfEnergy, double consumuptionAbove) {
    	//DEBUG: System.out.println("AgentID: " + this.agentID + " is running evalauteDelta_i with arguments selfEnergy: " + selfEnergy + ", consumptionAbove: " + consumuptionAbove + ".");
    	diFIS_in_selfEnergy.setValue(selfEnergy);
    	diFIS_in_selfConsumption.setValue(consumuptionAbove);
    	fb_delta_i.evaluate();
    	
    	diFIS_out_agreement = fb_delta_i.getVariable("delta_i");
    	
    	DEBUG: System.out.println("... delta_i evaluation is: " + diFIS_out_agreement.getValue() + ".");
    	
    	//DEBUG: JFuzzyChart.get().chart(diFIS_out_agreement, diFIS_out_agreement.getDefuzzifier(), true);
    	//fb_delta_i.getVariable("delta_i").chartDefuzzifier(true);
    	
    	return diFIS_out_agreement.getValue();
    }
    
    /**
     * FIS evaluation for delta parameter (external) and return agreement index [0, 1] with respect to delta_e.
     * @param relativeState
     * @param actionConsensus
     * @return
     */
    public double evaluateDelta_e(double networkState, double actionConsensus) {
        deFIS_in_networkState.setValue(networkState);
     	deFIS_in_actionConsensus.setValue(actionConsensus);
     	fb_delta_e.evaluate();
     	
     	deFIS_out_agreement = fb_delta_e.getVariable("delta_e");
    	
    	DEBUG: System.out.println("... delta_e evaluation is: " + deFIS_out_agreement.getValue() + ".");
    	
     	return deFIS_out_agreement.getValue();
    }
    
    /**
     * FIS evaluation for delta parameter (or-else) and return agreement index [0, 1] with respect to delta_o.
     * @param expectedImpact
     * @param sanctionRisk
     * @return
     */
    public double evaluateDelta_o(double expectedImpact, double sanctionRisk) {
    	doFIS_in_expectedImpact.setValue(expectedImpact);;
     	doFIS_in_sanctionRisk.setValue(sanctionRisk);;
     	fb_delta_o.evaluate();
     	
     	doFIS_out_agreement = fb_delta_o.getVariable("delta_o");
    	
    	DEBUG: System.out.println("... delta_o evaluation is: " + doFIS_out_agreement.getValue() + ".");
     	
     	return doFIS_out_agreement.getValue();
    }
    
    /**
     * Fuzzy Tree for FIS evaluation for an overall delta parameter (internal + external + or-else) and return agreement index [0, 1] to be used to approximate p(obey).
     * @param delta_i
     * @param delta_e
     * @param delta_o
     * @return
     */
    public double evaluateDelta_tree(double delta_i, double delta_e, double delta_o) {
    	dtreeFIS_in_delta_i.setValue(delta_i);;
     	dtreeFIS_in_delta_e.setValue(delta_e);;
     	dtreeFIS_in_delta_o.setValue(delta_o);;
     	fb_delta_tree.evaluate();
     	
     	dtreeFIS_out_agreement = fb_delta_tree.getVariable("delta_tree");
    	
    	DEBUG: System.out.println("... delta_tree evaluation is: " + dtreeFIS_out_agreement.getValue() + ".");
    	
     	return dtreeFIS_out_agreement.getValue();
    }
    
    /**
     * Initiate fuzzy deontic reasoning for given agent, evaluating compliance with institutions based on delta parameter-specific fuzzy inference systems.
     */
    public double runFuzzyDR(SimState state, int scenario) {
    	FuzzyDRController fuzzyDR = (FuzzyDRController) state;
    	
    	DEBUG: System.out.println("AgentID:" + this.getAgentID() + ", now running fuzzyDR.");
    	
    	double _resourceLevel = fuzzyDR.commons.getResourceLevel();
    	double _remaining = 0;
    	
    	double _di = 0;
    	double _de = 0;
    	double _do = 0;
    	
    	double _pObey = 1;  // !!! TODO: remove this and just operate directly with agreement_institution.
    	
    	// !!! --- delta internal parameters.
    	double _energy = this.energy;		// as-is on range [0, 100].
    	double _consumptionAbove;			// should reflect how much in excess of prescribed ADICO amount.
    	double _temp = this.consumptionTarget - fuzzyDR.adico_1.getI_quantity();		// difference between agent consumption target and the institution prescription.
    	
    	if ( _temp >= 0) {
    		_consumptionAbove = _temp;		// difference is positive with agent consumption target matching or exceeding institution, per innovateNewAction, up to x10.  
    	} else {
    		_consumptionAbove = 0;			// in case where consumption is below what is specified by the institution, return a 0.
    	}
    	//DEBUG: System.out.println("... estimating second delta_i parameter for consumption-above. _temp = " + _temp + ", _consumptionAbove =" + _consumptionAbove + ".");
    	
    	
    	// !!! --- delta external parameters. (NOTE: depends upon neighbor population size > 0, so based on self network, and some random agent still remaining in run)
    	List<Agent> _expandedNeighbors = getExpandedNeighbors(state);		// create extended neighbors list.
    	double _avgNetworkEnergy = calcAvgEnergyOfExtendedNeighbors(_expandedNeighbors);
    	double _networkState = _avgNetworkEnergy - this.energy; 			// energy difference between average energy of extended neighbors and this agent's energy.
    	
    	double _avgNetworkConsumption = calcAvgConsumptionOfExtendedNeighbors(_expandedNeighbors);
    	double _actionConsensus = Math.abs(_avgNetworkConsumption - this.getConsumptionTarget());		// absolute value difference between self consumption target and the target of the neighbors.
    	//DEBUG: System.out.println("... calculating delta_e input fields: avgNetworkEnergy=" + _avgNetworkEnergy + ", this.energy=" + this.energy + ", _networkState=" + _networkState + ", _avgNetworkConsumption=" + _avgNetworkConsumption + ", this.consumptionTarget=" + this.getConsumptionTarget() + ", _actionConsensus=" + _actionConsensus);
    	
    	
    	// !!! --- delta or-else parameters.
    	double _expectedImpact = 0; 			// logic needs to go here to compute what I mean by expected-impact of sanctions.
    	double _sanctionRisk = 0;				// logic needs to go here to compute what I mean by sanction-risk.
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	// !!! --- fuzzyDR scenario specific logic.
    	switch (scenario) {
	    	case 1:
	            // Scenario 1 - delta_i: 'maintain status quo' : sufficient energy, compliant with institution, lean toward maintaining 'embrace institution.'
	    		System.out.println("... running Scenario 1 fuzzyDR - delta (internal only) for agentID:" + this.getAgentID() + ".\n");
	    		
	    		//DEBUG: System.out.println("running delta_i evaluation using arguments _energy: " + _energy + ", and _consumptionAbove" + _consumptionAbove + ".");
	        	this.setAgreement_delta_i(evaluateDelta_i(_energy, _consumptionAbove));
	        	//DEBUG: System.out.println("... running fuzzyDR: AgentID:" + this.getAgentID() + ", and just evaluated delta_i, resulting in an agreement index of: " + _di + ".");
	        	
	        	_pObey = this.getAgreement_delta_i();
	    		break;
	        case 2:
	            // Scenario 2 - delta_i: 'dire straits' : critically low energy levels, compliant with institution, lean toward 'reject institution.'
	        	System.out.println("... running Scenario 2 fuzzyDR - delta (internal only) for agentID:" + this.getAgentID() + ".\n");
	        	this.setAgreement_delta_i(evaluateDelta_i(_energy, _consumptionAbove));
	        	_pObey = this.getAgreement_delta_i();
	    		break;
	        case 3:
	            // Scenario 3 - delta_e: 'behind the pack' : low energy relative to others, different targets, lean toward 'reject institution.'
	        	System.out.println("... running Scenario 3 fuzzyDR - delta (external only) for agentID:" + this.getAgentID() + ".\n");
	        	this.setAgreement_delta_e(evaluateDelta_e(_networkState, _actionConsensus));
	        	_pObey = this.getAgreement_delta_e();
	        	break;
	        case 4:
	        	// Scenario 4 - delta_e: 'king of the hill' : high energy relative to others, same targets, lean toward 'embrace institution.'
	        	System.out.println("... running Scenario 4 fuzzyDR - delta (external only) for agentID:" + this.getAgentID() + ".\n");
	        	this.setAgreement_delta_e(evaluateDelta_e(_networkState, _actionConsensus));
	        	_pObey = this.getAgreement_delta_e();
	        	break;
	        case 5:
	            // Scenario 5 - delta_o:
	        	System.out.println("... running Scenario 5 fuzzyDR - delta (or-else only) for agentID:" + this.getAgentID() + ".\n");
	        	this.setAgreement_delta_o(evaluateDelta_o(_expectedImpact, _sanctionRisk));
	        	_pObey = this.getAgreement_delta_o();
	        	break;
	        case 6:
	            // Scenario 6 - delta_o:
	        	System.out.println("... running Scenario 6 fuzzyDR - delta (or-else only) for agentID:" + this.getAgentID() + ".\n");
	        	this.setAgreement_delta_o(evaluateDelta_o(_expectedImpact, _sanctionRisk));
	        	_pObey = this.getAgreement_delta_o();
	        	break;
	        case 7:
	            // Scenario 7 - full fuzzyDR for complex scenario 1:
	        	System.out.println("... running Scenario 7 fuzzyDR - complex scenario 1 - for agentID:" + this.getAgentID() + ".\n");
	        	_pObey = complexDeltaParameterEvaluation(_energy, _consumptionAbove, _networkState, _actionConsensus, _expectedImpact, _sanctionRisk);
	        	break;
	        case 8:
	            // Scenario 8 - full fuzzyDR for complex scenario 2: 
	        	System.out.println("... running Scenario 8 fuzzyDR - complex scenario 2 - for agentID:" + this.getAgentID() + ".\n");
	        	_pObey = complexDeltaParameterEvaluation(_energy, _consumptionAbove, _networkState, _actionConsensus, _expectedImpact, _sanctionRisk);
	        	break;
	        case 9:
	            // Scenario 9 - full fuzzyDR for complex scenario 3: 
	        	System.out.println("... running Scenario 9 fuzzyDR - complex scenario 3 - for agentID:" + this.getAgentID() + ".\n");
	        	_pObey = complexDeltaParameterEvaluation(_energy, _consumptionAbove, _networkState, _actionConsensus, _expectedImpact, _sanctionRisk);
	        	break;
	        case 10:
	        	// Scenario 10 - full fuzzyDR: 'hello fuzzy world' : fuzzyDR for entire population.
	        	System.out.println("... running Scenario 10 fuzzyDR - delta (full fuzzy tree FIS with internal + external + or-else) for agentID:" + this.getAgentID() + ".\n");
	        	_pObey = complexDeltaParameterEvaluation(_energy, _consumptionAbove, _networkState, _actionConsensus, _expectedImpact, _sanctionRisk);
	        	break;
	        default:
	            System.out.println("Agent " + this.getAgentID() + "Running default fuzzyDR - breaking from switch() loop and defaulting to pObey(1.0) ...");
	            // defaults to initialized _pObey value of 1.0, or assumes full agreement with the institution.
	        	break;
    	}
    	
    	this.setAgreeemnt_institution(_pObey);
    	//DEBUG: System.out.println("... institutional agreement set to p(obey), verifying with: " + this.getAgreeemnt_institution() + ", which should match p(obey).");
    	//DEBUG: System.out.println("... running action() to determine obey or break based on chance roll from the localRNG.");
    	_remaining = action(state, this.getAgreeemnt_institution(), _resourceLevel);
    	
    	return _remaining;
    }
    
    /**
     * For Scenarios which run full fuzzyDR on agents, this method returns _pObey as a function of specific deltas in isolation, or all together. subScenario to be specified in Config.
     * @param energy
     * @param consumptionAbove
     * @param networkState
     * @param actionConsensus
     * @param expectedImpact
     * @param sanctionRisk
     * @return
     */
    public double complexDeltaParameterEvaluation(double energy, double consumptionAbove, double networkState, double actionConsensus, double expectedImpact, double sanctionRisk) {
    	double _di = 0;
    	double _de = 0;
    	double _do = 0;
    	double _dtree = 0;
    	double _pObey = 1;		// default to full agreement.
    	
    	double _energy = energy;
    	double _consumptionAbove = consumptionAbove;
    	double _networkState = networkState;
    	double _actionConsensus = actionConsensus;
    	double _expectedImpact = expectedImpact;
    	double _sanctionRisk = sanctionRisk;
    	
    	_di = evaluateDelta_i(_energy, _consumptionAbove);
    	_de = evaluateDelta_e(_networkState, _actionConsensus);
    	_do = evaluateDelta_o(_expectedImpact, _sanctionRisk);
    	_dtree = evaluateDelta_tree(_di, _de, _do);
    	
    	switch (Config.subScenarioID) {
    	case 1:
    		// delta_i only.
    		_pObey = _di;
    		DEBUG: System.out.println("... ... running Scenario:" + Config.scenarioID + ", subScenario:" + Config.subScenarioID + " --- (VERIFY this is ok for fuzzyDR evaulation of delta_i only):" + _pObey);
    		break;
    	case 2:
    		// delta_e only.
    		_pObey = _de;
    		DEBUG: System.out.println("... ... running Scenario:" + Config.scenarioID + ", subScenario:" + Config.subScenarioID + " --- (VERIFY this is ok for fuzzyDR evaulation of delta_e only):" + _pObey);
    		break;
    	case 3:
    		// delta_o only.
    		_pObey = _do;
    		DEBUG: System.out.println("... ... running Scenario:" + Config.scenarioID + ", subScenario:" + Config.subScenarioID + " --- (VERIFY this is ok for fuzzyDR evaulation of delta_o only):" + _pObey);
    		break;
    	case 4:
    		// delta_tree, all delta parameters.
    		_pObey = _dtree;
    		DEBUG: System.out.println("... ... running Scenario:" + Config.scenarioID + ", subScenario:" + Config.subScenarioID + " --- (VERIFY this is ok for fuzzyDR evaulation of full delta_tree):" + _pObey);
    		break;
    	default:
    		// default to full evaluation.
    		_pObey = _dtree;
    		DEBUG: System.out.println("... ... running Scenario:" + Config.scenarioID + ", subScenario:" + Config.subScenarioID + " --- (in DEFAULT - full delta_tree):" + _pObey);
    		break;
    	}
    	return _pObey;
    }
	
	public double action(SimState state, double pObey, double resourceLevel) {
		FuzzyDRController fuzzyDR = (FuzzyDRController) state;
		
		double _remaining = 0;
		double _chance = this.localRNG.nextDouble();
		
		if (_chance < pObey) {
			// obey.
			
			// reset the consumption target to the institution.
			this.setConsumptionTarget(fuzzyDR.adico_1.getI_quantity());
			//DEBUG: System.out.println("... ... pObey() is: " + pObey);
			_remaining = harvest(resourceLevel, this.energy, this.consumptionTarget);		// maintain current action policy for consumption levels.
			DEBUG: System.out.println("... running fuzzyDR: AgentID:" + this.getAgentID() + ", p(obey): " + this.getAgreeemnt_institution() + ", chance: " + _chance + ", agent will 'OBEY' --- maintain consumption: " + this.getConsumptionTarget() + ".");
		} else {
			// break.
			
			innovateNewAction();		// determine a new consumption target and update the agent's consumption target field.
			_remaining = harvest(resourceLevel, this.energy, this.getConsumptionTarget());			// innovate new action policy for consumption levels.
			DEBUG: System.out.println("... running fuzzyDR: AgentID:" + this.getAgentID() + ", p(obey): " + this.getAgreeemnt_institution() + ", chance: " + _chance + ", agent will 'BREAK' --- innovated new action --- new consumption: " + this.getConsumptionTarget() + ".");
		}
		return _remaining;
	}
	
	/**
	 * In the case of agents breaking with institution, determines a new action policy and updates the agent's consumption target field.
	 */
	public void innovateNewAction() {
		// TODO:  In case the agent decides to break with institution... need logic here to develop new rules for them to follow. This may require passed arguments for better heuristics or decision logic.
		
		double _newConsumptionTarget = 0;
		//double _newConsumptionTarget = this.consumptionTarget;		// for now, this is a placeholder.
		// TODO: is the new action innovation specific to scenario? w/ case statements here again? e.g., if in dire straits, catch up and consume as much as possible.
		// < insert new logic here to develop new consumption target... nothing fancy, e.g., imitate, fill exact need, etc. >
		
		// THIS USE CASE IS SPECIFIC TO SCENARIO 2 --- for other scenarios, like Scenario 1, we might want to *reduce* consumption target. 
		double _needsGap = 100 - this.getEnergy();
		// linear function for consumption target based on the needs gap, where _newConsumptionTarget equals 10 when _needsGap is 100, and decreases to 2 when _needsGap is 0.
		_newConsumptionTarget = 0.08 * _needsGap + 2;
		// ensure that _newConsumptionTarget does not exceed 10 --- this allows for new targets to go up to X10 higher than ADICO (assuming ADICO = 1 consumption).
		_newConsumptionTarget = Math.min(_newConsumptionTarget, 10);
		
		//DEBUG: System.out.println("... ... ... given institution BREAK, AgentID:" + this.getAgentID() + " has a needs gap of " + _needsGap + ", and innovated a new action policy, updating the old consumption target of : " + this.consumptionTarget + " with new target of : " + _newConsumptionTarget + ".");
		
		this.setConsumptionTarget(_newConsumptionTarget);
		//return this.getConsumptionTarget();
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
	 * Take the current list of initially connected agents from the small world network, and add random agent from those remaining in the run. Ensures at least one neighbor in the set at all times until end of run.
	 * @param state
	 * @return
	 */
	public List<Agent> getExpandedNeighbors(SimState state) {
	    FuzzyDRController fuzzyDR = (FuzzyDRController) state;
	    List<Agent> expandedNeighbors = new ArrayList<>(this.neighbors); // Start with existing neighbors
	    
	    // Exclude the current agent from selection to prevent self-selection
	    List<Agent> allActiveAgents = new ArrayList<>(fuzzyDR.masterMap_ActiveAgents.values());
	    allActiveAgents.remove(this);

	    // Add one random agent from the remaining active agents, if any are available
	    if (!allActiveAgents.isEmpty()) {
	        Agent randomAgent = allActiveAgents.get(this.localRNG.nextInt(allActiveAgents.size()));
	        expandedNeighbors.add(randomAgent);
	    }
	    return expandedNeighbors;
	}
 
	/**
	 * Calculate average energy among expanded neighbors (initially linked neighbor list in small world network, plus random agent from those remaining in the simulation).
	 * @param expandedNeighbors
	 * @return
	 */
	public double calcAvgEnergyOfExtendedNeighbors(List<Agent> expandedNeighbors) {
		double _totalEnergy = 0;
		for (Agent neighbor : expandedNeighbors) {
			_totalEnergy += neighbor.getEnergy();
		}
		double _avg = _totalEnergy / expandedNeighbors.size();
		return _avg;
	}
	
	/**
	 * Calculate average consumption target among expanded neighbors (initially linked neighbor list in small world network, plus random agent from those remaining in the simulation).
	 * @param expandedNeighbors
	 * @return
	 */
	public double calcAvgConsumptionOfExtendedNeighbors(List<Agent> expandedNeighbors) {
	    double _totalConsumptionTarget = 0;
	    for (Agent neighbor : expandedNeighbors) {
	    	_totalConsumptionTarget += neighbor.getConsumptionTarget();
	    }
	    double _actionConsensus = _totalConsumptionTarget / expandedNeighbors.size();
	    return _actionConsensus;
	}
	
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
    	
    	LinguisticTerm low = diFIS_in_selfEnergy.getLinguisticTerm("low");
        
    	
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

	/**
	 * Generate a log entry for the logger, following format for "Run, Step, AgentID, Energy-Level, Common-Pool-Levels, agent0_delta_i, agent0_delta_e, agent0_delta_o, agent0_agreement, pop_avg_agreement" 
	 * @param state
	 * @return
	 */
	public String generateLogEntry(SimState state) {
		FuzzyDRController fuzzyDR = (FuzzyDRController) state;
		
		double e;
		if (this.energy < 0) {
			e = 0;
		} else {
			e = this.energy;
		}
		
		//return fuzzyDR.schedule.getSteps() + "," + agentID + "," + e + "," + agreement;
        //return fuzzyDR.schedule.getSteps() + "," + getAgentID() + "," + e + "," + getAgreement();
		return Config.batchRunID + "," + fuzzyDR.schedule.getSteps() + "," + agentID + "," + e + "," + fuzzyDR.commons.getResourceLevel() +
				"," + this.getAgreement_delta_i() + "," + this.getAgreement_delta_e() + "," + this.getAgreement_delta_o() + "," + this.getAgreeemnt_institution();
    }
	
	
	/**
	 * Generate a normally distributed value between 0.8 and 1.0 for the desired result of highly clustered population that largely agrees with current institutional context.
	 * @param mean
	 * @param stdDev
	 * @return
	 */
	private double generateGaussian(double mean, double stdDev, double lower, double upper) {
        // Generate a normally distributed value with mean 0 and standard deviation 1
        double gaussianValue = localRNG.nextGaussian() * stdDev + mean;

        // Clamp the value to be within [0.8, 1.0] 
        gaussianValue = Math.min(Math.max(gaussianValue, lower), upper);
        
        return gaussianValue;
    }
	
	public void setStoppable(Stoppable s) {
		this.stopper = s;
	}
	
	public int getAgentID() { 
		return this.agentID; 
	}

	public boolean isFuzzyDRActivated() {
		return isFuzzyDRActivated;
	}

	public void setFuzzyDRActivated(boolean isFuzzyDRActivated) {
		this.isFuzzyDRActivated = isFuzzyDRActivated;
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

	public double getAgreeemnt_institution() {
		return agreeemnt_institution;
	}

	public void setAgreeemnt_institution(double agreeemnt_institution) {
		this.agreeemnt_institution = agreeemnt_institution;
	}

	public double getAgreement_delta_i() {
		return agreement_delta_i;
	}

	public void setAgreement_delta_i(double agreement_delta_i) {
		this.agreement_delta_i = agreement_delta_i;
	}

	public double getAgreement_delta_e() {
		return agreement_delta_e;
	}

	public void setAgreement_delta_e(double agreement_delta_e) {
		this.agreement_delta_e = agreement_delta_e;
	}

	public double getAgreement_delta_o() {
		return agreement_delta_o;
	}

	public void setAgreement_delta_o(double agreement_delta_o) {
		this.agreement_delta_o = agreement_delta_o;
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
