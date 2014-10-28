import java.awt.*;
import java.util.Vector;
import java.util.*;
import java.lang.*;
import java.lang.Math;

// by Georges Arnaout
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
 

public class SimCanvas extends Canvas implements Runnable, Constants{


    // Scaling and global size parameters
    // (determine with width() method AFTER cstr)

    private    int xsize;      // width, height of window in pixels
    private    int ysize;      // y pixel region of circle or U w/o on-ramp
    private double scale;      // scale factor pixels/m
    private double scalex;
    private double scaley;
    final int IMAXINIT=100;
	
	
	// newely implemented (statistics collection)
	protected Vector street = new Vector(IMAXINIT);
	protected Moveable me;
	ArrayList usedArray1 = new ArrayList(); // we create a normal ArrayList where we're going to store 2 other ArrayLists (mini ArrayLists in it)  (ENTERING)
	ArrayList usedArray2 = new ArrayList(); // we create a normal ArrayList where we're going to store 2 other ArrayLists (mini ArrayLists in it)  (EXITING)
	ArrayList usedArray3 = new ArrayList(); // we create another normal ArrayList where we're going to store 2 other ArrayLists (mini ArrayLists in it)  (EXITING)
    ArrayList list1=new ArrayList();  // create the Index of the car (entering)
	ArrayList list2=new ArrayList(); // and follows with its relative time1
	ArrayList list3=new ArrayList();  // create the Index of the car (leaving)
	ArrayList list4=new ArrayList(); // and follows with its relative time2
 


	//ArrayList<ArrayList<String>> usedArray1 = new ArrayList<ArrayList<String>>(); // ArrayList of ArrayLists
 
	// randomness
	protected Random random=new Random(42); // for truck perc. and veh. numbers
	
	// Time spent in system
	double average_time_spent = 0;
	
    private double rdlen_m;
    private double rdlen;         // (not rounded) road length in pixel units
    private double l_straight_m;  // length(m) of straight sections of Ushape
    private    int l_straight; 
    private double total_rmplen_m; // length of onramp + visible access road
    private    int total_rmplen; 
    private double ramppos_m;      // center ramp position if straight road
    private    int ramppos; 

    private    int lRamp; 
    private    int lw;         // lane width
    private    int r1,r2, r3, r4;      // the road lanes (newely implemented)
	private    int g1, g2;
    private    int radius;     // radius of road center
    private    int roadMargin; // space road-margin
    private    int xc,yc;      // center of SimCanvas (for all Scenarios)
    private    int xstart,ystart; // if STRAIGHT_ROAD
    private    int yrmpstart; // begin access road if STRAIGHT_ROAD
    private    int vehWidth;    // in pixels
    private    int pkwLen;    // in pixels
    private    int lkwLen;    // in pixels

	
	// For Randomness
	Random generator = new Random();
    

    // control variables
    private int framesPerStep = FRAMES_PER_TIMESTEP;   
    private static int fcnr=0;     // index of floating car; all 
                            // Moveable objects with 
                            // index=0 painted in same color (blue)

    // parameters influenced by the user; updated from the
    // corresponding variables of MicroSim* by method newValues(..)
 
    private int choice_Szen=2;   // {stopGo+free,obstacles,..,..,sync}
    private double density;
    private double qIn;
    private double perTr;
    private double v0_limit;
    private double v0_limit_old=0;
    private double p_factor;
    private double qRamp;
    private double p_factorRamp;
    private double deltaB;
    private double deltaBRamp;
    private int choice_Geom;  // {Circle, U, Straight}
    private boolean circleCarsRemoved = false;

	// new values
	private double CACC_rate;

    // times
    private double time= 0.;  // Simulation time (reset to 0 in each "run")
    private int itime=0;  // Accumulated simul. tinesteps
    private int itSinceLastChange=0; // if displayed window changed
          // (itSinceLastChange==0) paint road grass etc anow, not only cars
    private int tsleep_ms;
    private int tsleepFrame_ms;
    private int simTime_ms;
    private int prepTime_ms;
    private int paintTime_ms;
    private int avgPaintTime_ms=0;
    private int totalPaintTime_ms;
    private int totalTimeThisStep_ms;

	
	// Exiting Vehicles Variables
	int exit_counter = 0;
	int flow_counter = 0;
	double total_time_spent = 0;

    // Dynamical variables Array implementation
    //!!! radius in m, dens_max in invkm; therefore 0.001!!!

    int imaxinit = (int)(0.001*RADIUS_M * 4 * Math.PI * DENS_MAX_INVKM + 200) * 5 ; // * 4 in order to fix the crash when we have too many agents in the system at one time (imax > imaxinit)
    // initial array size; increase if necessary (!!! yet to do!!!)

    double[] pos = new double [imaxinit+1];
    int   [] lane = new int [imaxinit+1];
	int   [] geo_lane = new int [imaxinit+1];
    double[] lengthVeh = new double [imaxinit+1];
    int   [] nr = new int [imaxinit+1];
    int   [] old_i = new int [imaxinit+1];  // such that nr[i] = old_nr[old_i]
    double[] old_pos = new double [imaxinit+1];
    int   [] old_lane = new int [imaxinit+1];

    double[] posOn = new double [imaxinit+1];
    double[] old_posOn = new double [imaxinit+1];
    double[] lengthVehOn = new double [imaxinit+1];


    // main objects
    private MicroStreet microstreet;
    private OnRamp onramp;

    private Image buffer;
    private boolean painted=false;
    private Image diagram1,diagram2,diagram3,diagram4;

    private Color backgrColor=new Color(0,150,0);  // green grass
    private Color roadColor = Color.lightGray;

    private Graphics bufferedGraphics = null; 
    //    private Graphics fd = null;
    private Thread runner=null;

    private int textHeight; 
    private Font textFont; 
    private FontMetrics metricsText;



    // Inset diagrams
    private int dia1x, dia2x, dia3x, dia4x, dia1y, dia2y, dia3y, dia4y;
    private int diaWidth, diaHeight;
    private TimeAvDet det1_0;
    private TimeAvDet det1_1;
    private SpaceAvDet det2_0;
    private SpaceAvDet det2_1;
    private TimeAvDet det3_0;
    private TimeAvDet det3_1;
    private SpaceAvDet det4_0;
    private SpaceAvDet det4_1;

    private TimeAvDet det5_0;
    private TimeAvDet det5_1;
    private SpaceAvDet det6_0;
    private SpaceAvDet det6_1;
    private TimeAvDet det7_0;
    private TimeAvDet det7_1;
    private SpaceAvDet det8_0;
    private SpaceAvDet det8_1;


    private StatBufDiag fcdxdv;
    private StatBufDiag Qrho;
    private StatBufDiag QrhoS;
    private StatBufDiag Qrho1;
    private StatBufDiag QrhoS1;
    private DynBufDiag fcvt;
    private DynBufDiag Qt;
    
    private int det2c=0;
    private int det4c=0;



    SimCanvas(int choice_Szen, double density, double qIn, double perTr,
                double p_factor, double deltaB, 
                double p_factorRamp, double deltaBRamp, 
                 int tsleep_ms, double CACC_rate){

 
        newValues(choice_Szen, density, qIn, perTr, 33, p_factor, deltaB, tsleep_ms, CACC_rate);
        this.p_factorRamp = p_factorRamp;
        this.deltaBRamp = deltaBRamp;
        tsleepFrame_ms = 0;
		this.CACC_rate = CACC_rate;

        microstreet=new MicroStreet(2 * Math.PI * RADIUS_M,density,p_factor,deltaB,fcnr,choice_Szen);

    }
    
	public double getScale()
	{
		setScales();
		return(scale);
	}


    private void setScales(){

	
      xsize=getSize().width;    //-- Reduced Size for Animation Purposes
//      xsize = 4945; // new Size implemented to increase the highway size to 10 km
//    	xsize = 3001; // new Size implemented to increase the highway size to 6 km
		ysize=getSize().height; 
		
		//	System.out.printl("-----------------XSIZE ----"+ xsize);

        if(xsize<10){ xsize=10;}    // xsize=0  at beginning -> default!!
        if(ysize<10){ ysize=10;}    // ysize=0  at beginning -> default!!


	// determine physical road dimensions
	double xsize_m=500;
    double ysize_m=500;


	if((choice_Szen==2) && STRAIGHT_ROAD){
		
		
          rdlen_m = STRAIGHT_RDLEN_M;
          ramppos_m = STRAIGHT_RAMPPOS_M;
          total_rmplen_m = 0.5*L_RAMP_M + ramppos_m;

          xsize_m = rdlen_m-EXCESS_RDLEN_M; 
          ysize_m = STRAIGHT_ASPECTRATIO*xsize_m;
          scalex = xsize/xsize_m;
          scaley = ysize/ysize_m;
          scale = scalex<scaley ? scalex : scaley;
          l_straight_m=0.;               // not used
	}

        else{ // no straight road and/or not choice_Szen = 2

          xsize_m =  2 * RADIUS_M; 
          ysize_m = 2*RADIUS_M + ((choice_Szen==2) ? 3 : 2) * LANEWIDTH_M;
          xsize_m *= (1+2*REL_ROAD_MARGIN);
          ysize_m *= (1+2*REL_ROAD_MARGIN);
          scalex = xsize/xsize_m;
          scaley = ysize/ysize_m;
          scale = scalex<scaley ? scalex : scaley;
		// changes the scale of the model  
		//scale = 0.7;

          l_straight_m = xsize/scale - RADIUS_M;
          rdlen_m = Math.PI *RADIUS_M + 2*l_straight_m;  
          total_rmplen_m = RADIUS_M * (1+REL_ROAD_MARGIN) +L_RAMP_M;  
	}

	// determine pixel road dimensions
  	if(true){
          rdlen = rdlen_m * scale;
          total_rmplen = (int)(total_rmplen_m*scale);
          lRamp     = (int)(L_RAMP_M * scale);
          xstart = -(int)(scale*EXCESS_RDLEN_M);    
          ystart = (int)((1.-STRAIGHT_ASPECTRATIO)*ysize);
          ramppos = xstart + (int)(ramppos_m * scale);
          yrmpstart = ystart + (int)(scale*ANGLE_ACCESS*(ramppos_m - 0.5*L_RAMP_M));
          l_straight     = (int)(l_straight_m * scale);
          roadMargin =  (int)(REL_ROAD_MARGIN*ysize);
          radius = (int)(RADIUS_M*scale);
          lw    = (int) (LANEWIDTH_M * scale);            // Lane width
          r1    = radius-lw;   // inner radius
          r2    = radius+lw;   // outer radius
		  r3    = r2 - 16;
		  r4    = r1 - 16;      
          xc  = roadMargin + lw + radius;   // center of circle or semicircle
          yc  = roadMargin + lw + radius;   // (circle and U Scenarios)
 
		//rdlen = 5000;
		

	}  // end if true

        System.out.println("SimCanvas.setScales:"
             + " xsize_m="+xsize_m
             + " xsize="+xsize
             + " ysize="+ysize
             + " xstart="+xstart
             + " ystart="+ystart
             +", l_straight_m="+l_straight_m
             +", rdlen="+rdlen 
			 +", rdlen_m="+rdlen_m
			 +", scale="+scale
             );


	// vehicles (length varies from veh to vehicle)
        vehWidth = (int)(VEH_WIDTH_M * scale);
        pkwLen = (int)(PKW_LENGTH_M * scale);
		pkwLen = 4;
	    lkwLen = (int)(LKW_LENGTH_M * scale);
        lkwLen = 6;
        System.out.println("car length: "+pkwLen);
		System.out.println("truck length: "+lkwLen);
	 

        //textHeight = (int)(REL_TEXTHEIGHT * xsize); 
        textHeight = (int)(REL_TEXTHEIGHT * getSize().width); 
        if (textHeight>MAX_TEXTHEIGHT) textHeight=MAX_TEXTHEIGHT;
        if (textHeight<MIN_TEXTHEIGHT) textHeight=MIN_TEXTHEIGHT;

        textFont=new Font("SansSerif",Font.PLAIN,textHeight);
        metricsText=getFontMetrics(textFont);



       // layout of the insert diagrams
       
       double actsize = scale * 2 * RADIUS_M;

       diaHeight=(int)(0.275*actsize);

 
       if (choice_Szen==6){;}

       if (choice_Szen==5){  // Ushape
	   int xcenter=(int)(0.5*((0.9*xsize)+(0.225*actsize)));
	   diaWidth=(int)(2*(xcenter-(0.225*actsize)));	   
	   dia1x=xcenter-(int)(0.5*diaWidth);
	   dia1y=(int)(0.225*actsize);
	   dia2x=xcenter-(int)(0.5*diaWidth);
	   dia2y=(int)(0.5*actsize);
	   
       }
       else { // Ushape
 	   int xcenter=(int)(0.5*((0.9*xsize)+(0.225*actsize)));
	   diaWidth=(int)(xcenter-(0.225*actsize));
	   dia1x=xcenter-diaWidth;
	   dia1y=(int)(0.225*actsize);
	   dia2x=xcenter;
	   dia2y=(int)(0.225*actsize);
	   dia3x=xcenter-diaWidth;
	   dia3y=(int)(0.5*actsize);
	   dia4x=xcenter;
	   dia4y=(int)(0.5*actsize);

        }

       // <Treiber aug06>

       boolean isRingroad=(choice_Szen==6);

       diaWidth =(int)(0.22*actsize);
       diaHeight=(int)(0.18*actsize);

       dia1x =(isRingroad) 
             ?  (int)(0.99*xsize) - 2*diaWidth
             :  (int)(0.80*xsize) - 2*diaWidth;
       dia2x =dia1x+diaWidth;
       dia3x =dia1x;
       dia4x =dia2x;

       dia1y =(isRingroad) 
             ?  (int)(0.80*ysize) - 2*diaHeight
             :  (int)(0.85*ysize) - 2*diaHeight;
       dia2y =dia1y;
       dia3y =dia1y+diaHeight;
       dia4y =dia3y;
      // </Treiber>

    }
    // end setScales


