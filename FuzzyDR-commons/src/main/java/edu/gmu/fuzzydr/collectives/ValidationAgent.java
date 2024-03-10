package edu.gmu.fuzzydr.collectives;

import edu.gmu.fuzzydr.controller.Config;
import edu.gmu.fuzzydr.controller.FCLCodeGenerator;
import edu.gmu.fuzzydr.controller.SimUtil;
import edu.gmu.fuzzydr.controller.ValidationController;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.plot.JFuzzyChart;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import sim.engine.SimState;
import sim.engine.Steppable;

public class ValidationAgent implements Steppable{

	private static final long serialVersionUID = 1L;
	
	// Agent attributes.
	private int agentID;
	private int archetype;
	private double energy;
		
	private double consumptionTarget;	// agent's own self-policy for consumption (either aligned to institution, or own-strategy).
		
	private double agreeement_institution;		// overall p(obey) for given action policy.
	private double agreement_delta_i;			// agreement index [0, 1] for delta parameter: internal.
	private double agreement_delta_e;			// agreement index [0, 1] for delta parameter: external.
	private double agreement_delta_o;			// agreement index [0, 1] for delta parameter: or-else.
	
	private double delta_i_in;			// agreement index [0, 1] for delta parameter: internal.
	private double delta_e_in;			// agreement index [0, 1] for delta parameter: external.
	private double delta_o_in;			// agreement index [0, 1] for delta parameter: or-else.
	
	private FIS fis_delta_i;
	private FunctionBlock fb_delta_i;
	private Variable diFIS_in, diFIS_out;
	
	private FIS fis_delta_e;
	private FunctionBlock fb_delta_e;
	private Variable deFIS_in, deFIS_out;
	
	private FIS fis_delta_o;
	private FunctionBlock fb_delta_o;
	private Variable doFIS_in, doFIS_out;
	
	private FIS fis_delta_tree;
	private FunctionBlock fb_delta_tree;
	private Variable dtreeFIS_in_delta_i, dtreeFIS_in_delta_e, dtreeFIS_in_delta_o, dtreeFIS_out;
	
