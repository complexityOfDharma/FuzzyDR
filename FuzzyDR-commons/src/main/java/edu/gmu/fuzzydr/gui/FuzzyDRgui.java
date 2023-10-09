package edu.gmu.fuzzydr.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import java.io.IOException;

import edu.gmu.fuzzydr.collectives.Agent;
import edu.gmu.fuzzydr.controller.Config;
import edu.gmu.fuzzydr.controller.FuzzyDRController;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.display.Manipulating2D;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.SimpleInspector;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.inspector.TabbedInspector;
import sim.portrayal.network.NetworkPortrayal2D;
import sim.portrayal.network.SimpleEdgePortrayal2D;
import sim.portrayal.network.SpatialNetwork2D;
import sim.portrayal.simple.CircledPortrayal2D;
import sim.portrayal.simple.LabelledPortrayal2D;
import sim.portrayal.simple.MovablePortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.gui.ColorMap;
import sim.util.gui.SimpleColorMap;
import sim.util.gui.Utilities;
import sim.util.media.chart.HistogramGenerator;
import sim.util.media.chart.TimeSeriesChartGenerator;
import sim.portrayal.simple.RectanglePortrayal2D;


public class FuzzyDRgui extends GUIState {

	public FuzzyDRController fuzzyDRController;
	public Display2D display;
	public JFrame displayFrame;
	
	private ContinuousPortrayal2D agentPortrayal = new ContinuousPortrayal2D();
    private NetworkPortrayal2D networkPortrayal = new NetworkPortrayal2D();
    
    public TimeSeriesChartGenerator resourcePoolChart;
    public HistogramGenerator agreementHistogram;
    
    // called by GUI main.
    public FuzzyDRgui() throws IOException {
    	super(new FuzzyDRController(Config.RANDOM_SEED));
		fuzzyDRController = (FuzzyDRController) state;
		//setupDisplayAndAttachPortrayals();
    }
    
    public FuzzyDRgui(SimState state) {
		super(state);
		fuzzyDRController = (FuzzyDRController) state;
		//setupDisplayAndAttachPortrayals();
	}
    
    public static String getName() {
    	return "FuzzyDR-commons";
    }
    
	/**
	 * Setup tasks run once at the beginning of the simulation.
	 */
    public void init(final Controller c) {
		super.init(c);
		
		display = new Display2D(Config.WIDTH, Config.HEIGHT, this);
		
		display.setClipping(false);   // false --> render network links if they extend past display window bounds
		displayFrame = display.createFrame();
		displayFrame.setTitle("FuzzyDR-commons");
		controller.registerFrame(displayFrame);
		displayFrame.setVisible(true);
		
		display.attach(networkPortrayal, "Network");
		display.attach(agentPortrayal, "Agents");
		
		// plots.
		resourcePoolChart = new TimeSeriesChartGenerator();
		resourcePoolChart.setTitle("Resource Pool Level");
		resourcePoolChart.setYAxisLabel("Remaining Resources");
		resourcePoolChart.setXAxisLabel("Step");
		resourcePoolChart.setVisible(true);
		
		JFrame pool_frame = resourcePoolChart.createFrame();
		c.registerFrame(pool_frame);
		
		// agreement histogram.
		agreementHistogram = new HistogramGenerator();
		agreementHistogram.setTitle("Agent Agreement Levels");
		agreementHistogram.setXAxisLabel("Agreement");
		agreementHistogram.setYAxisLabel("Frequency");
		agreementHistogram.setVisible(true);
		
		JFrame agreement_frame = agreementHistogram.createFrame();
		c.registerFrame(agreement_frame);
		
	}
    
    /**
     * Lifecycle method to start the simulation loop.
     */
    public void start() {
		super.start();
		setupPortrayals(fuzzyDRController);
		
		System.out.println("\nStarting simulation visualization...\n");
		
		// reset resource pool chart.
		resourcePoolChart.removeAllSeries();
		resourcePoolChart.addSeries(((FuzzyDRController)state).resourcePoolLevels, null);
		
		// Schedule the histogram update to be executed in the GUI thread
	    scheduleRepeatingImmediatelyAfter(new Steppable() {
	        public void step(SimState state) {
	            updateAgreementHistogram(fuzzyDRController);
	        }
	    });
		
		
		//TODO: if XYSeries objects for plots, do clear them here.
	}
	
	public void load(SimState state) {
		super.load(state);
		setupPortrayals(state);
	}
	
    private void setupPortrayals(SimState state) {
    	
    	FuzzyDRController fuzzyDR = (FuzzyDRController) state;
    	
    	// agents portrayal.
    	agentPortrayal.setField(fuzzyDR.world);
    	//agentPortrayal.setPortrayalForAll(new OvalPortrayal2D(Color.BLUE, 10));
    	agentPortrayal.setPortrayalForAll(
    			new OvalPortrayal2D(10) {  // with default scale 
					private static final long serialVersionUID = 1L;
					
					@Override
					public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
						Agent agent = (Agent) object;
						
						if (agent.getAgreement() > 0.3) {
							// toward agree with institution.
							paint = Color.BLUE;
			                scale = 10;
							
						} else {
							// toward disagree with institution.
							paint = Color.MAGENTA;
			                scale = 10;
							
						}
						
						super.draw(object, graphics, info);
					}
				}
    	);
    	
        // network portrayal.
    	networkPortrayal.setField(new SpatialNetwork2D(fuzzyDR.world, fuzzyDR.network));
        networkPortrayal.setPortrayalForAll(new SimpleEdgePortrayal2D());

		display.reset();
	    display.setBackdrop(Color.WHITE);
	    display.repaint();
    }
    
    
    public void quit() {
    	super.quit();
    	if (displayFrame != null) {
    		displayFrame.dispose();
    	}
    	displayFrame = null;
    	display = null;
    }
    
    
    public void updateAgreementHistogram(SimState state) {
    	
    	FuzzyDRController fuzzyDR = (FuzzyDRController) state;
    	
        // Ensure you’re running on the GUI thread if this method alters GUI components
        if(SwingUtilities.isEventDispatchThread()) {
            // Gather agreement levels
            double[] agreementLevels = new double[fuzzyDR.masterList_Agents.size()];
            for(int i = 0; i < fuzzyDR.masterList_Agents.size(); i++) {
                agreementLevels[i] = fuzzyDR.masterList_Agents.get(i).getAgreement();
            }
            
            // clear out histogram before updating.
            agreementHistogram.removeAllSeries();
            // Add data to histogram.
            agreementHistogram.addSeries(agreementLevels, 10, "Agreement Levels", null);
        } else {
            SwingUtilities.invokeLater(() -> updateAgreementHistogram(state));
        }
    }

    
    public static void main(String[] args) throws IOException {
    	FuzzyDRgui simViz = new FuzzyDRgui();
    	Console c = new Console(simViz);
    	c.setVisible(true);
    }

    /*
    public void setupDisplayAndAttachPortrayals() {
    	display = new Display2D(Config.WIDTH, Config.HEIGHT, this);
		
		display.setClipping(false);   // false --> render network links if they extend past display window bounds
		displayFrame = display.createFrame();
		displayFrame.setTitle("FuzzyDR-commons");
		controller.registerFrame(displayFrame);
		displayFrame.setVisible(true);
		
		display.attach(networkPortrayal, "Network");
    }
    */
    
}