    public void run(){

	//	long startTime = System.currentTimeMillis();
	
        System.out.println("SimCanvas.run()");

        boolean traffLightTurnedGreen=false;
        //time= -300.; // warm up period 5 mins
		time = 0;

	setScales();
        if (SHOW_INSET_DIAG){setInsetDiagrams();}

	// The main loop: runs until current thread no longer runner;
	// if stop or new scenario -> stop() method applied:
	// sets runner=null -> run() method ends

       	while (Thread.currentThread()== runner){
	//while ((Thread.currentThread()== runner) && (time<100)){
	    try {Thread.sleep(tsleep_ms);} 
		catch (InterruptedException e) { ; }


	    // simulate new timestep
        long timeBeforeSim_ms=System.currentTimeMillis();

	    if ((time>T_GREEN)&&(choice_Szen==5) &&(traffLightTurnedGreen==false)){

		microstreet.open();   // traffic light turns green
		traffLightTurnedGreen=true; // (only traffic light scenario)
	    }
	    microstreet.update(TIMESTEP_S, choice_Szen, density, qIn, perTr, p_factor, deltaB, CACC_rate);
	    
			if (choice_Szen==2)
			{
				onramp.update(TIMESTEP_S,qRamp,perTr, p_factorRamp, deltaBRamp, CACC_rate);
			}
			if (SHOW_INSET_DIAG){updateInsetDiagrams();}


	    // calculate some times
	    time              += TIMESTEP_S;    
	    itime             ++;
	    itSinceLastChange += 1;  
 



        simTime_ms = (int)(System.currentTimeMillis() -timeBeforeSim_ms);

  	    int dtf_ms=-1;  //!!!time

            tsleepFrame_ms  = (int)(
               (tsleep_ms- simTime_ms - prepTime_ms)/framesPerStep - avgPaintTime_ms + dtf_ms);

            if (tsleepFrame_ms<2) tsleepFrame_ms=2;


	   

	    repaint();  

	    // totalTimeThisStep_ms should be the same or a little bit 
	    // smaller as tsleep
	    // !! Cannot measure runtime of repaint -> 0 ms if 
	    // times logged before and after repaint()

            totalTimeThisStep_ms = simTime_ms+prepTime_ms+totalPaintTime_ms;

	    if(false){
              System.out.println("tsleep="+tsleep_ms
                +", tsleepFr="+tsleepFrame_ms
                +", simT="+simTime_ms
                +", prepT="+prepTime_ms
                +", avgPaintT="+avgPaintTime_ms
                +", totPaintT="+totalPaintTime_ms
                +", totT="+totalTimeThisStep_ms
              );
	    }

	}

    }



    // called from start() method of main applet class MicroSim*

    public void start(int choice_Szen, double density){
      System.out.println("SimCanvas.start(choice_Szen="+choice_Szen+  ", density="+density+")");
      microstreet=new MicroStreet(rdlen_m, density,p_factor,deltaB,fcnr,choice_Szen);
		if((choice_Szen==3)||(choice_Szen==4)||(choice_Szen==5))
		{
			imposeSpeedLimit();
		}
      
		if(choice_Szen==2)
		{
			double mergingpos = (STRAIGHT_ROAD) ? ramppos_m : Math.PI * RADIUS_M + l_straight_m + 0.5*L_RAMP_M;
	
			System.out.println("SimCanvas.start: mergingpos="+mergingpos
				+",l_straight_m="+l_straight_m
				+",total_rmplen_m="+total_rmplen_m
				);

			onramp=new OnRamp (microstreet,total_rmplen_m, L_RAMP_M, mergingpos, p_factorRamp, deltaBRamp, CACC_rate);
		}

      if (runner==null){
          runner=new Thread(this);
          runner.start();
      }
    }
    
    // called from stop() method of main applet class MicroSim*


	public void stop()
	{
		System.out.println("SimCanvas.stop()");
		System.out.println("Simulation is Over");
		if (runner != null)
		{
			Thread waitFor = runner;
			runner= null;      // kill thread (seems necessary) 
			// -> start=start from new
			try {waitFor.join(tsleep_ms);} 
			catch (InterruptedException e){}
		}
	}


    // update necessary!! repaint() [standard mechanism for animation]
    // calls per default update(g), 
    // and the standard update(g) per default 
    // clears everything before it calls
    // paint(); oversede with own update(g) which does not clear prior
    // to painting!

    // MicroApplet2_0 
	public void update (Graphics g)
	{
		if(!DOUBLEBUF){paintSim(g, framesPerStep);}
		else{ paintBuffer(g);  }
	}

    // only called if display window changed or reactivated; 
    // otherwise update() called for painting

	public void paint (Graphics g)
	{
		//System.out.println("SimCanvas.paint()");
		itSinceLastChange=0;
		paintBuffer(g); // also if no double buffering!
	}


	public int george_random()
	{
		int randomIndex = generator.nextInt( 2 );   // generates a random number between 0 and 1
		return randomIndex;
	}

	private void paintBuffer (Graphics g)
	{    // make buffered graphics object bg  "Doppelpufferung"

		if (buffer == null)
		{
			buffer = createImage(xsize,ysize);
			bufferedGraphics = buffer.getGraphics();
		}
		if (choice_Geom==0){paintCircle(bufferedGraphics);}
		if (choice_Geom==1){paintU(bufferedGraphics);}
		if (choice_Geom==2){paintStraight(bufferedGraphics);}
		paintSim(bufferedGraphics);                  // paint into buffer
		g.drawImage (buffer, 0, 0, this);         // display buffer
	}
      
      

    // paint actual road simulation to Graphics object g 
    // which may be screen or buffer

    // MicroApplet2_0 
    private void paintSim(Graphics g){
        paintSim(g,1);
    }

    private void paintSim(Graphics g, int framesPerStep){


		if ((itSinceLastChange>=1) && (time>=TIMESTEP_S)) 
		{
			long timeBeforePrep_ms = System.currentTimeMillis();
			prepareVehData();
			prepTime_ms = (int)(System.currentTimeMillis()-timeBeforePrep_ms);

			long timeBeforePaint_ms = System.currentTimeMillis();
			if (choice_Geom==0)
			{
				updateCircle(g,framesPerStep);}
			if (choice_Geom==1){updateU(g,framesPerStep);}
			if (choice_Geom==2){updateStraight(g,framesPerStep);}
			totalPaintTime_ms = (int) 
				(System.currentTimeMillis()-timeBeforePaint_ms);
		}

		else
		{
			//!! repainting action after returning to applet;
			// paint also the vehivles; therefore also the updte.. calls
			if (choice_Geom==0){paintCircle(g);updateCircle(g,1);}
			if (choice_Geom==1){paintU(g);updateU(g,1);}
			if (choice_Geom==2){paintStraight(g);updateStraight(g,1);}
		}

      paintLegend(g);
      paintTimes(g);
      paintSymbols(g);
      if (SHOW_INSET_DIAG){paintInsetDiagrams(g);}
      if (((choice_Szen==3)||(choice_Szen==4)||(choice_Szen==5)) &&(v0_limit!=v0_limit_old)){
          imposeSpeedLimit(); // !!! Bring outside of graph; not confuse
	  // action with painting!!
          paintSpeedLimit(g);
      }
    }


    // ################################################################
    // central simulation visulalization: paints street
    //  and all vehicles for closed systems
    // First time: call paintCircle, paintU etc: Street, grass + vehicles
    // After this: call updateCircle etc: Only cars moved
    // ################################################################


	private void paintCircle(Graphics g)
	{

		g.setColor(backgrColor);                 // green grass
		g.fillRect(0,0,xsize, ysize);			 // all this does is draw a green rectangle where the width and height are xsize and ysize (covering the whole area concerned)

		//paintCircleRoad(g);
      
		g.setColor(Color.pink);                 // grey circle with outer diameter (notice the virtual cars in Scenario 1)
		g.fillOval(xc-r2,yc-r2,2*r2,2*r2);
		g.setColor(Color.blue);                  // I just colored blue the inner diameter
		g.fillOval(xc-r1,yc-r1,2*r1,2*r1);
      
		drawLines(g);
	}


    private void paintCircleRoad(Graphics g){
      System.out.println("Simcanvas.paintCircleRoad...");
      final int NPOINTS = 50;
      int[] xpoints = new int[2*NPOINTS+2];
      int[] ypoints = new int[2*NPOINTS+2];

      for (int i=0; i<NPOINTS+1; i++){
         DetPosC dp = new DetPosC(rdlen_m * i / NPOINTS, LANEWIDTH_M);
         xpoints[i] = dp.x;
         ypoints[i] = dp.y;
      }
      for (int i=NPOINTS+1; i<2*NPOINTS+2; i++){
         DetPosC dp = new DetPosC
          (rdlen_m * (i-2*NPOINTS-1) / NPOINTS, -LANEWIDTH_M);
         xpoints[i] = dp.x;
         ypoints[i] = dp.y;
      }

      g.setColor(roadColor); 
      g.fillPolygon(xpoints, ypoints, 2*NPOINTS+2);
      drawLines(g);

    }


    private void updateCircle(Graphics g, int framesPerStep){

      // calculate rectangles for new and old vehicle positions and draw.
      // Remove a little bit more (make old rectangle larger); otherwise lost vehicle
      // pixels might appear; pos = back (!) edge of vehicle
      // same nr -> same length -> need no old length!

      Vector colors=microstreet.colors;
      int imax = colors.size()-1;
      int sumPaintTime_ms=0;

      for (int iframes=1; iframes<=framesPerStep; iframes++){

        long timeBeforePaint_ms = System.currentTimeMillis();

        double weightNew = (double) iframes/FRAMES_PER_TIMESTEP;
        double weightOld = (double)(iframes-1)/FRAMES_PER_TIMESTEP;

        for (int i=imax; i>=0; i--){
          PolyVehCircle pvc= new PolyVehCircle(
             weightNew*pos[i]+(1.-weightNew)*old_pos[i], 
             weightNew*lane[i]+(1.-weightNew)*old_lane[i], 
             lengthVeh[i], VEH_WIDTH_M, r1, r2, xc, yc);

	  // again, old veh a little big larger -> initpos-=1, length+=2
          PolyVehCircle pvc_old= new PolyVehCircle(
             weightOld*pos[i]+(1.-weightOld)*old_pos[i] - 1, 
             weightOld*lane[i]+(1.-weightOld)*old_lane[i], 
             lengthVeh[i]+2, VEH_WIDTH_M+2, r1, r2, xc, yc);

          int car_xPoints[] = pvc.xPoints;
          int car_yPoints[] = pvc.yPoints;
          int car_old_xPoints[] = pvc_old.xPoints;
          int car_old_yPoints[] = pvc_old.yPoints;
	  //
	  // paint old vehicle polygons with background color
          g.setColor(roadColor);
	      g.fillPolygon(car_old_xPoints, car_old_yPoints, 4);

	  // paint vehicle polygon at new position
          if ((SHOW_INSET_DIAG)&&(nr[i]==fcnr)){ g.setColor(Color.blue); }
          else{ g.setColor((Color)(colors.elementAt(i)));}
	      g.fillPolygon(car_xPoints, car_yPoints, 4);

	}

        paintTime_ms = (int)(System.currentTimeMillis() - timeBeforePaint_ms);
        sumPaintTime_ms += paintTime_ms;
        try{Thread.sleep(tsleepFrame_ms);}catch(InterruptedException e){};

      }

      avgPaintTime_ms = sumPaintTime_ms/framesPerStep;

      // remove old cars
      if ((itime % NT_ROAD==0)&&(circleCarsRemoved)){
        paintCircleRoad(g);}  
      if ( itime % NT_LINES==0){drawLines(g);}

    }