	// Default constructor.
	public ValidationAgent() {
		// generate a random UID.
		this.agentID = SimUtil.generateUID();
		this.archetype = this.agentID;
		
		this.energy = 100;
		this.consumptionTarget = 100;
		
		String _fclString_di = Config.delta_i_vv_FCLPath;
	 	FCLCodeGenerator _codeGenerator = new FCLCodeGenerator(_fclString_di);
	 	fis_delta_i = _codeGenerator.loadFCL();
	 					
	 	fb_delta_i = fis_delta_i.getFunctionBlock("delta_i_vv");
	 	diFIS_in = fb_delta_i.getVariable("delta_i_in");
	 	diFIS_out = fb_delta_i.getVariable("delta_i_out");
	    
	 	// --- load FCL for FIS delta parameter external ---
	 	String _fclString_de = Config.delta_e_vv_FCLPath;
	 	_codeGenerator = new FCLCodeGenerator(_fclString_de);		// overwrite local var for loading new FCL on delta parameter external.
	 	fis_delta_e = _codeGenerator.loadFCL();
	 	
	    fb_delta_e = fis_delta_e.getFunctionBlock("delta_e_vv");
	 	deFIS_in = fb_delta_e.getVariable("delta_e_in");
	 	deFIS_out = fb_delta_e.getVariable("delta_e_out");
	    	     	
	 	// --- load FCL for FIS delta parameter or-Else ---
	 	String _fclString_do = Config.delta_o_vv_FCLPath;
	 	_codeGenerator = new FCLCodeGenerator(_fclString_do);		// overwrite local var for loading new FCL on delta parameter external.
	 	fis_delta_o = _codeGenerator.loadFCL();
	 	
	    fb_delta_o = fis_delta_o.getFunctionBlock("delta_o_vv");
	 	doFIS_in = fb_delta_o.getVariable("delta_o_in");
	 	doFIS_out = fb_delta_o.getVariable("delta_o_out");
	    
	 	// --- load FCL for FIS tree to determine overall p(obey) ---
	 	String _fclString_dtree = Config.delta_tree_vv_FCLPath;
	 	_codeGenerator = new FCLCodeGenerator(_fclString_dtree);		// overwrite local var for loading new FCL on delta parameter external.
	 	fis_delta_tree = _codeGenerator.loadFCL();
	 	
	    fb_delta_tree = fis_delta_tree.getFunctionBlock("delta_tree_vv");
	 	dtreeFIS_in_delta_i = fb_delta_tree.getVariable("delta_i_out");
	 	dtreeFIS_in_delta_e = fb_delta_tree.getVariable("delta_e_out");
	 	dtreeFIS_in_delta_o = fb_delta_tree.getVariable("delta_o_out");
	 	dtreeFIS_out = fb_delta_tree.getVariable("delta_tree_out");
	
	}
	
	
	@Override
	public void step(SimState state) {
		ValidationController vc = (ValidationController) state;
		
		double _targetOld = this.consumptionTarget;
		this.setConsumptionTarget(500);
		System.out.println("... update target from ADICO, agentID=" + this.agentID + "     prior consumptionTarget:" + _targetOld + "   --->  new consumptionTarget:" + this.consumptionTarget);
		
		// for Scenario 1:
		
			// hard code the inputs
			this.setDelta_i_in(0.165);
			this.setDelta_e_in(0.165);
			this.setDelta_o_in(0.165);
		
			// run FIS evaluation
			// delta_internal
			diFIS_in.setValue(this.getDelta_i_in());
	    	fb_delta_i.evaluate();
	    	
	    	diFIS_out = fb_delta_i.getVariable("delta_i_out");
	    	this.setAgreement_delta_i(diFIS_out.getValue());
	    	
	    	// delta_external
	    	deFIS_in.setValue(this.getDelta_e_in());
	    	fb_delta_e.evaluate();
	    	
	    	deFIS_out = fb_delta_e.getVariable("delta_e_out");
	    	this.setAgreement_delta_e(deFIS_out.getValue());
	    	
	    	// delta_orElse
	    	doFIS_in.setValue(this.getDelta_o_in());
	    	fb_delta_o.evaluate();
	    	
	    	doFIS_out = fb_delta_o.getVariable("delta_o_out");
	    	this.setAgreement_delta_o(doFIS_out.getValue());
	    	
	    	// delta_tree
	    	dtreeFIS_in_delta_i.setValue(this.getAgreement_delta_i());
	    	dtreeFIS_in_delta_e.setValue(this.getAgreement_delta_e());
	    	dtreeFIS_in_delta_o.setValue(this.getAgreement_delta_o());
	    	fb_delta_tree.evaluate();
	    	
	    	dtreeFIS_out = fb_delta_tree.getVariable("delta_tree_out");
	    	this.setAgreeemnt_institution(dtreeFIS_out.getValue());
	    	
	    	// output to console the results --> 0.125 as final agreement
			if (this.getAgentID() == 0) {
		    	DEBUG: System.out.println("\nLevel_0 Validation --- Scenario 1:");
		    	DEBUG: System.out.println("\n... For AgentID=0:");
		    	DEBUG: System.out.println("... >>> delta_i evaluation    --- input: " + this.getDelta_i_in() + "   --->   out: " + this.getAgreement_delta_i() + ".");
		    	DEBUG: System.out.println("... >>> delta_e evaluation    --- input: " + this.getDelta_e_in() + "   --->   out: " + this.getAgreement_delta_e() + ".");
		    	DEBUG: System.out.println("... >>> delta_o evaluation    --- input: " + this.getDelta_o_in() + "   --->   out: " + this.getAgreement_delta_o() + ".");
		    	DEBUG: System.out.println("... >>> delta_tree evaluation --- input_di: " + this.getAgreement_delta_i() + ", input_de:" + this.getAgreement_delta_e() + ", input_do:" + this.getAgreement_delta_o() + "   --->   out: P(Obey)=" + this.getAgreement_delta_i() + ".\n\n");
		    }
	    	
	    		    	
		// for Scenario 2:
		
			// hard code the inputs
			this.setDelta_i_in(0.835);
			this.setDelta_e_in(0.835);
			this.setDelta_o_in(0.835);
					
			// run FIS evaluation
			// delta_internal
			diFIS_in.setValue(this.getDelta_i_in());
			fb_delta_i.evaluate();
				    	
			diFIS_out = fb_delta_i.getVariable("delta_i_out");
			this.setAgreement_delta_i(diFIS_out.getValue());
				    	
			// delta_external
			deFIS_in.setValue(this.getDelta_e_in());
			fb_delta_e.evaluate();
				    	
			deFIS_out = fb_delta_e.getVariable("delta_e_out");
			this.setAgreement_delta_e(deFIS_out.getValue());
				    	
			// delta_orElse
			doFIS_in.setValue(this.getDelta_o_in());
			fb_delta_o.evaluate();
				    	
			doFIS_out = fb_delta_o.getVariable("delta_o_out");
			this.setAgreement_delta_o(doFIS_out.getValue());
				    	
			// delta_tree
			dtreeFIS_in_delta_i.setValue(this.getAgreement_delta_i());
			dtreeFIS_in_delta_e.setValue(this.getAgreement_delta_e());
			dtreeFIS_in_delta_o.setValue(this.getAgreement_delta_o());
			fb_delta_tree.evaluate();
			    	
			dtreeFIS_out = fb_delta_tree.getVariable("delta_tree_out");
			this.setAgreeemnt_institution(dtreeFIS_out.getValue());
			   	
			// output to console the results --> 0.835 as final agreement
			if (this.getAgentID() == 1) {
			   	DEBUG: System.out.println("\nLevel_0 Validation --- Scenario 2:");
			   	DEBUG: System.out.println("\n... For AgentID=1:");
			   	DEBUG: System.out.println("... >>> delta_i evaluation    --- input: " + this.getDelta_i_in() + "   --->   out: " + this.getAgreement_delta_i() + ".");
			   	DEBUG: System.out.println("... >>> delta_e evaluation    --- input: " + this.getDelta_e_in() + "   --->   out: " + this.getAgreement_delta_e() + ".");
			   	DEBUG: System.out.println("... >>> delta_o evaluation    --- input: " + this.getDelta_o_in() + "   --->   out: " + this.getAgreement_delta_o() + ".");
			   	DEBUG: System.out.println("... >>> delta_tree evaluation --- input_di: " + this.getAgreement_delta_i() + ", input_de:" + this.getAgreement_delta_e() + ", input_do:" + this.getAgreement_delta_o() + "   --->   out: P(Obey)=" + this.getAgreement_delta_i() + ".\n\n");
			}
		
		// for Scenario 3:
			
			// hard code the inputs
			this.setDelta_i_in(0.165);
			this.setDelta_e_in(0.5);
			this.setDelta_o_in(0.835);
					
			// run FIS evaluation
			// delta_internal
			diFIS_in.setValue(this.getDelta_i_in());
			fb_delta_i.evaluate();
				    	
			diFIS_out = fb_delta_i.getVariable("delta_i_out");
			this.setAgreement_delta_i(diFIS_out.getValue());
				    	
			// delta_external
			deFIS_in.setValue(this.getDelta_e_in());
			fb_delta_e.evaluate();
				    	
			deFIS_out = fb_delta_e.getVariable("delta_e_out");
			this.setAgreement_delta_e(deFIS_out.getValue());
				    	
			// delta_orElse
			doFIS_in.setValue(this.getDelta_o_in());
			fb_delta_o.evaluate();
				    	
			doFIS_out = fb_delta_o.getVariable("delta_o_out");
			this.setAgreement_delta_o(doFIS_out.getValue());
				    	
			// delta_tree
			dtreeFIS_in_delta_i.setValue(this.getAgreement_delta_i());
			dtreeFIS_in_delta_e.setValue(this.getAgreement_delta_e());
			dtreeFIS_in_delta_o.setValue(this.getAgreement_delta_o());
			fb_delta_tree.evaluate();
			    	
			dtreeFIS_out = fb_delta_tree.getVariable("delta_tree_out");
			this.setAgreeemnt_institution(dtreeFIS_out.getValue());
			   	
			// output to console the results --> 0.500 as final agreement
			if (this.getAgentID() == 2) {
			   	DEBUG: System.out.println("\nLevel_0 Validation --- Scenario 3:");
			   	DEBUG: System.out.println("\n... For AgentID=2:");
			   	DEBUG: System.out.println("... >>> delta_i evaluation    --- input: " + this.getDelta_i_in() + "   --->   out: " + this.getAgreement_delta_i() + ".");
			   	DEBUG: System.out.println("... >>> delta_e evaluation    --- input: " + this.getDelta_e_in() + "   --->   out: " + this.getAgreement_delta_e() + ".");
			   	DEBUG: System.out.println("... >>> delta_o evaluation    --- input: " + this.getDelta_o_in() + "   --->   out: " + this.getAgreement_delta_o() + ".");
			   	DEBUG: System.out.println("... >>> delta_tree evaluation --- input_di: " + this.getAgreement_delta_i() + ", input_de:" + this.getAgreement_delta_e() + ", input_do:" + this.getAgreement_delta_o() + "   --->   out: P(Obey)=" + this.getAgreeemnt_institution() + ".\n\n");
			}
			
		// for Scenario 4:
			
			double _adjustedAgreement;
			
			if (this.getAgentID() == 99) {
				
				_adjustedAgreement = (this.getAgreeemnt_institution() / 2);
				DEBUG: System.out.println("\nLevel_0 Validation --- Scenario 4:");
				DEBUG: System.out.println("\n... For AgentID=99:");
				DEBUG: System.out.println("... >>> computed institutional agreeement    --- original P(Obey): " + this.getAgreeemnt_institution() + "   --->   adjusted for archetype: " + _adjustedAgreement + ".\n\n");
			}

	}


