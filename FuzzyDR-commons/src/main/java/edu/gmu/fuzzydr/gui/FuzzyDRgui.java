package edu.gmu.fuzzydr.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import javax.swing.JFrame;
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
import sim.util.media.chart.TimeSeriesChartGenerator;
import sim.portrayal.simple.RectanglePortrayal2D;


public class FuzzyDRgui extends GUIState {

	public FuzzyDRController fuzzyDRController;
	public Display2D display;
	public JFrame displayFrame;
	
	private ContinuousPortrayal2D agentPortrayal = new ContinuousPortrayal2D();
    private NetworkPortrayal2D networkPortrayal = new NetworkPortrayal2D();
    
    public TimeSeriesChartGenerator resourcePoolChart;
    
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
		
		JFrame frame = resourcePoolChart.createFrame();
		c.registerFrame(frame);
		
	}
    
    /**
     * Lifecycle method to start the simulation loop.
     */
    public void start() {
		super.start();
		setupPortrayals();
		
		System.out.println("\nStarting simulation visualization...\n");
		
		resourcePoolChart.removeAllSeries();
		resourcePoolChart.addSeries(((FuzzyDRController)state).resourcePoolLevels, null);
		
		//TODO: if XYSeries objects for plots, do clear them here.
	}
	
	public void load(SimState state) {
		super.load(state);
		setupPortrayals();
	}
	
    private void setupPortrayals() {
    	
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
						
						Color currentColor = graphics.getColor();
						
						//System.out.println("drawing... agent " + agent.getAgentID() + " has an agreement level of: " + agent.getAgreement());
						
						// TODO: this dynamic color change is not working for some reason...
						if (agent.getAgreement() >= 0.5) {
							graphics.setColor(Color.GREEN);
						} else {
							graphics.setColor(Color.RED);
						}
						
						// Explicitly draw the oval
		                int x = (int)info.draw.x;
		                int y = (int)info.draw.y;
		                int w = (int)info.draw.width;
		                int h = (int)info.draw.height;
		                graphics.fillOval(x, y, w, h);

		                // Reset color back to original
		                graphics.setColor(currentColor);
						
						super.draw(object, graphics, info);
					}
				}
    	);
    	
    	
        // network portrayal.
    	networkPortrayal.setField(new SpatialNetwork2D(fuzzyDR.world, fuzzyDR.network));
        networkPortrayal.setPortrayalForAll(new SimpleEdgePortrayal2D());


        /*
    	SimpleEdgePortrayal2D edgePortrayal = new SimpleEdgePortrayal2D();
        edgePortrayal.setShape(new Line2D.Double(-1,-1,1,1));
        edgePortrayal.setPaint(Color.GRAY);
        networkPortrayal.setPortrayalForAll(edgePortrayal);

        // set up the portrayal for the agents
        OvalPortrayal2D agentPortrayal = new OvalPortrayal2D();
        agentPortrayal.setFilled(true);
        agentPortrayal.setPaint(Color.BLUE);
        networkPortrayal.setPortrayalForAll(agentPortrayal);
        */
    	
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