    private void paintStraight(Graphics g){
      System.out.println("in paintStraight ...");
      g.setColor(backgrColor);          // green grass everywhere
      g.fillRect(0,0,xsize, ysize);
      g.setColor(roadColor);            // straight road sections
      g.fillRect(xstart,ystart-2*lw,(int)rdlen,2*lw); // left-upper pt, obj size

      // draw on-ramp beginning at xc
		if(choice_Szen==2)
		{
			g.setColor(roadColor); 

			// section parallel to main road
			int x_merge_end  =  (int)(ramppos + 0.5*lRamp);      
			g.fillRect(x_merge_end-lRamp,ystart,lRamp,lw);

			// "merging taper" at end of on ramp
			int[] poly_x = {x_merge_end,x_merge_end,x_merge_end+2*lw};
			int[] poly_y = {ystart,ystart+lw,ystart};
			g.fillPolygon(poly_x,poly_y,poly_x.length);

			// access road
			int[] poly2_x ={xstart,xstart,x_merge_end-lRamp,x_merge_end-lRamp};
			int[] poly2_y ={yrmpstart,yrmpstart+lw,ystart+lw,ystart};
			g.fillPolygon(poly2_x,poly2_y,poly2_x.length);

		}

      // lane-separating road lines
      drawLines(g);

      // speed-limit sign for Scen 3..5
      if((choice_Szen==3)||(choice_Szen==4)||(choice_Szen==5)){
	  paintSpeedLimit(g);}

   }

// (code modified by Georges Arnaout)
    private void paintU(Graphics g){  // MAIN STREET BUILDER!!!!!

      g.setColor(backgrColor);         // green grass everywhere
      g.fillRect(0,0,xsize, ysize);    // paint the outer C part of the curve with green

      g.setColor(roadColor);             // paint circle as in paintCircle
      g.fillOval(xc-r2,yc-r2,2*r2,2*r2); // paint the C road with roadColor (and fill the inside with roadColor)
	
      g.setColor(backgrColor);           // Green grass color
      g.fillOval(xc-r4,yc-r4,2*r4,2*r4); // paint the inner C part of the curve with green (modified to fit 4 lanes)

      g.fillRect(xc,0,xsize, ysize);  // HIDE the right part of the circle in order to have a 180 degrees C cuve and not a  360 degrees circle

      g.setColor(roadColor);            // two straight road sections
      
		
	// the four lanes of the road graphics (code implemented by Georges Arnaout)
	g.fillRect(xc,yc-r2,l_straight,2*lw);     // LANE 1 & 2 (outer / up)
    g.fillRect(xc,yc+r1,l_straight,2*lw);     // LANE 1 & 2 (outer / bottom)
    g.fillRect(xc,yc-r3,l_straight,2*lw);     // LANE 3 & 4 (inner / up)
	g.fillRect(xc,yc+r4,l_straight,2*lw);     // LANE 3 & 4 (inner / bottom)
 

      // draw on-ramp for Szenario 2
		if(choice_Szen==2)
		{            
			g.setColor(roadColor); 
			g.fillRect(0,yc+r2,xc+lRamp,lw);
			int[] poly_x = {xc+lRamp,xc+lRamp,xc+lRamp+2*lw};
			int[] poly_y = {yc+r2,yc+r2+lw,yc+r2};
			g.fillPolygon(poly_x ,poly_y,poly_x.length);
		}

      // lane-separating road lines
      drawLines(g);

      // speed-limit sign for Scen 3..5
      if((choice_Szen==3)||(choice_Szen==4)||(choice_Szen==5)){
	  paintSpeedLimit(g);}

   }




    private void updateU(Graphics g, int framesPerStep){

	 	  
      timeSpent();
	  //density();

      Vector colors=microstreet.colors;
      int imax = colors.size()-1;
      int sumPaintTime_ms=0;


      // paint mainroad and possibly ramp vehicles in iframes steps

      for (int iframes=1; iframes<=framesPerStep; iframes++){

        long timeBeforePaint_ms = System.currentTimeMillis();
        double weightNew = (double) iframes/FRAMES_PER_TIMESTEP;
		//System.out.println("---------------------- weightNew: -------------"+weightNew); // is 1 ALWAYS
        double weightOld = (double)(iframes-1)/FRAMES_PER_TIMESTEP;
		//System.out.println("---------------------- weightOld: -------------"+weightOld); // is 0 ALWAYS

 

	// main road vehicles
	//System.out.println("updateU: 3");
   
		  for (int i=imax; i>=0; i--)
		  {
			  PolyVehU pvu  = new PolyVehU(
				   weightNew*pos[i]+ (1.-weightNew)*old_pos[i], 
				   weightNew*lane[i]+(1.-weightNew)*old_lane[i],  // or can be replaced with simply lane[i] since weightNew is always 1
				   lengthVeh[i], VEH_WIDTH_M, r2, r1, xc, l_straight, yc, (int)(rdlen));  		  

		 
			  PolyVehU pvu_old=new PolyVehU(
				  weightOld*pos[i]+(1.-weightOld)*old_pos[i] - 1, 
				  weightOld*lane[i]+(1.-weightOld)*old_lane[i], 
				  lengthVeh[i]+2, VEH_WIDTH_M+2, r2, r1, xc, l_straight, yc, (int)(rdlen)); 
 			        
			  int car_xPoints[] = pvu.xPoints;
			  int car_yPoints[] = pvu.yPoints;
			  int car_old_xPoints[] = pvu_old.xPoints;
			  int car_old_yPoints[] = pvu_old.yPoints;
 
			  // paint old vehicle polygons with background color
			  //g.setColor(Color.pink);
			  g.setColor(roadColor);
			  g.fillPolygon(car_old_xPoints,  car_old_yPoints, 4);          // PAINT VIRTUAL CAR (original)
			//  g.fillPolygon(car_old_xPoints2, car_old_yPoints2, 4);       // PAINT VIRTUAL CAR (clone)

			  // paint vehicle polygon at new position
			  if ((SHOW_INSET_DIAG)&&(nr[i]==fcnr)){ g.setColor(Color.blue); }
			  else
			  { 
				  g.setColor((Color)(colors.elementAt(i)));
			  }
			  g.fillPolygon(car_xPoints, car_yPoints, 4);            // THIS LINE PAINTS THE CARS ON THE HIGHWAY (LANE 1 and LANE 2)

			  	  
		
//			  // Georges : We don't want CACC vehicles on the clone lanes so:
//			  if ((Color)(colors.elementAt(i)) != (Color.blue)  )
//			  {
//				  // g.setColor(Color.red); // will turn them all to red
//				  g.setColor((Color)(colors.elementAt(i)));
//				  //(Moveable)  
//			  }
//			  else
//			  {
//			    g.setColor(Color.red);
//			  }
//

		//	  g.fillPolygon(car_xPoints2, car_yPoints2, 4);          // THIS LINE PAINTS THE CARS ON THE HIGHWAY (LANE 3 and LANE 4) - clones
		  
		  } //end of for loop


	  // if choice_Szen=2, paint ramp vehicles
        if(choice_Szen==2)
		{
           int imaxOn = onramp.colors.size()-1;
           int yTopOn    = yc+r2+(int)(0.25*lw);

           // paint new merging region 
           // (otherwise paint of changed veh not removed)

	   //         if(choice_Szen==2){
           int xmerge = xc;
           int lmerge = lRamp;
           g.setColor(roadColor); 
           g.fillRect(xmerge,yc+r2,lmerge,lw);
	   //         }

           for (int i=imaxOn; i>=0; i--){

	      // old pos

              int length = (int)(scale*lengthVehOn[i]);
              double pos_m = weightOld*posOn[i]+(1.-weightOld)*old_posOn[i];
              int xstart   = xc +lRamp + (int)(scale * (pos_m - onramp.roadLength ));
              g.setColor(roadColor);
              g.fillRect(xstart, yTopOn, length, vehWidth); 

	      // new pos

              pos_m = weightNew*posOn[i]+(1.-weightNew)*old_posOn[i];
              xstart   = xc +lRamp + (int)(scale * (pos_m - onramp.roadLength ));
              g.setColor((Color)(onramp.colors.elementAt(i)));
              g.fillRect(xstart, yTopOn, length, vehWidth); 
	   }
	}

        paintTime_ms = (int)(System.currentTimeMillis() - timeBeforePaint_ms);
        sumPaintTime_ms += paintTime_ms;
        try{Thread.sleep(tsleepFrame_ms);}catch(InterruptedException e){};

      }
      avgPaintTime_ms = sumPaintTime_ms/framesPerStep;

      if ( itime % NT_LINES==0){drawLines(g);}

    } // end updateU



    private void updateStraight(Graphics g, int framesPerStep){
      System.out.println("in updateStraight");


      Vector colors=microstreet.colors;
      int imax = colors.size()-1;
      int sumPaintTime_ms=0;


      // paint mainroad and possibly ramp vehicles in iframes steps

      for (int iframes=1; iframes<=framesPerStep; iframes++){

        long timeBeforePaint_ms = System.currentTimeMillis();

        double weightNew = (double) iframes/FRAMES_PER_TIMESTEP;
        double weightOld = (double)(iframes-1)/FRAMES_PER_TIMESTEP;


	// main road vehicles    // for the ramp 
	for (int i=imax; i>=0; i--)
	{
          PolyVehStraight pvs  = new PolyVehStraight(
             weightNew*pos[i]+(1.-weightNew)*old_pos[i], 
             weightNew*lane[i]+(1.-weightNew)*old_lane[i], 
             lengthVeh[i], VEH_WIDTH_M, (int)(rdlen));  

          PolyVehStraight pvs_old=new PolyVehStraight(
             weightOld*pos[i]+(1.-weightOld)*old_pos[i] - 1, 
             weightOld*lane[i]+(1.-weightOld)*old_lane[i], 
             lengthVeh[i]+2, VEH_WIDTH_M+2, (int)(rdlen));
       
          int car_xPoints[] = pvs.xPoints;
          int car_yPoints[] = pvs.yPoints;
          int car_old_xPoints[] = pvs_old.xPoints;
          int car_old_yPoints[] = pvs_old.yPoints;

	  // paint old vehicle polygons with background color

          g.setColor(roadColor);
          g.fillPolygon(car_old_xPoints, car_old_yPoints, 4);

	  // paint vehicle polygon at new position

          if ((SHOW_INSET_DIAG)&&(nr[i]==fcnr)){ g.setColor(Color.blue); }
          else{ g.setColor((Color)(colors.elementAt(i)));}
          g.fillPolygon(car_xPoints, car_yPoints, 4);
	}


	  // if choice_Szen=2, paint ramp vehicles

        if(choice_Szen==2){
           int imaxOn = onramp.colors.size()-1;

           // paint new merging region 
           // (otherwise paint of changed veh not removed)

	   g.setColor(roadColor); 
	   int x_merge_end  =  (int)(ramppos + 0.5*lRamp);      
       g.fillRect(x_merge_end-lRamp-10,ystart,lRamp+10,lw);

           for (int i=imaxOn; i>=0; i--){
             PolyVehStraightRamp pvs  = new PolyVehStraightRamp(
               weightNew*posOn[i]+(1.-weightNew)*old_posOn[i], 
               lengthVehOn[i], VEH_WIDTH_M, (int)(total_rmplen));  

             PolyVehStraightRamp pvs_old=new PolyVehStraightRamp(
               weightOld*posOn[i]+(1.-weightOld)*old_posOn[i] - 1, 
               lengthVehOn[i]+2, VEH_WIDTH_M+2, (int)(total_rmplen));

             int car_xPoints[] = pvs.xPoints;
             int car_yPoints[] = pvs.yPoints;
             int car_old_xPoints[] = pvs_old.xPoints;
             int car_old_yPoints[] = pvs_old.yPoints;


	     // paint old vehicle polygons with background color
             g.setColor(roadColor);
             g.fillPolygon(car_old_xPoints, car_old_yPoints, 4);

	     // paint vehicle polygon at new position

             if ((SHOW_INSET_DIAG)&&(nr[i]==fcnr)){ g.setColor(Color.blue); }
             else{ g.setColor((Color)(onramp.colors.elementAt(i)));}
             g.fillPolygon(car_xPoints, car_yPoints, 4);
	   }

	} // end choice_Szen=2, Straight road

        paintTime_ms = (int)(System.currentTimeMillis() - timeBeforePaint_ms);
        sumPaintTime_ms += paintTime_ms;
        try{Thread.sleep(tsleepFrame_ms);}catch(InterruptedException e){};

      }  // end frames loop

      avgPaintTime_ms = sumPaintTime_ms/framesPerStep;

      if ( itime % NT_LINES==0){drawLines(g);}

    } // end updateStraight