	public int getAgentID() {
		return agentID;
	}


	public void setAgentID(int agentID) {
		this.agentID = agentID;
	}


	public int getArchetype() {
		return archetype;
	}


	public void setArchetype(int archetype) {
		this.archetype = archetype;
	}


	public double getEnergy() {
		return energy;
	}


	public void setEnergy(double energy) {
		this.energy = energy;
	}


	public double getConsumptionTarget() {
		return consumptionTarget;
	}


	public void setConsumptionTarget(double consumptionTarget) {
		this.consumptionTarget = consumptionTarget;
	}


	public double getAgreeemnt_institution() {
		return agreeement_institution;
	}


	public void setAgreeemnt_institution(double agreeemnt_institution) {
		this.agreeement_institution = agreeemnt_institution;
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


	public double getDelta_o_in() {
		return delta_o_in;
	}


	public void setDelta_o_in(double delta_o_in) {
		this.delta_o_in = delta_o_in;
	}


	public double getDelta_i_in() {
		return delta_i_in;
	}


	public void setDelta_i_in(double delta_i_in) {
		this.delta_i_in = delta_i_in;
	}


	public double getDelta_e_in() {
		return delta_e_in;
	}


	public void setDelta_e_in(double delta_e_in) {
		this.delta_e_in = delta_e_in;
	}

}
