

/**
An assortment of constants influencing the global appearance and functionality
of the applet. In many cases, the applet can be adapted to 
special needs (e.g. a different applet size, different time steps, different truck percentage etc) by simply changing some numbers here and recompiling.

*/


// if changes here, compile everything anow (rm *.class before compiling)

public interface Constants{ // MicroApplet2_0

    // #################################################################
    // MicroApplet2_0: Most important overall properties
    // #################################################################

    static final boolean GERMAN       = false;
    static final int CHOICE_SZEN_INIT = 2;		 // 1=ring,2=onramp,3=lane closing - initial start on the ON RAMP scenario
    static final boolean DOUBLEBUF    = true;    // whether double-buffering used
    static final int FRAMES_PER_TIMESTEP = 1;    // simulation time step
    static final boolean STRAIGHT_ROAD = false;
    static final boolean CLOCKWISE    = true;    // car movement in ring geometry (IN RING SCENARIO ONLY)
    static final double TIMESTEP_S    = 0.25;    // simulation time step
    static final double FRAC_TRUCK_INIT = 0.2;   // not for circle
    static final double FRAC_TRUCK_INIT_CIRCLE = .1;  // Trucks Percentage

    static final int     CONTROL_SIZE     =1;     // {relScreen, relBrowser, fix}
    static final double  REL_APPL_SIZE    =0.7;   // size / screen size
    static final int     APPL_WIDTH       = 700;   
    static final int     APPL_HEIGHT      = 550;   


    // #################################################################
    // Lane-change parameters 
    // Safety:    BSAVE,SMIN
    // Incentive: BIAS*, P_FACTOR, DB
    // Law:       EUR_RULES,VC_MS
    // #################################################################

    static final boolean EUR_RULES      = false;
    static final int     VC_MS = 16;    // crit. velocity where Europ rules 
                                     // kick in (in m/s)

    static final double MAIN_BSAVE = 12.;  
    static final double MAIN_SMIN = 2.; 

    static final double RMP_BSAVE     = 20.; // high to force merging
    static final double RMP_SMIN      = 2.;  // min. safe distance
    static final double RMP_BIAS_LEFT= 8.;  // high to force merging (8)

    static final double LANECL_BSAVE      = 12.; // high to force merging
    static final double LANECL_BIAS_RIGHT = 0.7; // negative to force merging
                                                 // to the left
 
    // Dont Touch for lane change (not just on ramp)
    static final double BIAS_RIGHT_CAR = 0.1; // right-lane bias  
    static final double BIAS_RIGHT_TRUCK = 0.3; 
    static final double P_FACTOR_CAR = 0.2;   // politeness factor
    static final double P_FACTOR_TRUCK = 0.2; 
    static final double DB_CAR = 0.3;   // changing thresholds (m/s^2)
    static final double DB_TRUCK = 0.2; 
    static final double DB_CAR3 = 0.3;  
    static final double DB_TRUCK3 = 0.2; //?!! aus irgendeinen Grund
    // etwa grob Faktor zehn bei ANgabe der db noetig ?!!??!!



    // #################################################################
    // interactive control variables (initial values in MicroSim*.java)
    // #################################################################

    static final int DENS_MIN_INVKM = 4 ;     // (veh/km/ (2 lanes))
    static final int DENS_MAX_INVKM = 80;     // try increasing this maybe?
    static final int DENS_INIT_INVKM = 42;    //

    // this controls the speed of the cars/trucks

    static final int SPEED_MAX = 20;    // maximum simulation speed possible
    static final int SPEED_MIN = 400;   // minimum simulation speed possible
    static final int SPEED_INIT = 20;   // initial simulation speed
    //static final double LNSPEED_MAX = Math.log(1./20);
    //static final double LNSPEED_MIN = Math.log(1./500); 


    static final int V0_MAX_KMH = 140;    // (km/h) (140: free)
    static final int V0_MIN_KMH = 20; 
    static final int V0_INIT_KMH = 80; 
    static final int VMAX_TRUCK_KMH = 80;

    

    
	// Vehicles Inflow Parameters
	static final int Q_INIT2    = 4000;   //initial setup of the arrival rate for Scenario 2
    static final int QRMP_INIT2 = 500;    //initial setup of the arrival rate for Scenario 2 - on ramp
    static final int Q_INIT3    = 1600;
    static final int Q_INIT4    = 2800;
    static final int Q_INIT5    = 1800;
	static final int Q_MAX      = 20000;       // Maximum attainable arrival rate
	static final int QRMP_MAX   = 1800;       // Maximum arrival rate on ramp