    private void prepareVehData(){

      Vector positions=microstreet.positions;
      Vector numbers=microstreet.numbers;
      Vector lanes=microstreet.lanes;
	  //int lanes= generator.nextInt( 2 );   // generates a random number between 0 and 1 TESTING
      //      Vector colors=microstreet.colors;
      Vector lengths=microstreet.lengths;
      Vector old_positions=microstreet.old_pos;
      Vector old_lanes=microstreet.old_lanes;
      Vector old_numbers=microstreet.old_numbers;

      int imax   = positions.size()-1;
	  
      //System.out.println("---------------------- Number of Cars in system: -------------"+ imax * 2); // because we are cloning another 2 lanes into the system (so * 2)
      int imaxOn=0;
      if(choice_Szen==2){ imaxOn=onramp.positions.size()-1;}

      circleCarsRemoved=microstreet.circleCarsRemoved;
		if(circleCarsRemoved){System.out.println("circleCarsRemoved!");}

		if(imax > imaxinit)
		{
			System.out.println("imax>imaxinit!! Doubling array size...");
		}



      // define the arrays
		for (int i=imax; i>=0; i--)
		{

			pos[i]           = ((Double) positions.elementAt(i)).doubleValue();
			lane[i]          = ((Integer) lanes.elementAt(i)).intValue();
			nr[i]        = ((Integer) numbers.elementAt(i)).intValue();
			
			//geo_lane[i]      = ((Integer) (i % 2)).intValue();
			geo_lane[i]      = ((Integer) lanes.elementAt(i)).intValue();
			//geo_lane[i]      = generator.nextInt( 4 ); // this will create messy behavior - we need to define it from Microstreet
			//System.out.println("----------------------  LANE[i]: -------------"+ lane[i]);
			//System.out.println("----------------------  GEO LANE: -------------"+ lane[i]);
			//System.out.println("----------------------  IMAX: -------------"+ imax);
			
 
			lengthVeh[i] = ((Double) lengths.elementAt(i)).doubleValue();
			//nr[i]        = ((Integer) numbers.elementAt(i)).intValue();

			// determine vector index of same car in the past time step
			old_i[i]     = i;	  
			int old_nr   = ((Integer) old_numbers.elementAt(i)).intValue();

			//   try{

			if (old_nr != nr[i])
			{   // overtaking during last step -> changed pos
				old_i[i] = (i+1<=imax) ? (i+1) : 0;  // old vehicle number at i+1?
				//  System.out.println("1: i="+i+" old_i[i]="+old_i[i]);
				old_nr = ((Integer) old_numbers.elementAt(old_i[i])).intValue();

				if(old_nr!=nr[i])
				{   // veh index not at i+1 -> must be at i-1
					old_i[i] = (i-1>=0) ? (i-1) : imax;
					//  System.out.println("2: i="+i+" old_i[i]="+old_i[i]);
					old_nr = ((Integer) old_numbers.elementAt(old_i[i])).intValue();
				}
			}

			if (old_nr != nr[i])
			{   // not yet ordered? 
				//System.out.println("prepareVehData, 2nd ordering round!!");
				for (int j=2; ((j<=5)&&(old_nr != nr[i])); j++)
				{
					old_i[i] = (i+j<=imax) ? (i+j) : 0; 
					old_nr = ((Integer) old_numbers.elementAt(old_i[i])).intValue();
				}
				if(old_nr!=nr[i])
				{  
					//System.out.println("prepareVehData, 2nd ordering round,2!!");
					for (int j=-2; ((j>=-5)&&(old_nr != nr[i])); j--)
					{
						old_i[i] = (i+j>=0) ? (i+j) : imax;
						old_nr =((Integer)old_numbers.elementAt(old_i[i])).intValue();
					}
				}
				if(old_nr!=nr[i]){ 
					//System.out.println ("Error! old vehicle number not in neighborhood!");
					}
			}

			old_lane[i]  = ((Integer) old_lanes.elementAt(old_i[i])).intValue();
			old_pos[i] = ((Double) old_positions.elementAt(old_i[i])).doubleValue();
		}

      // on-ramp data

      if (choice_Szen==2){
        for (int i=imaxOn; i>=0; i--){
          posOn[i]    = ((Double) onramp.positions.elementAt(i)).doubleValue();
          old_posOn[i] = ((Double) onramp.old_pos.elementAt(i)).doubleValue();
          lengthVehOn[i]=((Double) onramp.lengths.elementAt(i)).doubleValue();
	}
      }

    }



	public void density()
	{
		Vector positions=microstreet.positions;
		Vector numbers=microstreet.numbers;
		Vector lanes=microstreet.lanes;
		//int lanes= generator.nextInt( 2 );   // generates a random number between 0 and 1 TESTING
		//      Vector colors=microstreet.colors;
		Vector lengths=microstreet.lengths;
		int imax   = positions.size()-1;
		int counter = 0;
        

		// define the arrays
		for (int i=imax; i>=0; i--)
		{

			pos[i]           = ((Double) positions.elementAt(i)).doubleValue();
			lane[i]          = ((Integer) lanes.elementAt(i)).intValue();
			nr[i]        = ((Integer) numbers.elementAt(i)).intValue();

			//////////////////////////////////////////////////////////// TIME SPENT IN THE SYSTEM ////////////////////////////////////////////////////////////
			////            int min, sec;
			////			min=(int)(time/60.0);
			////			sec=(int)(time-min*60.0);
			double myPosition = pos[i];     // Get the car's current position
			int myIndex = nr[i];            // Get the car's index (to avoid looping repetitions)
			boolean found = false;
	
			
			if( (int)myPosition >= 100  && (int)myPosition  <= 1100 ) // to count number of vehicles in 1 km
			{
			
					//System.out.println("Counter Working");
				    counter +=1;
			
			}
		}

		//System.out.println("Traffic Density (in 4 lanes) = "+counter);
		// reset counter
		counter = 0;
	
	
	
	}

	public void timeSpent() // collect time spent AND flow rate
	{
	
		Vector positions=microstreet.positions;
		Vector numbers=microstreet.numbers;
		Vector lanes=microstreet.lanes;
		Vector lengths=microstreet.lengths;
		int imax   = positions.size()-1;
        

		// define the arrays
		for (int i=imax; i>=0; i--)
		{
			pos[i]           = ((Double) positions.elementAt(i)).doubleValue();
			lane[i]          = ((Integer) lanes.elementAt(i)).intValue();
			nr[i]        = ((Integer) numbers.elementAt(i)).intValue();

			//////////////////////////////////////////////////////////// TIME SPENT IN THE SYSTEM ////////////////////////////////////////////////////////////
			////            int min, sec;
			////			min=(int)(time/60.0);
			////			sec=(int)(time-min*60.0);
			double myPosition = pos[i];     // Get the car's current position
			int myIndex = nr[i];            // Get the car's index (to avoid looping repetitions)
			boolean found = false;
			double time1 = 0;
			double time2 = 0;
			double time3 = 0;
			double init_time = 0;
			double time_spent = 0;
			
			//System.out.println("---------------------- myPosition of >>>"+i+"<<< is "+myPosition);
			//System.out.println("---------------------- Car Index -------------"+myIndex);

			// To get the average time spent by each agent in the system (CACC, Trucks, and normal cars)
			if( (int)myPosition  <= 20 ) // beginning of the highway (part 1/2)
			{ 
				if( list1.contains( myIndex ) )   // to make sure we don't have redundancy (collecting several times for the same vehicle)
				{
					//System.out.println("Found in array so don't use that index's time");
					break;
				}
				else
				{
					//System.out.println("Not found in the array - use its time");
					time1 = time; // collect the initial time of entry of the vehicle
					//System.out.println("---------------------- Time1 of >>>"+myIndex+"<<< is "+time1);
					list1.add(myIndex); // add it to the list of vehicles which we've collected their time of entry
					list2.add(time1);   // add the time to the list of times for all vehicles 
					//System.out.println("---------------------- Time1 of >>>"+myIndex+"<<< is "+list2.get(list2.indexOf(time1)));
					usedArray1.add(list1);          // add Index to the main array list usedArray1
					usedArray1.add(list2);          // and add its relative time1
				}

			}
			
	 
			
			if( (int)myPosition >= 6000  && (int)myPosition  <= 6300 )        
			{
				//System.out.println("---------------------- My TIME: ----------------"+ myTime);
				if( list3.contains( myIndex ) )
				{
					//System.out.println("Found in array so don't use that index's time");
					break;
				}
				else
				{
					time2 = time ;
					//System.out.println("---------------------- Time2 of >>>"+myIndex+"<<< is "+time2);
					list3.add(myIndex);
					list4.add(time2);   // add the exit time to the list of times for all vehicles exiting
					//System.out.print("size of list3 : " + list3.size());
					
					
					// get index in list1 of myIndex
					int temporary  = list1.indexOf(myIndex); // entering cars
					
					// get initial time of arrival for the myIndex 
					Object initial_time =  (list2.get(temporary)) ;
					String tempo = initial_time.toString();
					init_time  =  Double.valueOf(tempo.trim()).doubleValue();  // the time1 value of myIndex

					// Calculate the time spent in the system
					time_spent = time2 - init_time; // current time - initial time of entry 
					 
					// Count the number of exiting vehicles
					exit_counter++;

					// to later get the avg
					total_time_spent += time_spent;
					

					//  Now REMOVE index from List in the arrays (or maybe we could keep it for results - might make program slow, will decide later)
					list1.remove(temporary);
					list2.remove(temporary);
				}
				
				
			}
			
			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
			
	

		} // end of for loop
	
		// Get time spent in system (AVG)
		average_time_spent = total_time_spent / exit_counter;  // avg time spent by agents in system
		 
		//System.out.println("---------------------- Number of agents that exited: "+exit__counter);
						
		if (average_time_spent > 0)
		{
			System.out.println("---------------------- AVG TIME SPENT: "+average_time_spent/60);
		}
	}



	
	
	
	
    // draw lane-separating road lines (Modified to fit 4 lanes)
    private void drawLines(Graphics g)
	{
   
       g.setColor(Color.white);   
       int lineLength = (int)(scale*LINELENGTH_M);
       int gapLength = (int)(scale*GAPLENGTH_M);
       int lineWidth = 1;
       // nuber of lines: determ with rdlen/(lineLength+..) 
       // would imply large discr. error!!
       int numberOfLines = (int)(rdlen/(scale*(LINELENGTH_M+GAPLENGTH_M)));
       //System.out.println("rdlen="+rdlen+" scale="+scale+
       //    " numberOfLines="+numberOfLines);
   
		if(choice_Geom==0)
		{
			for (int i=0; i<numberOfLines; i++)
			{
				double pos_m = i*(LINELENGTH_M+GAPLENGTH_M);
				PolyVehCircle roadLine = new PolyVehCircle(pos_m, -1, lineLength, lineWidth, r1, r2, xc, yc);
				g.fillPolygon(roadLine.xPoints, roadLine.yPoints, 4);
			}
		}

       if (choice_Geom==1){
         for (int i=0; i<numberOfLines; i++){
           double pos_m = i*(LINELENGTH_M+GAPLENGTH_M);
           PolyVehU roadLine = new PolyVehU (pos_m, -1, lineLength, lineWidth, r1, r2, xc, l_straight, yc, (int)(rdlen));         // dotted line 1
		   PolyVehU roadLine2 = new PolyVehU (pos_m, -1, lineLength, lineWidth, r3-2, r4-2, xc, l_straight, yc, (int)(rdlen));    // dotted line 2
		   PolyVehU roadLine3 = new PolyVehU (pos_m, -1, lineLength, lineWidth, r3+6, r4+6, xc, l_straight, yc, (int)(rdlen));    // dotted line 3
	       g.fillPolygon(roadLine.xPoints, roadLine.yPoints, 4);     // fill dotted line 1
		   g.fillPolygon(roadLine2.xPoints, roadLine2.yPoints, 4);   // fill dotted line 2
		   g.fillPolygon(roadLine3.xPoints, roadLine3.yPoints, 4);   // fill dotted line 3
	 }
         if (choice_Szen==2){
           numberOfLines = (int)(L_RAMP_M/(LINELENGTH_M+GAPLENGTH_M) + 1);
           for (int i=0; i<numberOfLines; i++){
             int xstart = xc + (int)(i * scale * (LINELENGTH_M+GAPLENGTH_M));
	     // drawPolygon draws centered lines -> must add half linewidth
	     // to height,width parameters of drawRect
             int yTopOn = yc+r2-(int)((lineWidth+1)/2);
             g.fillRect(xstart, yTopOn, lineLength+1, lineWidth);
	   }
	 }

 

       }


       if (choice_Geom==2){  // STRAIGHT_ROAD=true
         for (int i=0; i<numberOfLines; i++){
           double pos_m = i*(LINELENGTH_M+GAPLENGTH_M);
           PolyVehStraight roadLine = new PolyVehStraight(pos_m, -1, lineLength, lineWidth,  (int)(rdlen));
	       g.fillPolygon(roadLine.xPoints, roadLine.yPoints, 4);
	 
		 }
         
		   if (choice_Szen==2){
           numberOfLines = (int)(L_RAMP_M/(LINELENGTH_M+GAPLENGTH_M) + 1);
           for (int i=0; i<numberOfLines; i++){
             int xLeft =ramppos 
               + (int)(scale*( - 0.5*L_RAMP_M + i*(LINELENGTH_M+GAPLENGTH_M)));
	     // drawPolygon draws centered lines -> must add half linewidth
	     // to height,width parameters of drawRect
             int yTopOn = ystart-(int)((lineWidth+1)/2);
             //System.out.println("SimCanvas.paintLines:"
	     //			+ "xLeft="+xLeft);
             g.fillRect(xLeft, yTopOn, lineLength+1, lineWidth+1);
	   }
	 }
       }

    }



