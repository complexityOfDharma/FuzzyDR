package edu.gmu.fuzzydr.constitutional;

public class ADICO {

	public int adico_ID;
	
	public String A_field;
	
	public String D_field;  // what deontic fields are necessary here to enable fuzzyDR? 
	
	public String I_verb;   // maybe better as an Enum? and Switch statements based on Enum.
	public String I_bound;
	public double I_quantity;
	public String I_object;
	
	public String C_object;
	public String C_bound;
	public double C_quantity;
	public String C_time;
	
	public double O_quantity;
	
	/**
	 * Default constructor for an ADICO institutional statement, w.r.t. aIm(I) and Conditions(C)
	 * @param a_field
	 * @param d_field
	 * @param i_verb
	 * @param i_bound
	 * @param i_quantity
	 * @param i_object
	 * @param c_object
	 * @param c_bound
	 * @param c_quantity
	 * @param c_time
	 * @param o_quantity
	 */
	public ADICO(
			int adico_ID,
			
			String a_field,
			
			String d_field,
			
			String i_verb,
			String i_bound,
			double i_quantity,
			String i_object,
			
			String c_object,
			String c_bound,
			double c_quantity,
			String c_time,
			
			double o_quantity) {
		
		this.adico_ID = adico_ID;
		
		this.A_field = a_field;					// Attributes: who.
		
		this.D_field = d_field;					// Deontic: forbidden, permitted, obligated.
		
		this.I_verb = i_verb;					// aIm: e.g., demand, harvest, consume.
		this.I_bound = i_bound;					// aIm: e.g., less than, more than.
		this.I_quantity = i_quantity;			// aIm: amount.
		this.I_object = i_object;				// aIm: e.g., water, resources.
		
		this.C_object = c_object;				// Conditions: e.g., source, reservoir, common pool.
		this.C_bound = c_bound;					// Conditions: e.g., lower than.
		this.C_quantity = c_quantity;			// Conditions: amount.
		this.C_time = c_time;					// Conditions: e.g., at any time, seasonal.
		
		this.O_quantity = o_quantity;			// sanction field (+ risk, etc) for the FIS.
		
	}
	
	// TODO: consider a method to ouput the ADICO into a readable narrative statement to the console and report logs.
	/*
	public String outputNarrative_ADICO() {
		System.out.println("ADICO: " + adico_ID);
		System.out.println("Agents " + A_field + ", " + D_field + " " + I_verb + " " + ...)
	}
	*/

	public int getA_ID() {
		return adico_ID;
	}
	
	public String getA_field() {
		return A_field;
	}

	public void setA_field(String a_field) {
		A_field = a_field;
	}

	public String getD_field() {
		return D_field;
	}

	public void setD_field(String d_field) {
		D_field = d_field;
	}

	public String getI_verb() {
		return I_verb;
	}

	public void setI_verb(String i_verb) {
		I_verb = i_verb;
	}

	public String getI_bound() {
		return I_bound;
	}

	public void setI_bound(String i_bound) {
		I_bound = i_bound;
	}

	public double getI_quantity() {
		return I_quantity;
	}

	public void setI_quantity(int i_quantity) {
		I_quantity = i_quantity;
	}

	public String getI_object() {
		return I_object;
	}

	public void setI_object(String i_object) {
		I_object = i_object;
	}

	public String getC_object() {
		return C_object;
	}

	public void setC_object(String c_object) {
		C_object = c_object;
	}

	public String getC_bound() {
		return C_bound;
	}

	public void setC_bound(String c_bound) {
		C_bound = c_bound;
	}

	public double getC_quantity() {
		return C_quantity;
	}

	public void setC_quantity(int c_quantity) {
		C_quantity = c_quantity;
	}

	public String getC_time() {
		return C_time;
	}

	public void setC_time(String c_time) {
		C_time = c_time;
	}

	public double getO_quantity() {
		return O_quantity;
	}

	public void setO_quantity(int o_quantity) {
		O_quantity = o_quantity;
	}
	
	
	
}