    // Politeness Factor Parameters
	static final int P_MAX = 2;       // Politeness factor (0..P_MAX)
    static final double PRMP_MIN = -1;   // may be < 0
    static final double PRMP_MAX = 3;  

    static final double DELTAB_MAX = 1 ;        // Switching threshold (m/s^2)  
    static final double DELTABRAMP_MIN = -2 ;   // Switching threshold (m/s^2)  
    static final double DELTABRAMP_MAX = 1 ;    // Switching threshold (m/s^2)  
    static final double DELTABRAMP_INIT = -2 ; 

 

	// new variable controlling the CACC number of cars
	static final double CACC_init = 0.2;  //  % of the vehicles arriving are CACC enabled (initial %)
	static final int CACC_MAX = 1;
	static final int CACC_MIN = 0;


    // colors in MicroSim*.java

    // Layout windows - This controls the layout and size of the applet window

    static final int MARGIN = 60;     // space between windows and applet boundary the more you increase the smaller the map becomes and larger the buttons

    static final int SB_SPACEX = 4;   // space between scrollbar window
    static final int SB_SPACEY = 0;   // elements

    static final int TEXTWINDOW_HEIGHT = 110;   // textarea with explanations
    static final int BUTTONWINDOW_HEIGHT = 10;  
    static final int SBWINDOW_HEIGHT = 140;  

    static final boolean SHOW_INSET_DIAG=false;
    static final boolean SHOW_TEXT=false;

    // Geometric simulation constants

    // onramp: Chose straight road geometry (true) or U-shaped (false)
    static final double STRAIGHT_RDLEN_M = 800.;
    static final double EXCESS_RDLEN_M =100.;
    static final double STRAIGHT_RAMPPOS_M = 600.; // center of on-ramp
    static final double ANGLE_ACCESS = 0.2;        // arctan(angle of access road)
    static final double STRAIGHT_ASPECTRATIO= 0.4;


    // U-shaped geometry
    static final double RADIUS_M = 120.;  // of circular road sections
    static final double L_RAMP_M = 100.;  // length of on ramp
    static final double L_STRAIGHT_M = 200.; // of straight sections of U 
    static final double REL_ROAD_MARGIN = 0.01; // relative space between roads
    static final double LANEWIDTH_M = 10.;  // width of one lane
    static final double LINELENGTH_M = 3.;  // white middle lines
    static final double GAPLENGTH_M = 5.;  // white middle lines
    static final double RELPOS_TL= 0.7;  // pos of traffic light/roadlength
    static final double RELPOS_LANECL= 0.7;  
    static final double LANECL_LENGTH = 10;   // Length (m) of closed lane
    static final double RELPOS_SPEEDSIGN= 0.;  // pos of speed-limit sign
    static final double VEH_WIDTH_M= 2.;  // of both cars and trucks
	//static final double VEH_WIDTH_M= 1.6;  // of both cars and trucks
    static final double PKW_LENGTH_M= 4;  
    static final double LKW_LENGTH_M= 6; 

	 

    // Layout text
    static final int    MAX_TEXTHEIGHT = 16;
    static final int    MIN_TEXTHEIGHT = 8;
    static final double REL_TEXTHEIGHT = 0.01; // textheight/simWindow size
    static final double SBTEXT_MAINTEXT_HEIGHTRATIO = 0.8; // for some unknown
    // reason ratio 0.9 or higher makes LARGER font although font size smaller
    // (unknown java bug?) 

    // times
    static final double T_GREEN=120.0; // traffic light turns green
    //static final double TIMESTEP_S=0.4;  // oben!!
    static final double INIT_SIMTIME_MS=20;  
    static final double NT_LINES=5;    // redraw road lines every .. timesteps
    static final double NT_ROAD=10;     // redraw road every .. timesteps
        // tsleepFrame_ms = max((tsleep_ms- SIMTIME_MS)/framesPerStep
    // - DRAWTIME_MS,0)


}
  