    public void newValues (int choice_Szen, 
                           double density, double qIn, double perTr, 
                           double v0_limit, double p_factor, double deltaB,  
                           int tsleep_ms, double CACC_rate) {


	this.choice_Szen=choice_Szen;
	this.density = density;
	this.qIn = qIn;
	this.perTr = perTr;
	this.v0_limit = v0_limit;
	this.p_factor = p_factor;
	this.deltaB = deltaB;
	this.tsleep_ms = tsleep_ms;
	this.CACC_rate = CACC_rate;

 
	choice_Geom=((choice_Szen==1)||(choice_Szen==6)) ? 0 : 1;
        if((STRAIGHT_ROAD)&&(choice_Szen==2)){choice_Geom=2;}
	    setScales(); // NOT after update of rdlen, but behind choice_Szen
        rdlen_m = (choice_Geom==0) ? 2 * Math.PI *RADIUS_M : Math.PI *RADIUS_M + 2*l_straight_m;

        System.out.println("SimCanvas.newValues():"
                 +" STRAIGHT_ROAD="+STRAIGHT_ROAD
                 +" choice_Szen= "+ choice_Szen
                 +" choice_Geom= "+ choice_Geom
                   );


    }

    public void newValues2 (double qIn, double perTr,
                     double p_factor, double deltaB, 
                     double qRamp, double p_factorRamp,
                     double deltaBRamp, int tsleep_ms, double CACC_rate) {


	this.choice_Szen=2;
	setScales(); // NOT after update of rdlen, but behind choice_Szen
	this.qIn = qIn;
	this.perTr = perTr;
	this.p_factor = p_factor;
	this.qRamp = qRamp;
	this.p_factorRamp = p_factorRamp;

	//New 
	this.CACC_rate = CACC_rate;

	this.deltaB = deltaB;
	this.deltaBRamp = deltaBRamp;
	this.tsleep_ms = tsleep_ms;
	choice_Geom= (STRAIGHT_ROAD) ? 2 : 1;
        rdlen_m = (choice_Geom==2)
	    ? STRAIGHT_RDLEN_M
            : Math.PI *RADIUS_M + 2*l_straight_m;
        System.out.println("SimCanvas.newValues2():"
                 +" STRAIGHT_ROAD="+STRAIGHT_ROAD
                 +" choice_Szen= "+ choice_Szen
                 +" choice_Geom= "+ choice_Geom
                   );
    }



    // paint "Car" and "Truck" symbols and labels
    private void paintLegend(Graphics g){

      double xRelRight=0.99; // right end of the area for the legend
      double yRelSpaceBot=0.12; // space between U road and bottom of area
      if (choice_Szen==5){yRelSpaceBot=0.06;}
	// boolean withProbeCar=((choice_Szen==5)||(choice_Szen==1));
      boolean withProbeCar=false;
      String carString = (GERMAN) ? "PKW" : "Car";
      String truckString = (GERMAN) ? "LKW" : "Truck";
      String cacc_carString = (GERMAN) ? "PKWCACC" : "CACC";
      String idString = (GERMAN) ? "Gleicher PKW-Typ" : "Same Car";
      String probeString = (GERMAN) ? "Testfahrzeug" : "Probe Car";

      int lineHeight=(int)(1.25*metricsText.stringWidth("M"));
	  int lineHeight2=(int)(1.25*metricsText.stringWidth("M") * 2);
      
	  int xoff=(withProbeCar)
                ? metricsText.stringWidth(probeString)+pkwLen
                : metricsText.stringWidth(truckString)+pkwLen;
      if (choice_Szen==6){
          xoff=metricsText.stringWidth(idString)+pkwLen;}

      int x0=(int)(xRelRight*xsize)-xoff-lkwLen;  // xsize,ysize in pixels      
      int nUp=(withProbeCar) ? 2 : 1;
      int y0=yc+r1-(int)(yRelSpaceBot*ysize)-nUp*lineHeight;
      //System.out.println("y0="+y0);
      if (choice_Szen==6){y0+=lineHeight;}
      //if(choice_Geom==2){ y0=ystart - 4*lineHeight;}

      int x1 =(int)(xRelRight*xsize)-xoff-lkwLen;  // xsize,ysize in pixels      
	  int y1 = yc+r1-(int)(yRelSpaceBot*ysize)-nUp*lineHeight2; 


      // text "Cars" or "Trucks"

      g.setFont(textFont);
      g.setColor(Color.black);

      g.drawString(carString,x0,y0);
	  g.drawString(cacc_carString,x1,y1);
      g.drawString( (choice_Szen==6)?idString:truckString, x0,y0+lineHeight);
      if (withProbeCar){
         g.drawString(probeString,x0, y0+2*lineHeight);
	 }

      // vehicle symbols

      g.setColor(Color.red);
      g.fillRect    (x0+xoff, y0       -vehWidth, pkwLen, vehWidth);
		
	  g.setColor(Color.blue);
	  g.fillRect    (x0+xoff, y1       -vehWidth, pkwLen, vehWidth);
	  
      if (choice_Szen==6){
          g.setColor(Color.green);
          g.fillRect    (x0+xoff, y0+lineHeight -vehWidth, pkwLen, vehWidth);}
      else{
          g.setColor(Color.black);
          g.fillRect    (x0+xoff, y0+lineHeight  -vehWidth, lkwLen, vehWidth);}
      if (withProbeCar){
          g.setColor(Color.blue);
          g.fillRect(x0+xoff, y0+2*lineHeight-vehWidth, pkwLen, vehWidth);
      }
    }


    private void paintTimes(Graphics g){

      int min, sec;
      int x0=10;      // start position of time display (left upper corner)
      int y0=20; 
	  int xx = 600;
	  int yy = 200;

      min=(int)(time/60.0);
      sec=(int)(time-min*60.0);
      String str_time = (GERMAN) ? "Simulierte Zeit  " : "Time  "; 
      String timeString = str_time
           + (new Integer(min)).toString()
           + ((sec>9) ? ":" : ":0")   + (new Integer(sec)).toString();
     
		
		// Simuation Timer (Every minute, show me the flow rate) - Georges Arnaout
		if(sec == 59)
		{
			// get counter - later
		}



	   // Simuation Timer (Stops when simulation run threshold 30 min is reached) - Georges Arnaout
		if(min == 91)
		{
		 stop();
	     // start(); try to reset for another replication - not implemented yet - throws an IndexOutof... 
		}
	
	  String myTime =    (new Integer(min)).toString() + ((sec>9) ? "/" : "/0")   + (new Integer(sec)).toString();
	  
	  int widthTime = metricsText.stringWidth (str_time);
      int widthDisplay = metricsText.stringWidth (timeString);

	  // display car counter
	  Vector positions=microstreet.positions;
	  int imax   = positions.size()-1;


	  String carCounter = "Number of Cars in the system: " + imax;
	  g.drawString(carCounter,xx,yy);

      // clear old time
      g.setColor(backgrColor);
      // g.setColor(Color.white);
      g.fillRect(x0+widthTime,y0-textHeight,widthDisplay-widthTime,textHeight);

      // display new time

      g.setFont(textFont);
      g.setColor(Color.black);
      g.drawString(timeString,x0,y0);
	  System.out.println("---------------------- SIMULATION TIME: ----------------"+ myTime);
	  
	 
	  

      // draw "Countdown <m>:<ss>" at right bottom pos

      if (choice_Szen==5){
        if (time<T_GREEN){
          min=(int)((T_GREEN-time)/60.0);
          sec=(int)((T_GREEN-time)-min*60.0);
          String countdString = "Countdown  "   + (new Integer(min)).toString()+ ((sec>9) ? ":" : ":0") + (new Integer(sec)).toString();

          widthTime    = metricsText.stringWidth ("Countdown");
          widthDisplay = metricsText.stringWidth (countdString);
          x0           = l_straight+xc-5-widthDisplay;
          y0           = yc+r2-lw-20;

          //g.setColor(backgrColor);
          g.setColor(Color.white);
          g.fillRect(x0+widthTime,y0-textHeight,
                     widthDisplay-widthTime,textHeight);
          g.setColor(Color.black);
          g.drawString(countdString,x0,y0);
	}
      }
    }


    private void paintSymbols(Graphics g){

      // traffic light
      // t<T_GREEN: red; otherwise green

      if (choice_Szen==5){
          double det1pos =RELPOS_TL * rdlen_m;
          int detx=(new DetPosU(det1pos)).x ;
          int dety=(new DetPosU(det1pos)).y - lw;
          int heightTL = (int)(radius*0.16);   // total height of T.L.
          int radiusTL = (int)(radius*0.024);   // radius of lights
          g.fillRect(detx,dety-heightTL,2,heightTL);    // pole
          g.fillRect(detx-radiusTL,dety-heightTL,
                       2*radiusTL,4*radiusTL);

          if (time<T_GREEN){
            g.setColor(Color.red);
            g.fillOval(detx-radiusTL,dety-heightTL,
                       2*radiusTL,2*radiusTL);
          }
          else{
            g.setColor(Color.green);
            g.fillOval(detx-radiusTL,dety-heightTL+2*radiusTL,
                       2*radiusTL,2*radiusTL);
          }
      }

      // paint the "virtual traffic light" controlling too much inflow

      if ((choice_Szen==5)||(choice_Szen==3)||(choice_Szen==4)){

          if (microstreet.red==true){
            g.setColor(Color.black);
            int bw=(int)(radius*0.1);
            g.fillRect((l_straight+xc-5-bw),yc-r1+5,bw,bw);
            g.setColor(Color.red);
            g.fillOval((l_straight+xc-5-bw),yc-r1+5,bw,bw);
          }
          else{
            g.setColor(Color.black);
            int bw=(int)(radius*0.1);
            g.fillRect((l_straight+xc-5-bw),yc-r1+5,bw,bw);
            g.setColor(Color.green);
            g.fillOval((l_straight+xc-5-bw),yc-r1+5,bw,bw);            
          } 
      }




      // paint Uphill traffic Sign (US)
      // for the flow-conserving bottleneck Scen.

      if (choice_Szen==4){
          double detpos =l_straight_m + 0.5 * Math.PI * RADIUS_M;

	  // positin of pole
          int heightTot = (int)(radius*0.24);   // total height of U.S.
          int heightSign = (int)(radius*0.16);   // 
          int x0=(new DetPosU(detpos)).x + (int)(0.2*radius);
          int y0=(new DetPosU(detpos)).y + heightTot;
          int xc        = x0;
          int yc        = y0-heightTot + (int)(0.5*heightSign);
          double relWidthRed = 0.2;   // 


	  // draw pole
          g.setColor(Color.black);
          g.fillRect(x0,y0-heightTot,2,heightTot); 

	  // draw triangular sign
          int hbasis = (int)(0.577*heightSign);   // tan(30 deg)
          int vbasis = (int)(0.5*heightSign);  
          int[] ptsx = {xc,xc-hbasis,xc+hbasis};
          int[] ptsy = {yc-vbasis, yc+vbasis, yc+vbasis};
          g.setColor(Color.red);
          g.fillPolygon(ptsx,ptsy,ptsx.length);

          yc+=(int)(0.3*relWidthRed*vbasis);
          hbasis = (int)((1-relWidthRed)*hbasis);
          vbasis = (int)((1-relWidthRed)*vbasis);
          int[] ptsx1 = {xc,xc-hbasis,xc+hbasis};
          int[] ptsy1 = {yc-vbasis, yc+vbasis, yc+vbasis};
          g.setColor(Color.white);
          g.fillPolygon(ptsx1,ptsy1,ptsx1.length);

	  // draw gradient
          ptsx1[0] = (int)(0.5*(ptsx[0]+ptsx[2]));
          ptsy1[0] = (int)(0.5*(ptsy[0]+ptsy[2]));

          g.setColor(Color.black);
          g.fillPolygon(ptsx1,ptsy1,ptsx1.length);

	  // text

          y0+=heightSign;
          g.drawString("Begin",x0,y0);
          y0 += (int)( 0.07*radius);
          g.drawString("uphill",x0,y0);

      }


      // paint Optional text "Engstelle" and crop marks

      if (false){
           int x0=(int)(0.25*xsize);
           int y0=(int)(0.82*ysize);
           if (choice_Szen==3){  // choice_Szen==3: lane closing
               g.setFont(textFont);
                  g.setColor(Color.black);
                g.drawString("Engstelle",x0, y0);
           }
   
           int x1mark = (int)(0.10*xsize);
           int y1mark = (int)(0.50*ysize);
           int x2mark = (int)(0.70*xsize);
           int y2mark = (int)(0.90*ysize);
           int markLen = (int)(0.05*xsize);
   
           if (choice_Szen==3){  // choice_Szen==3: lane closing
             //            g.setFont(textFont);
              g.setColor(Color.black);
               g.drawLine(x1mark,y1mark,x1mark+markLen,y1mark);
               g.drawLine(x2mark,y2mark,x2mark,y2mark-markLen);
           }
      }
    }

