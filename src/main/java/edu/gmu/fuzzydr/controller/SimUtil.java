package edu.gmu.fuzzydr.controller;

public class SimUtil {

	
	public static int generateUID() {
		
		// take the global UID and return this value.
		int _nextUID = FuzzyDRController.UID; 
		
		// increment the global for the next agent.
		FuzzyDRController.UID += 1;
		
		return _nextUID;
	}
	
	
}
