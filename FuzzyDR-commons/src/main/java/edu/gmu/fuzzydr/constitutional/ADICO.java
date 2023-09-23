package edu.gmu.fuzzydr.constitutional;

public class ADICO {

	public int adico_ID;
	
	public String A_field;
	
	public String D_field;  // what deontic fields are necessary here to enable fuzzyDR? 
	
	public String I_verb;   // maybe better as an Enum? and Switch statements based on Enum.
	public String I_bound;
	public int I_quantity;
	public String I_object;
	
	public String C_object;
	public String C_bound;
	public int C_quantity;
	public String C_time;
	
	public int O_quantity;
	
	/**
	 * Default constructor for an ADICO institutional statement.
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
			int i_quantity,
			String i_object,
			
			String c_object,
			String c_bound,
			int c_quantity,
			String c_time,
			
			int o_quantity) {
		
		this.adico_ID = adico_ID;
		
		this.A_field = a_field;
		
		this.D_field = d_field;
		
		this.I_verb = i_verb;
		this.I_bound = i_bound;
		this.I_quantity = i_quantity;
		this.I_object = i_object;
		
		this.C_object = c_object;
		this.C_bound = c_bound;
		this.C_quantity = c_quantity;
		this.C_time = c_time;
		
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

	public int getI_quantity() {
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

	public int getC_quantity() {
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

	public int getO_quantity() {
		return O_quantity;
	}

	public void setO_quantity(int o_quantity) {
		O_quantity = o_quantity;
	}
	
	
	
}