    private void imposeSpeedLimit(){
       v0_limit_old = v0_limit;

       double v0trucks = (v0_limit<VMAX_TRUCK_KMH/3.6)
          ? v0_limit
          : VMAX_TRUCK_KMH/3.6;

       if ((choice_Szen==3)||(choice_Szen==5)){
          ( (IDM)(microstreet.idmCar)).set_v0(v0_limit);
          ( (IDM)(microstreet.idmCar)).initialize();
          // comment following 2 lines if trucks are "free"
          ( (IDM)(microstreet.idmTruck)).set_v0(v0trucks);
          ( (IDM)(microstreet.idmTruck)).initialize();
       }
       if(choice_Szen==4){
          ( (IDM)(microstreet.sync1Car)).set_v0(v0_limit);
          ( (IDM)(microstreet.sync1Car)).initialize();
          // comment following 2 lines if trucks are "free"
          ( (IDM)(microstreet.sync1Truck)).set_v0(v0trucks);
          ( (IDM)(microstreet.sync1Truck)).initialize();
       }


    }




    // paint the speed-limit sign

    private void paintSpeedLimit(Graphics g){
          int v0_kmh = (int)(3.6*v0_limit);
          int heightSS = (int)(radius*0.24);   // total height of T.L.
          int radiusSS = (int)(radius*0.08);   // radius of lights
          double det1pos =RELPOS_SPEEDSIGN * rdlen_m;
          int x0=(new DetPosU(det1pos)).x ;    // basis of pole
          int y0=(new DetPosU(det1pos)).y + (int)(1.5*lw) + heightSS;
          int xc=x0;                        // center of circular sign
          int yc=y0-heightSS+radiusSS;        // center of circular sign
 
          g.setColor(Color.black);
          g.fillRect(x0,y0-heightSS,2,heightSS);    // pole

          if (v0_kmh>=V0_MAX_KMH){  // no speed limit
            g.setColor(Color.white);
            g.fillOval(xc-radiusSS,yc-radiusSS, 2*radiusSS,2*radiusSS);
            g.setColor(Color.black);
            int space=(int)(0.15*radiusSS);
            for (int i=-2; i<=2; i++){
                g.drawLine(xc-(int)(0.7*radiusSS)+i*space,
                           yc+(int)(0.7*radiusSS)+i*space,
                           xc+(int)(0.7*radiusSS)+i*space,
                           yc-(int)(0.7*radiusSS)+i*space);
	    }
	  }

          else{           // speed limit active
            int rwhite=(int)(0.8*radiusSS);
            String speedLimitStr=String.valueOf(v0_kmh);
            int ref_width=metricsText.stringWidth (speedLimitStr);
            int speedFontHeight = (int)(textHeight*1.4*radiusSS/ref_width);
            Font speedFont=new Font("SansSerif",Font.BOLD,speedFontHeight);
            int xoff=(int)(0.65*radiusSS);
            int yoff=(int)(0.4*speedFontHeight);

            g.setColor(Color.red);
            g.fillOval(xc-radiusSS,yc-radiusSS, 2*radiusSS,2*radiusSS);
            g.setColor(Color.white);
            g.fillOval(xc-rwhite,yc-rwhite, 2*rwhite,2*rwhite);
            g.setColor(Color.black);
            g.setFont(speedFont);
            g.drawString(String.valueOf(v0_kmh), xc-xoff, yc+yoff);
            g.setFont(textFont);
          }
    }



    // lane=0: left/inner
    // lane=1: right/outer
    // lane=-1: polygon for road lines
    // lane double-valued for continuous transition!

    class PolyVehCircle{  
    
      public  int xPoints[] = new int[5];
      public  int yPoints[] = new int[5];

      public PolyVehCircle(double pos, double lane, double length, double width, int r1, int r2, int xc, int yc){

          double l    = length*scale;           // length of drawn car
          double w    = width*scale;            // width of drawn car

          double center_pos   = pos + length/2;   // in sim pos=back(!) end
          double arc          = -center_pos/RADIUS_M - 0.25*(Math.PI); 
          double ca           = Math.cos(arc);
          double sa           = ((CLOCKWISE) ? -1 : 1) * Math.sin(arc);
          double r            = (lane>=0) ? r1 + (0.25+0.5*lane) * (r2-r1) : r1 + 0.5 * (r2-r1);
          
	  // pos=7/8 * CIRCUMF_M -> arc=2 pi -> arc=0 -> center at (xc+r,yc)

          double xc_car       = (xc + r*ca); 
          double yc_car       = (yc + r*sa);    // graph y = - math y

          int    x_frontleft  = (int) (xc_car + 0.5*(-w*ca - l*sa));
          int    x_frontright = (int) (xc_car + 0.5*( w*ca - l*sa));
          int    x_backleft   = (int) (xc_car + 0.5*(-w*ca + l*sa));
          int    x_backright  = (int) (xc_car + 0.5*( w*ca + l*sa));

          int    y_frontleft  = (int) (yc_car + 0.5*(-w*sa + l*ca));
          int    y_frontright = (int) (yc_car + 0.5*( w*sa + l*ca));
          int    y_backleft   = (int) (yc_car + 0.5*(-w*sa - l*ca));
          int    y_backright  = (int) (yc_car + 0.5*( w*sa - l*ca));


          xPoints[0] = x_frontright;
          xPoints[1] = x_frontleft;
          xPoints[2] = x_backleft;
          xPoints[3] = x_backright;
               
          yPoints[0] = y_frontright;
          yPoints[1] = y_frontleft;
          yPoints[2] = y_backleft;
          yPoints[3] = y_backright;

	  // Polygon instead explicit arrays: 15 % runtime perf. loss!
	  // vehShape = new Polygon(xPoints, car_yPoints, 4);
      }
    }


    // lane=0: left/inner
    // lane=1: right/outer
    // lane=-1: polygon for road lines
    // lane double-valued for continuous transition!

    class PolyVehU{
    
      public  int xPoints[] = new int[5];
      public  int yPoints[] = new int[5];

      public PolyVehU(double pos, double lane, double length, double width, int r1, int r2, int xc, int l_straight, int yc, int rdlen){

          double l    = length*scale;           // length of drawn car
          double w    = width*scale;            // width of drawn car
          double center_pos   = pos + length/2;   // in sim pos=back (!) end
          int intpos=(int)(center_pos*scale);
          double r            = (lane>=0) ? r1 + (0.25+0.5*lane) * (r2-r1) : r1 + 0.5 * (r2-r1);

          if ((intpos>l_straight)&&(intpos<(rdlen-l_straight))){

            double arc = -0.5*(Math.PI) -(2*(Math.PI)*((double)(intpos-l_straight)))/((double)(2*(rdlen-2*l_straight)));
             
            double ca           = Math.cos(arc);
            double sa           = Math.sin(arc);
            double xc_car       = (xc + r*ca); 
            double yc_car       = (yc + r*sa); 

            int    x_frontleft  = (int) (xc_car + 0.5*(-w*ca - l*sa));
            int    x_frontright = (int) (xc_car + 0.5*( w*ca - l*sa));
            int    x_backleft   = (int) (xc_car + 0.5*(-w*ca + l*sa));
            int    x_backright  = (int) (xc_car + 0.5*( w*ca + l*sa));

            int    y_frontleft  = (int) (yc_car + 0.5*(-w*sa + l*ca));
            int    y_frontright = (int) (yc_car + 0.5*( w*sa + l*ca));
            int    y_backleft   = (int) (yc_car + 0.5*(-w*sa - l*ca));
            int    y_backright  = (int) (yc_car + 0.5*( w*sa - l*ca));
          
            xPoints[0] = x_frontright;
            xPoints[1] = x_frontleft;
            xPoints[2] = x_backleft;
            xPoints[3] = x_backright;
            xPoints[4] = x_frontright;
               
            yPoints[0] = y_frontright;
            yPoints[1] = y_frontleft;
            yPoints[2] = y_backleft;
            yPoints[3] = y_backright;
            yPoints[4] = y_frontright;
          }
          if (intpos<=l_straight){

            double ca           = 0.0;
            double sa           = 1.0;

            double xc_car       = xc+l_straight-intpos; 
            double yc_car       =  yc - r; 

            int    x_frontleft  = (int) (xc_car + 0.5*(-w*ca - l*sa));
            int    x_frontright = (int) (xc_car + 0.5*( w*ca - l*sa));
            int    x_backleft   = (int) (xc_car + 0.5*(-w*ca + l*sa));
            int    x_backright  = (int) (xc_car + 0.5*( w*ca + l*sa));

            int    y_frontleft  = (int) (yc_car + 0.5*(-w*sa + l*ca));
            int    y_frontright = (int) (yc_car + 0.5*( w*sa + l*ca));
            int    y_backleft   = (int) (yc_car + 0.5*(-w*sa - l*ca));
            int    y_backright  = (int) (yc_car + 0.5*( w*sa - l*ca));

            xPoints[0] = x_frontright;
            xPoints[1] = x_frontleft;
            xPoints[2] = x_backleft;
            xPoints[3] = x_backright;
            xPoints[4] = x_frontright;
               
            yPoints[0] = y_frontright;
            yPoints[1] = y_frontleft;
            yPoints[2] = y_backleft;
            yPoints[3] = y_backright;
            yPoints[4] = y_frontright;
          }

		  if (intpos>(rdlen-l_straight))
		  {

			  double ca           = 0.0;
			  double sa           = -1.0;
            
			  double xc_car       = xc+l_straight+intpos-rdlen; 
			  double yc_car       =  yc+r; 

			  int    x_frontleft  = (int) (xc_car + 0.5*(-w*ca - l*sa));
			  int    x_frontright = (int) (xc_car + 0.5*( w*ca - l*sa));
			  int    x_backleft   = (int) (xc_car + 0.5*(-w*ca + l*sa));
			  int    x_backright  = (int) (xc_car + 0.5*( w*ca + l*sa));

			  int    y_frontleft  = (int) (yc_car + 0.5*(-w*sa + l*ca));
			  int    y_frontright = (int) (yc_car + 0.5*( w*sa + l*ca));
			  int    y_backleft   = (int) (yc_car + 0.5*(-w*sa - l*ca));
			  int    y_backright  = (int) (yc_car + 0.5*( w*sa - l*ca));

			  xPoints[0] = x_frontright;
			  xPoints[1] = x_frontleft;
			  xPoints[2] = x_backleft;
			  xPoints[3] = x_backright;
			  xPoints[4] = x_frontright;
               
			  yPoints[0] = y_frontright;
			  yPoints[1] = y_frontleft;
			  yPoints[2] = y_backleft;
			  yPoints[3] = y_backright;
			  yPoints[4] = y_frontright;
		  }
      }
    }

    // lane=0: left/inner
    // lane=1: right/outer
    // lane=-1: polygon for road lines
    // lane double-valued for continuous transition!
    // pos = vehicle pos in m!
    // length,width: vehicle dimensions in m!
    // xstart, ystart: left beginning road (pixel units!)
    // rdlen: road length (pixel units!)

    class PolyVehStraight{
    
      public  int xPoints[] = new int[5];
      public  int yPoints[] = new int[5];

      public PolyVehStraight(double pos_m, double lane, double vehLen_m, double vehWidth_m, int rdlen){

          double l    = vehLen_m*scale;           // length of drawn car
          double w    = vehWidth_m*scale;         // width of drawn car
          int    xveh =xstart + (int)((pos_m+ vehLen_m/2)*scale);
          double yveh = (lane>=0) ? ystart-(1.5-lane)*lw : ystart-lw;

          if (true){


            int    x_frontleft  = (int) (xveh - 0.5*l);
            int    x_frontright = (int) (xveh - 0.5*l);
            int    x_backleft   = (int) (xveh + 0.5*l);
            int    x_backright  = (int) (xveh + 0.5*l);

            int    y_frontleft  = (int) (yveh + 0.5*w);
            int    y_frontright = (int) (yveh - 0.5*w);
            int    y_backleft   = (int) (yveh + 0.5*w);
            int    y_backright  = (int) (yveh - 0.5*w);

            xPoints[0] = x_frontright;
            xPoints[1] = x_frontleft;
            xPoints[2] = x_backleft;
            xPoints[3] = x_backright;
            xPoints[4] = x_frontright;
               
            yPoints[0] = y_frontright;
            yPoints[1] = y_frontleft;
            yPoints[2] = y_backleft;
            yPoints[3] = y_backright;
            yPoints[4] = y_frontright;
          }
      }  // end constructor
    } // end PolyVehStraight


    class PolyVehStraightRamp{
    
      public  int xPoints[] = new int[5];
      public  int yPoints[] = new int[5];

      public PolyVehStraightRamp(double pos_m,
                  double vehLen_m, double vehWidth_m, int rdlen){

          double l    = vehLen_m*scale;              // length of drawn car
          double w    = vehWidth_m*scale;            // width of drawn car
          double sa = Math.sin(ANGLE_ACCESS);
          double ca = Math.cos(ANGLE_ACCESS);
          if (pos_m>(ramppos_m-0.5*L_RAMP_M)){  // actual merging section
            int xveh =xstart + (int)((pos_m+ vehLen_m/2)*scale); 
            double yveh = ystart+0.5*lw;


            int    x_frontleft  = (int) (xveh + 0.5*l);
            int    x_frontright = (int) (xveh + 0.5*l);
            int    x_backleft   = (int) (xveh - 0.5*l);
            int    x_backright  = (int) (xveh - 0.5*l);

            int    y_frontleft  = (int) (yveh - 0.5*w);
            int    y_frontright = (int) (yveh + 0.5*w);
            int    y_backleft   = (int) (yveh - 0.5*w);
            int    y_backright  = (int) (yveh + 0.5*w);

            xPoints[0] = x_frontright;
            xPoints[1] = x_frontleft;
            xPoints[2] = x_backleft;
            xPoints[3] = x_backright;
            xPoints[4] = x_frontright;
               
            yPoints[0] = y_frontright;
            yPoints[1] = y_frontleft;
            yPoints[2] = y_backleft;
            yPoints[3] = y_backright;
            yPoints[4] = y_frontright;
          }
          else{  // access section, pos < ramppos_m-0.5*L_RAMP_M
	    double xs_m = (ramppos_m-0.5*L_RAMP_M); // start merging
	    double ys_m = ystart/scale; // start merging
            double xveh =xstart +scale*(xs_m - ca*(xs_m-pos_m- 0.5*vehLen_m));
            double yveh =0.5*lw+scale*(ys_m + sa*(xs_m-pos_m- 0.5*vehLen_m));


            int    x_frontleft  = (int) (xveh + 0.5*( ca*l - sa*w));
            int    x_frontright = (int) (xveh + 0.5*( ca*l + sa*w));
            int    x_backleft   = (int) (xveh + 0.5*(-ca*l - sa*w));
            int    x_backright  = (int) (xveh + 0.5*(-ca*l + sa*w));

            int    y_frontleft  = (int) (yveh + 0.5*(-sa*l - ca*w));
            int    y_frontright = (int) (yveh + 0.5*(-sa*l + ca*w));
            int    y_backleft   = (int) (yveh + 0.5*( sa*l - ca*w));
            int    y_backright  = (int) (yveh + 0.5*( sa*l + ca*w));

            xPoints[0] = x_frontright;
            xPoints[1] = x_frontleft;
            xPoints[2] = x_backleft;
            xPoints[3] = x_backright;
            xPoints[4] = x_frontright;
               
            yPoints[0] = y_frontright;
            yPoints[1] = y_frontleft;
            yPoints[2] = y_backleft;
            yPoints[3] = y_backright;
            yPoints[4] = y_frontright;
          }

      }  // end constructor
    } // end PolyVehStraightRamp


    class DetPosU{  // positioning of road signs etc as function of position
      public int x;
      public int y;

      public DetPosU(double pos){
          int intpos=(int)(pos*scale);

          if ((intpos>l_straight)&&(intpos<(rdlen-l_straight))){

            double arc = -0.5*(Math.PI)
                -(2*(Math.PI)*((double)(intpos-l_straight)))/
                ((double)(2*(rdlen-2*l_straight)));
             
            double ca           = Math.cos(arc);
            double sa           = Math.sin(arc);

 
            x=(int)(xc + radius*ca);
            y=(int)(yc + radius*sa); 
          }
          if (intpos<=l_straight){
            x = (int) xc+l_straight-intpos; 
            y = (int) yc - radius; 
          }
          if (intpos>(rdlen-l_straight)){
      
            x = (int) (xc+l_straight+intpos-rdlen); 
            y = (int) (yc+radius);
          }
      }
    }


    class DetPosS{  // positioning of road signs etc as function of position
	            // Straight roads
      public int x;
      public int y;

      public DetPosS(double pos_m, int yc){
            x = (int) (int)(pos_m*scale); 
            y = yc;
      }
    } // end DetPosS


    class DetPosC{
      public int x;
      public int y;

      public DetPosC(double pos_m){

          double arc          = -pos_m/RADIUS_M-0.25*(Math.PI); 
          double ca           = Math.cos(arc);
          double sa           = Math.sin(arc);

          x = (int)(xc + radius*ca);   // radius in pixels
          y = (int)(yc + radius*sa); 
      }
      public DetPosC(double pos_m, double rightOffset_m){
          double rightOffset  = scale *  rightOffset_m;
          double arc          = -pos_m/RADIUS_M-0.25*(Math.PI); 
          double ca           = Math.cos(arc);
          double sa           = Math.sin(arc);

          x = (int)(xc + (radius+rightOffset)*ca); 
          y = (int)(yc + (radius+rightOffset)*sa); 
      }
    }


    private void setInsetDiagrams(){
        final double CIRCUMF_M = 2.* Math.PI * RADIUS_M;

	if (choice_Szen==1){
	    det1_0=new TimeAvDet(0.125*CIRCUMF_M, 30.0, 0);
	    det1_1=new TimeAvDet(0.125*CIRCUMF_M, 30.0, 1);
	    det2_0=new SpaceAvDet(0.125*CIRCUMF_M, 50.0, 0);
	    det2_1=new SpaceAvDet(0.125*CIRCUMF_M, 50.0, 1);
	    det3_0=new TimeAvDet(0.375*CIRCUMF_M, 30.0, 0);
	    det3_1=new TimeAvDet(0.375*CIRCUMF_M, 30.0, 1);
	    det4_0=new SpaceAvDet(0.375*CIRCUMF_M, 50.0, 0);
	    det4_1=new SpaceAvDet(0.375*CIRCUMF_M, 50.0, 1);
	    det5_0=new TimeAvDet(0.625*CIRCUMF_M, 30.0, 0);
	    det5_1=new TimeAvDet(0.625*CIRCUMF_M, 30.0, 1);
	    det6_0=new SpaceAvDet(0.625*CIRCUMF_M, 50.0, 0);
	    det6_1=new SpaceAvDet(0.625*CIRCUMF_M, 50.0, 1);
	    det7_0=new TimeAvDet(0.875*CIRCUMF_M, 30.0, 0);
	    det7_1=new TimeAvDet(0.875*CIRCUMF_M, 30.0, 1);
	    det8_0=new SpaceAvDet(0.875*CIRCUMF_M, 50.0, 0);
	    det8_1=new SpaceAvDet(0.875*CIRCUMF_M, 50.0, 1);

	    diagram1 = createImage(diaWidth,diaHeight);	
	    diagram2 = createImage(diaWidth,diaHeight);
	    diagram3 = createImage(diaWidth,diaHeight);
	    diagram4 = createImage(diaWidth,diaHeight);

	    fcdxdv=new StatBufDiag(-10.0, 10.0, 0.0, 100.0,
                                   diaWidth, diaHeight, 400,
				   Color.white, Color.black, diagram1,
				   "Velocity Difference", "Headway", "",
				   "Blue Car",5.0,"5 m/s",50.0,"50 m");
	    String[] ylabels={"acc","V"};
	    Color[] colors={Color.black,Color.red};
	    double[] ytics={4.0,2.86};
	    String[] yticLabels={"4 m/s^2","20 m/s"};
	    fcvt=new DynBufDiag(0.0, 60.0, -5.0, 5.0, diaWidth, diaHeight,
		    60.0,Color.white, Color.black, diagram2, "time", ylabels,
				colors ,"","Acceleration+Velocity",
				30.0,"-30s",ytics,yticLabels);
	    Qrho=new StatBufDiag(0.0, 0.15, 0.0, 0.8, diaWidth,diaHeight,
			       2000, Color.white, Color.black, diagram3, 
                                 "rho", "Q",
				 "Detectors D1-D4","Data from",
				 0.075,"75 Veh/km",0.417,"1500 Veh/h");
	    QrhoS=new StatBufDiag(0.0, 0.15, 0.0, 0.8, diaWidth,diaHeight,
			       1000, Color.white, Color.black, diagram4, 
                                  "rho", "Q",
				  "Detectors D1-D4","Actual Data",
				  0.075,"75 Veh/km",0.417,"1500 Veh/h");

	}
	if (choice_Szen==5){

	    det1_0=new TimeAvDet(0.8*CIRCUMF_M, 30.0, 0);
	    det1_1=new TimeAvDet(0.8*CIRCUMF_M, 30.0, 1);	    

	    diagram1 = createImage(diaWidth,diaHeight);	
	    diagram2 = createImage(diaWidth,diaHeight);
	    
	    String[] ylabels={"acc","V"};
	    Color[] colors={Color.black,Color.red};
	    double[] ytics={4.0,2.86};
	    String[] yticLabels={"4 m/s^2","20 m/s"};
	    fcvt=new DynBufDiag(0.0, 120.0, -5.0, 5.0, diaWidth, diaHeight,
				120.0,Color.white, Color.black, diagram1, 
                                 "time",
				ylabels, colors,"Blue Car",
				"Acceleration+Velocity",
				60.0,"-60s",ytics,yticLabels);
	    String[] ylabels1={"Q"};
	    Color[] colors1={Color.black};
	    double[] ytics1={0.417};
	    String[] yticLabels1={"1500 Veh/h"};

	    Qt = new DynBufDiag(0.0, 60.0, 0.0, 0.8, diaWidth, diaHeight,
		     	60.0,Color.white, Color.black, diagram2, "time",
		     	ylabels1, colors1,"","Total Flow at D1",
		      	30.0,"-30s",ytics1,yticLabels1);
	}
	

            // <Treiber aug06>
                if ((choice_Szen==2)||(choice_Szen==3)||(choice_Szen==4)){
            //</Treiber>

	//if ((choice_Szen==3)||(choice_Szen==4)){
	    det1_0=new TimeAvDet(0.9*CIRCUMF_M, 30.0, 0);
	    det1_1=new TimeAvDet(0.9*CIRCUMF_M, 30.0, 1);
	    det2_0=new SpaceAvDet(0.9*CIRCUMF_M, 50.0, 0);
	    det2_1=new SpaceAvDet(0.9*CIRCUMF_M, 50.0, 1);
	    if (choice_Szen==3){
		det3_0=new TimeAvDet(0.6*CIRCUMF_M, 30.0, 0);
		det3_1=new TimeAvDet(0.6*CIRCUMF_M, 30.0, 1);
		det4_0=new SpaceAvDet(0.6*CIRCUMF_M, 50.0, 0);
		det4_1=new SpaceAvDet(0.6*CIRCUMF_M, 50.0, 1);
	    }
	    else{
		det3_0=new TimeAvDet(0.4*CIRCUMF_M, 30.0, 0);
		det3_1=new TimeAvDet(0.4*CIRCUMF_M, 30.0, 1);
		det4_0=new SpaceAvDet(0.4*CIRCUMF_M, 50.0, 0);
		det4_1=new SpaceAvDet(0.4*CIRCUMF_M, 50.0, 1);
	    }
	    diagram1 = createImage(diaWidth,diaHeight);	
	    diagram2 = createImage(diaWidth,diaHeight);
	    diagram3 = createImage(diaWidth,diaHeight);
	    diagram4 = createImage(diaWidth,diaHeight);


	    Qrho=new StatBufDiag(0.0, 0.15, 0.0, 0.8, diaWidth,diaHeight,500,
				Color.white, Color.black, diagram1, "rho", "Q",
				"Detector at D2","Measured Data",
				0.075,"75 Veh/km",0.417,"1500 Veh/h");
	    QrhoS=new StatBufDiag(0.0, 0.15, 0.0, 0.8, diaWidth, diaHeight,100,
			        Color.white, Color.black, diagram2, "rho", "Q",
			        "Detector at D2","Actual Data",
			        0.075,"75 Veh/km",0.417,"1500 Veh/h");
	    Qrho1=new StatBufDiag(0.0, 0.15, 0.0, 0.8, diaWidth,diaHeight,500,
			        Color.white, Color.black, diagram3, "rho", "Q",
			        "Detector at D1","Measured Data",
			        0.075,"75 Veh/km",0.417,"1500 Veh/h");	  
	    QrhoS1=new StatBufDiag(0.0, 0.15, 0.0, 0.8, diaWidth,diaHeight,100,
			       Color.white, Color.black, diagram4, "rho", "Q",
			      "Detector at D1","Actual Data",
			       0.075,"75 Veh/km",0.417,"1500 Veh/h");
	}
    }


    private void updateInsetDiagrams(){
       if (choice_Szen==1){
            det1_0.update(microstreet.positions, microstreet.old_pos, 
                        microstreet.velocities, microstreet.lanes, time);
            det1_1.update(microstreet.positions, microstreet.old_pos, 
                        microstreet.velocities, microstreet.lanes, time);
            det2_0.update(microstreet.positions, microstreet.distances,
                        microstreet.velocities,microstreet.lanes);
            det2_1.update(microstreet.positions, microstreet.distances,
                        microstreet.velocities,microstreet.lanes);
            det3_0.update(microstreet.positions, microstreet.old_pos, 
                        microstreet.velocities, microstreet.lanes ,time);
            det3_1.update(microstreet.positions, microstreet.old_pos, 
                        microstreet.velocities, microstreet.lanes ,time);
            det4_0.update(microstreet.positions, microstreet.distances,
                        microstreet.velocities, microstreet.lanes);
            det4_1.update(microstreet.positions, microstreet.distances,
                        microstreet.velocities, microstreet.lanes);
            det5_0.update(microstreet.positions, microstreet.old_pos, 
                        microstreet.velocities, microstreet.lanes, time);
            det5_1.update(microstreet.positions, microstreet.old_pos, 
                        microstreet.velocities, microstreet.lanes, time);
            det6_0.update(microstreet.positions, microstreet.distances,
                        microstreet.velocities,microstreet.lanes);
            det6_1.update(microstreet.positions, microstreet.distances,
                        microstreet.velocities,microstreet.lanes);
            det7_0.update(microstreet.positions, microstreet.old_pos, 
                        microstreet.velocities, microstreet.lanes ,time);
            det7_1.update(microstreet.positions, microstreet.old_pos, 
                        microstreet.velocities, microstreet.lanes ,time);
            det8_0.update(microstreet.positions, microstreet.distances,
                        microstreet.velocities, microstreet.lanes);
            det8_1.update(microstreet.positions, microstreet.distances,
                        microstreet.velocities, microstreet.lanes);


            double fcdx = (microstreet.fcd)-5.0;
            double fcvd = -(microstreet.fcvd);
            double fcacc = microstreet.fcacc;
            double fcv = microstreet.fcvel;
            fcvt.addPoint(time,fcacc,0);
            fcvt.addPoint(time,(fcv/7.0),1);
            fcdxdv.addPoint(fcvd, fcdx);

            double Q=det1_0.flow();
            double V=det1_0.harmVel();
            double rho=0;
            if (V>0.0){
                rho=Q/V;
                Qrho.addPoint(rho,Q);
            }
            Q=det1_1.flow();
            V=det1_1.harmVel();
            rho=0;
            if (V>0.0){
                rho=Q/V;
                Qrho.addPoint(rho,Q);
            }
            Q=det3_0.flow();
            V=det3_0.harmVel();
            rho=0;
            if (V>0.0){
                rho=Q/V;
                Qrho.addPoint(rho,Q);
            }
            Q=det3_1.flow();
            V=det3_1.harmVel();
            rho=0;
            if (V>0.0){
                rho=Q/V;
                Qrho.addPoint(rho,Q);
            }
            Q=det5_0.flow();
            V=det5_0.harmVel();
            rho=0;
            if (V>0.0){
                rho=Q/V;
                Qrho.addPoint(rho,Q);
            }
            Q=det5_1.flow();
            V=det5_1.harmVel();
            rho=0;
            if (V>0.0){
                rho=Q/V;
                Qrho.addPoint(rho,Q);
            }
            Q=det7_0.flow();
            V=det7_0.harmVel();
            rho=0;
            if (V>0.0){
                rho=Q/V;
                Qrho.addPoint(rho,Q);
            }
            Q=det7_1.flow();
            V=det7_1.harmVel();
            rho=0;
            if (V>0.0){
                rho=Q/V;
                Qrho.addPoint(rho,Q);
            }
            
            rho=det2_0.density();
            V=det2_0.avVel();
            QrhoS.addPoint(rho,rho*V);
            rho=det2_1.density();
            V=det2_1.avVel();
            QrhoS.addPoint(rho,rho*V);
            
            rho=det4_0.density();
            V=det4_0.avVel();
            QrhoS.addPoint(rho,rho*V);
            rho=det4_1.density();
            V=det4_1.avVel();
            QrhoS.addPoint(rho,rho*V);

            rho=det6_0.density();
            V=det6_0.avVel();
            QrhoS.addPoint(rho,rho*V);
            rho=det6_1.density();
            V=det6_1.avVel();
            QrhoS.addPoint(rho,rho*V);
            
              rho=det8_0.density();
            V=det8_0.avVel();
            QrhoS.addPoint(rho,rho*V);
            rho=det8_1.density();
            V=det8_1.avVel();
            QrhoS.addPoint(rho,rho*V);
       }   

       if (choice_Szen==5){
            
            det1_0.update(microstreet.positions, microstreet.old_pos, 
                        microstreet.velocities, microstreet.lanes, time);
            det1_1.update(microstreet.positions, microstreet.old_pos, 
                        microstreet.velocities, microstreet.lanes, time);

            double fcdx = (microstreet.fcd)-5.0;
            double fcvd = -(microstreet.fcvd);
            double fcacc = microstreet.fcacc;
            double fcv = microstreet.fcvel;

            double Q=(det1_0.flow());
            Q=Q+(det1_1.flow());
                        
            fcvt.addPoint(time,fcacc,0);
            fcvt.addPoint(time,(fcv/7.0),1);
            fcdxdv.addPoint(fcvd, fcdx);
            Qt.addPoint(time,Q,0);
       } 

// <Treiber aug06>
      if ((choice_Szen==2)||(choice_Szen==3)||(choice_Szen==4)){
//</Treiber>

      // if ((choice_Szen==3)||(choice_Szen==4)){
            det1_0.update(microstreet.positions, microstreet.old_pos,  microstreet.velocities, microstreet.lanes, time);
            det1_1.update(microstreet.positions, microstreet.old_pos,  microstreet.velocities, microstreet.lanes, time);
            det2_0.update(microstreet.positions, microstreet.distances,microstreet.velocities,microstreet.lanes);
            det2_1.update(microstreet.positions, microstreet.distances,microstreet.velocities,microstreet.lanes);
            det3_0.update(microstreet.positions, microstreet.old_pos,  microstreet.velocities, microstreet.lanes ,time);
            det3_1.update(microstreet.positions, microstreet.old_pos,  microstreet.velocities, microstreet.lanes ,time);
            det4_0.update(microstreet.positions, microstreet.distances,microstreet.velocities, microstreet.lanes);
            det4_1.update(microstreet.positions, microstreet.distances,microstreet.velocities, microstreet.lanes);

            double Q=det1_0.flow();
            double V=det1_0.harmVel();
            double rho=0;
            
		  if (V>0.0){
                      rho=Q/V;
                      Qrho.addPoint(rho,Q);
                    }
            Q=det1_1.flow();
            V=det1_1.harmVel();
            rho=0;
            if (V>0.0){
                rho=Q/V;
                Qrho.addPoint(rho,Q);
            }

            Q=det3_0.flow();
            V=det3_0.harmVel();
            rho=0;
            if (V>0.0){
                rho=Q/V;
                Qrho1.addPoint(rho,Q);
            }
            Q=det3_1.flow();
            V=det3_1.harmVel();
            rho=0;
            if (V>0.0){
                rho=Q/V;
                Qrho1.addPoint(rho,Q);
            }
            
            rho=det2_0.density();
            V=det2_0.avVel();
            QrhoS.addPoint(rho,rho*V);
            rho=det2_1.density();
            V=det2_1.avVel();
            QrhoS.addPoint(rho,rho*V);

            rho=det4_0.density();
            V=det4_0.avVel();
            QrhoS1.addPoint(rho,rho*V);
            rho=det4_1.density();
            V=det4_1.avVel();
            QrhoS1.addPoint(rho,rho*V);
       }
    }


    private void paintInsetDiagrams(Graphics g){
      final double CIRCUMF_M = 2.* Math.PI * RADIUS_M;

      if (choice_Szen==1){

          // actual diagrams

          g.drawImage (fcdxdv.picture(), dia1x,dia2y,null);
          g.drawImage (fcvt.picture(), dia2x,dia2y,null);
          g.drawImage (Qrho.picture(), dia3x,dia3y,null);
          g.drawImage (QrhoS.picture(), dia4x,dia4y,null);

          // labels "D1" (top) ... "D4" (right)

            int detx,dety;
            int distance=(int)(15.0 * scale);  // distance from road (15 m) 
            int xoff=metricsText.stringWidth ("D2");
            int yoff=(int)(0.8*metricsText.stringWidth("M"));
            g.setFont(textFont);
            g.setColor(Color.black);

          detx=(new DetPosC(0.125*CIRCUMF_M)).x;
          dety=(new DetPosC(0.125*CIRCUMF_M)).y;
          g.drawString("D1",detx-(int)(0.5*xoff),dety+distance+yoff);

          detx=(new DetPosC(0.375*CIRCUMF_M)).x;
          dety=(new DetPosC(0.375*CIRCUMF_M)).y;
          g.drawString("D2",detx+distance,dety+(int)(0.5*yoff));

          detx=(new DetPosC(0.625*CIRCUMF_M)).x;
          dety=(new DetPosC(0.625*CIRCUMF_M)).y;
          g.drawString("D3",detx-(int)(0.5*xoff),dety-distance);

          detx=(new DetPosC(0.875*CIRCUMF_M)).x;
          dety=(new DetPosC(0.875*CIRCUMF_M)).y;
          g.drawString("D4",detx-distance-xoff,dety+(int)(0.5*yoff));

      }

      // traffic light scenario; 2 diagrams

      if (choice_Szen==5){
          g.drawImage (fcvt.picture(), dia1x,dia1y,null);
          g.drawImage (Qt.picture(), dia2x,dia2y,null);          

          // label "D1" at the position of the detector

          double det1pos =0.8*CIRCUMF_M;
          int detx=(new DetPosU(det1pos)).x;
          int dety=(new DetPosU(det1pos)).y;
          g.setFont(textFont);
          g.setColor(Color.black);
          g.drawString("D1",detx,dety);


      }

      
      //if ((choice_Szen==3)||(choice_Szen==4)){
// <Treiber aug06>
      if ((choice_Szen==2)||(choice_Szen==3)||(choice_Szen==4)){
//</Treiber> 
          g.drawImage (Qrho1.picture(), dia1x,dia1y,null);          
          g.drawImage (QrhoS1.picture(), dia2x,dia2y,null);
          g.drawImage (Qrho.picture(), dia3x,dia3y,null);
          g.drawImage (QrhoS.picture(), dia4x,dia4y,null);

          double detpos = (choice_Szen==4) ? 0.4*CIRCUMF_M : 0.6*CIRCUMF_M;
          int detx=(new DetPosU(detpos)).x;
          int dety=(new DetPosU(detpos)).y;
          g.setFont(textFont);
          g.setColor(Color.black);
          g.drawString("D1",detx,dety);
          detpos =0.9*CIRCUMF_M;
          detx=(new DetPosU(detpos)).x;
          dety=(new DetPosU(detpos)).y;
          g.setFont(textFont);
          g.setColor(Color.black);
          g.drawString("D2",detx,dety);
      }

    }

    // end paintInsetDiagrams



}

