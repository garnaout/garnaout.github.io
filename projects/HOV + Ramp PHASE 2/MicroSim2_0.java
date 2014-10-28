
// Compile with: javac MicroSim2_0.java

import java.applet.*;
import java.awt.*;
import java.util.Vector;
import java.lang.Integer;
import java.io.*;
import java.awt.event.*;

public class MicroSim2_0 extends Applet implements Constants{

    
    private int choice_Szen=CHOICE_SZEN_INIT;


    // controlled variables with init. values
    private double density=0.001*DENS_INIT_INVKM;  // avg. density closed s.
    private int    tsleep_ms=SPEED_INIT;           // sleeping time per sim. step
    private double qIn=Q_INIT2/3600.;              // Total inflow (all lanes) (veh/s)
    private double qRamp=QRMP_INIT2/3600.;         // Ramp inflow (veh/s)
    private double p_factorRamp=0.;                // ramp Lanechange factor
    private double deltaBRamp=DELTABRAMP_INIT;     // ramp Lanechange factor
    private double v0_limit=(CHOICE_SZEN_INIT==3) ? 80/3.6 : 35;  // speed limit (m/s)
    private double perTr=FRAC_TRUCK_INIT_CIRCLE;   // truck fraction !!!
    private double p_factor=0.;                    // lanechanging: politeness factor
    private double deltaB=0.2;                     // lanechanging: changing threshold


	// DEFINE CACC
	//private double CACC_rate = (qIn) * CACC_init/100 ; 
	private double CACC_rate = CACC_init; 

    // Colors

    final Color BACKGROUND_COLOR  
          = new Color((float)(0.8), (float)(0.8), (float)(0.8));
    final Color SIM_BG_COLOR  = new Color(0,150,0); // green grass

    final Color BUTTON_COLOR 
          = new Color((float)(0.8), (float)(0.8), (float)(0.8));



    


	// pre-defined labels
    private Label label_density;
    private Label label_simSpeed1, label_simSpeed2, label_simSpeed3, label_simSpeed6;
    private Label label_qIn2, label_qIn3;
    private Label label_qRamp;
    private Label label_p_factorRamp;
    private Label label_deltaBRamp;
    private Label label_perTr1;
    private Label label_perTr35;
    private Label label_v0_limit;
    private Label label_p_factor;
    private Label label_deltaB;

	// new labels added
	private Label label_CACC_percentage;

	// pre-defined strings
    String str_avgdens = (GERMAN) ? "Verkehrsdichte" : "Average Density"; 
    String str_inflow  = (GERMAN) ? "Haupt-Zufluss" : "Main Inflow"; 
    String str_rmpinflow  = (GERMAN) ? "Zufluss der Zufahrt"
                                         : "Ramp Inflow"; 
    String str_polite  = (GERMAN) ? "Hoeflichkeitsfaktor" 
                                      : "Politeness Factor"; 
    String str_rmppolite  = (GERMAN) ? "p-Faktor Zufahrt" 
                                      : "Ramp politeness Factor"; 
    String str_trucks  = (GERMAN) ? "LKW-Anteil" : "Truck Percentage"; 
    String str_db  = (GERMAN) ? "Wechselschwelle" : "Changing Threshold"; 
    String str_rmpdb  = (GERMAN) ? "a_bias,Zufahrt" : "a_bias,onramp"; 
    //String str_speed  = (GERMAN) ? "Simulationsgeschwindigkeit" 
    //                                 : "Simulation Speed"; 
    //String str_framerate  = (GERMAN) ? " Bilder/s" 
    //                                     : " Frames/s"; 
    String str_speed  = (GERMAN) ? "Zeitlicher Warp-Faktor" 
                                     : "Simulation Speed"; 
    String str_framerate  = (GERMAN) ? " - fach" 
                                         : " times"; 
    String str_speedl  = (GERMAN) ? "Tempolimit" 
                                     : "Imposed Speed Limit"; 
    String str_vehperh  = (GERMAN) ? " Kfz/h" : " Vehicles/h"; 
    String str_vehperkm  = (GERMAN) ? " Kfz/km/Spur" 
                                        : " Vehicles/km/lane"; 

	// new strings added
	String str_cacc_percentage  = (GERMAN) ? "CACC Percentage" : "CACC Percentage"; 



	// Buttons that appear on the main screen
    String str_button1  = (GERMAN) ? "Ringstrasse" : "1: Highway (3 Km Highway) Not working"; 
    String str_button2  = (GERMAN) ? "Zufahrt" : "2:Highway (10 Km Highway)"; 
    String str_button3  = (GERMAN) ? "Spursperrung" : "3: Not working"; 
    String str_button4  = (GERMAN) ? "Steigung" : "4: Not working"; 
    String str_button5  = (GERMAN) ? "Stadtverkehr" : "5: Not working"; 
    String str_button6  = (GERMAN) ? "Deterministisches Chaos" : "6: Not working"; 


    // interactive control  (1:1 to corresponding labels)

    private Scrollbar sb_density;
    private Scrollbar sb_simSpeed1,sb_simSpeed2,sb_simSpeed3,sb_simSpeed6;
    private Scrollbar sb_qIn2,sb_qIn3;
    private Scrollbar sb_qRamp;
    private Scrollbar sb_p_factorRamp;
    private Scrollbar sb_deltaBRamp;
    private Scrollbar sb_perTr1;
    private Scrollbar sb_perTr35;
    private Scrollbar sb_v0_limit;
    private Scrollbar sb_p_factor;
    private Scrollbar sb_deltaB;

	// new scrollbar added
	private Scrollbar sb_CACC_percentage; 


    // buttons

    private Button start_button;  
    private Button stop_button;
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
    private Button button6;

    // graphical components

    private int textHeight;                  // in pixels
    private Panel pClosedSystem=new Panel(); // scrollbars for closed system 1
    private Panel pRamp  =new Panel();       // Ramp szenario 2
    private Panel pSource=new Panel();       // scenarios 3-5
    private Panel pLanechange=new Panel();   // Slalom Szen. 6


    private Panel      pButtons=new Panel();           // scenario button field
    private Panel      pScrollbars=new Panel();        // scrollbar field
    private CardLayout cardLayout = new CardLayout(0,10); // for pScrollbars
    private SimCanvas  simCanvas=new SimCanvas (2, density, qIn, perTr,  p_factor, deltaB, p_factorRamp, deltaBRamp, tsleep_ms, CACC_rate);// simulation field

    private TextCanvas1 textCanvas=new TextCanvas1(1); // brown text field


    public void init(){

	// determine size of screen, application, and client area
	// usedWidt in makeGlobalLayout


        // size given relative to screen

        Dimension screensize = getToolkit().getScreenSize();
	if(CONTROL_SIZE==0){ 
    
  	  setSize ((int)(REL_APPL_SIZE*screensize.width),
	           (int)(REL_APPL_SIZE*screensize.height));
	}

        // size given by applet tag => by size of browser window

	else if(CONTROL_SIZE==1){ ;}

        // fixed size

	else if(CONTROL_SIZE==2){
	    setSize (APPL_WIDTH,APPL_HEIGHT);
	}

        int textHeight = (int)(REL_TEXTHEIGHT * getSize().width);
        if (textHeight>MAX_TEXTHEIGHT) textHeight=MAX_TEXTHEIGHT;
        if (textHeight<MIN_TEXTHEIGHT) textHeight=MIN_TEXTHEIGHT;
        int sbFontHeight = (int)(SBTEXT_MAINTEXT_HEIGHTRATIO *textHeight);
        Font textFont=new Font("SansSerif",Font.PLAIN,textHeight);
        Font sbFont=new Font("SansSerif",Font.PLAIN,sbFontHeight);
        System.out.println("----------------------\nMicroSim2_1.init():"
                          + " xsize="+getSize().width);

        setBackground (BACKGROUND_COLOR);

	pButtons.setLayout(new GridLayout(2,4));
        pButtons.setFont(textFont); 
	pSource.setLayout(new GridBagLayout());
	pRamp.setLayout(new GridBagLayout());
	pLanechange.setLayout(new GridBagLayout());
	pClosedSystem.setLayout(new GridBagLayout());

	 

    // this sets the layout depending on the scenario, the font, and the background color of the scroll bars
	pScrollbars.setLayout(cardLayout);
    pScrollbars.setFont(sbFont); 
	pScrollbars.setBackground(SIM_BG_COLOR); // as sim backgr 
	//	pScrollbars.setBackground(Color.BUTTON_COLOR); // if outside

        System.out.println("textFont="+textFont
                  +", sbFont="+sbFont);

	start_button=new Button("Start / Reset");
	start_button.setForeground(Color.black);
	start_button.setBackground(BACKGROUND_COLOR);
	//start_button.setFont(sbFont);
	pButtons.add(start_button);
 
	// #######################################
	// Define scrollbars 
	// #######################################


	int flow_invh = (int) (3600.0*qIn);
	int flowRamp_invh = (int) (3600.0*qRamp);
	int dens_invkm = (int)(1000*density);
    int lnspeed100init = (int)(100*Math.log(1./SPEED_INIT));
    int lnspeed100max  = (int)(100*Math.log(1./SPEED_MIN)); //MIN !!
    int lnspeed100min  = (int)(100*Math.log(1./SPEED_MAX));
	int truckPerc = (int) (100.0*perTr);
    int truckPerc1 = (int)(100*FRAC_TRUCK_INIT_CIRCLE);
	int p_factor100 = (int) (p_factor*100);
	int p_factor100Ramp = (int) (p_factorRamp*100);
	int deltaB100 = (int) (100.0*deltaB);
	int deltaB100Ramp = (int) (100.0*deltaBRamp);
	int i_warpfactor = (int)(1000*TIMESTEP_S/tsleep_ms); //!! at initial

	// initial CACC value
	int CACC_percentage = (int) (100.0* CACC_rate);
        
	String str_warpfactor     = String.valueOf(i_warpfactor) + "." + String.valueOf((int)(10000*TIMESTEP_S/tsleep_ms-10*i_warpfactor));

    sb_density      = getSB(DENS_MIN_INVKM, DENS_MAX_INVKM, dens_invkm);
	sb_simSpeed1    = getSB(lnspeed100max, lnspeed100min, lnspeed100init);
	sb_simSpeed2    = getSB(lnspeed100max, lnspeed100min, lnspeed100init);
	sb_simSpeed3    = getSB(lnspeed100max, lnspeed100min, lnspeed100init);
	sb_simSpeed6    = getSB(lnspeed100max, lnspeed100min, lnspeed100init);
    sb_qIn2         = getSB(0, Q_MAX, flow_invh);
	sb_qIn3         = getSB(0, Q_MAX, flow_invh);
	sb_qRamp        = getSB(0, QRMP_MAX, flowRamp_invh);
	sb_p_factorRamp = getSB((int)(100*PRMP_MIN), (int)(100*PRMP_MAX), p_factor100Ramp);
	sb_deltaBRamp   = getSB((int)(100*DELTABRAMP_MIN), 
                                (int)(100*DELTABRAMP_MAX), deltaB100Ramp);
	sb_perTr1        = getSB(0, 100, (int)(100*FRAC_TRUCK_INIT_CIRCLE));
	sb_perTr35        = getSB(0, 100, truckPerc);
	//sb_v0_limit     = getSB(0, 140, 80);
	sb_v0_limit     = getSB(V0_MIN_KMH, V0_MAX_KMH, V0_INIT_KMH);
	sb_p_factor     = getSB(0, 100*P_MAX, p_factor100);
	sb_deltaB       = getSB(0, (int)(100*DELTAB_MAX), deltaB100);

	// new defined scroll bar the percentage of CACC vehicles
	sb_CACC_percentage = getSB(0, 100, CACC_percentage); 

	// #######################################
	// Make Layout for scrollbars for 4 panels Szen 1,2,3-5 (same),6
	// #######################################

	GridBagConstraints gbconstr = new GridBagConstraints();

	// 1th column: Variable names

	gbconstr.insets = new Insets(SB_SPACEY,SB_SPACEX,SB_SPACEY,SB_SPACEX);
                 // (N,W,S,E)
	gbconstr.gridx = 0;
	gbconstr.gridy = 0;
	gbconstr.gridwidth=1;
	gbconstr.gridheight=1;
	gbconstr.fill = GridBagConstraints.NONE;
	gbconstr.anchor = GridBagConstraints.EAST;
	gbconstr.weightx = 0.;
        gbconstr.weighty = 0.5;


	// on ramp scenario
	pRamp.add(new Label(str_inflow),gbconstr); // this is the first scroll bar


	// other scenarios
	pClosedSystem.add(new Label(str_avgdens),gbconstr);
	pSource.add(new Label(str_inflow),gbconstr);
	pLanechange.add(new Label(str_polite), gbconstr);

	gbconstr.gridx = 0;
	gbconstr.gridy = 1;

	// other scenarios
 	pClosedSystem.add(new Label(str_trucks),gbconstr);
 	pSource.add(new Label(str_trucks),gbconstr);
	pLanechange.add(new Label(str_db), gbconstr);


	// on ramp scenario
	pRamp.add(new Label(str_rmpinflow),gbconstr); // this is the second scroll bar

	gbconstr.gridx = 0;
	gbconstr.gridy = 2;

	// other scenarios
	pClosedSystem.add(new Label(str_speed),gbconstr);
	pSource.add(new Label(str_speedl), gbconstr);
	pLanechange.add(new Label(str_speed), gbconstr);

	// on ramp scenario
	pRamp.add(new Label(str_rmppolite), gbconstr); // this is the third scroll bar

	gbconstr.gridx = 0;
	gbconstr.gridy = 3;

	// on ramp scenario
	//pRamp.add(new Label(str_rmpdb), gbconstr); // this is the 4th scroll bar
    pRamp.add(new Label(str_trucks),gbconstr);// truck percentage (newely implemented)


	// other scenarios
	pSource.add(new Label(str_speed), gbconstr);

	gbconstr.gridx = 0;
	gbconstr.gridy = 4;

	// on ramp scenario
	pRamp.add(new Label(str_speed), gbconstr);            // this is the 5th scroll bar

	gbconstr.gridx = 0;
	gbconstr.gridy = 5;
	
	pRamp.add(new Label(str_cacc_percentage), gbconstr);  // this is the 6th scroll bar

 
	// 2th column: actual scrollbars

	gbconstr.gridx = 1;
	gbconstr.gridy = 0;
	gbconstr.weightx = 1.;
	gbconstr.fill = GridBagConstraints.HORIZONTAL;
	gbconstr.anchor = GridBagConstraints.CENTER;

	pClosedSystem.add(sb_density,gbconstr);
	pRamp.add(sb_qIn2,gbconstr); // 1
	pSource.add(sb_qIn3,gbconstr);
	pLanechange.add(sb_p_factor,gbconstr);

	gbconstr.gridx = 1;
	gbconstr.gridy = 1;

	pClosedSystem.add(sb_perTr1,gbconstr);
	pRamp.add(sb_qRamp,gbconstr); //2
	pSource.add(sb_perTr35,gbconstr);
	pLanechange.add(sb_deltaB,gbconstr);

	gbconstr.gridx = 1;
	gbconstr.gridy = 2;

	pClosedSystem.add(sb_simSpeed1,gbconstr);
    pRamp.add(sb_p_factorRamp,gbconstr); //3
	pSource.add(sb_v0_limit,gbconstr); 
	pLanechange.add(sb_simSpeed6,gbconstr); 

	gbconstr.gridx = 1;
	gbconstr.gridy = 3;
  //pRamp.add(sb_deltaBRamp,gbconstr);
	pRamp.add(sb_perTr1,gbconstr);   //4
	pSource.add(sb_simSpeed3,gbconstr); 

	gbconstr.gridx = 1;
	gbconstr.gridy = 4;

    pRamp.add(sb_simSpeed2,gbconstr);

// newely added for the on ramp scenario CACC scroll bar
		gbconstr.gridx = 1;
		gbconstr.gridy = 5;

	pRamp.add(sb_CACC_percentage,gbconstr);




	// 3th column: Actual values + units

	gbconstr.gridx = 2;
	gbconstr.gridy = 0;
	gbconstr.weightx = 0.;
	gbconstr.fill = GridBagConstraints.NONE;
	gbconstr.anchor = GridBagConstraints.WEST;
	
	// on ramp
	pRamp.add(label_qIn2  = new Label(String.valueOf (flow_invh) + str_vehperh),gbconstr);

	// other scenarios
    pClosedSystem.add(label_density  = new Label(String.valueOf(dens_invkm)+str_vehperkm),gbconstr);
	pSource.add(label_qIn3 = new Label(String.valueOf (flow_invh) + str_vehperh),gbconstr);
	pLanechange.add(label_p_factor = new Label(String.valueOf(p_factor)), gbconstr);

	gbconstr.gridx = 2;
	gbconstr.gridy = 1;
	
	//on ramp
	pRamp.add(label_qRamp = new Label(String.valueOf (flowRamp_invh) + str_vehperh),gbconstr);
		
    // other scenarios
	pClosedSystem.add(label_perTr1 = new Label(String.valueOf((int)(100*FRAC_TRUCK_INIT_CIRCLE)) +" %"),gbconstr);
	pSource.add(label_perTr35 = new Label(String.valueOf(truckPerc) +" %"),gbconstr);
	pLanechange.add(label_deltaB = new Label(String.valueOf(deltaB)+" m/s^2"), gbconstr);

	gbconstr.gridx = 2;
	gbconstr.gridy = 2;

	// on ramp
	pRamp.add(label_p_factorRamp = new Label(  String.valueOf(p_factorRamp)), gbconstr);

    // other scenarios
	pClosedSystem.add(label_simSpeed1  = new Label( str_warpfactor  +str_framerate),gbconstr);
	pSource.add(label_v0_limit  = new Label( String.valueOf((int)(3.6*v0_limit)) +" km/h"), gbconstr);
	pLanechange.add(label_simSpeed6 = new Label( str_warpfactor +str_framerate), gbconstr);

	gbconstr.gridx = 2;
	gbconstr.gridy = 3;

	// on ramp
	//pRamp.add(label_deltaBRamp  = new Label(  String.valueOf(deltaBRamp)+" m/s^2"), gbconstr);
	 pRamp.add(label_perTr1 = new Label(String.valueOf((int)(100*FRAC_TRUCK_INIT_CIRCLE)) +" %"),gbconstr);
	
	// other scenarios
	pSource.add(label_simSpeed3  = new Label( str_warpfactor +str_framerate), gbconstr);

	gbconstr.gridx = 2;
	gbconstr.gridy = 4;

	// on ramp
	pRamp.add(label_simSpeed2  = new Label( str_warpfactor +str_framerate), gbconstr);

	gbconstr.gridx = 2;
	gbconstr.gridy = 5;


	// on ramp
	pRamp.add(label_CACC_percentage  = new Label( String.valueOf(CACC_percentage)  +" %" ), gbconstr); // we'll define later

	pScrollbars.add("Source",pSource);
	pScrollbars.add("onRamp",pRamp);
    pScrollbars.add("closedSystem",pClosedSystem);
	pScrollbars.add("Lanechange",pLanechange);
      
    makeGlobalLayout();

	if(true){   // obviously no dynamic layout possible!
           if(CHOICE_SZEN_INIT==1){
             cardLayout.show(pScrollbars,"closedSystem");}
           else if(CHOICE_SZEN_INIT==2){
             cardLayout.show(pScrollbars,"onRamp");}
           else if(CHOICE_SZEN_INIT==6){
             cardLayout.show(pScrollbars,"Lanechange");}
           else{cardLayout.show(pScrollbars,"Source");}
	}

    } // end init





    private void makeGlobalLayout(){
        boolean isCircular = ((choice_Szen==1) || (choice_Szen==6));
        
		int usedWidth = getSize().width - 2*MARGIN;   // -- 2.5km highway

 
		// Custom Size
		 //int usedWidth = 4975;    // -- 10 km
		//int usedWidth = 3001;    // -- 6 km
		//int usedWidth = 3700;    // -- 7.5 km
		//int usedWidth = 4200;    // -- 8.5 km
        
		int usedHeight = getSize().height -  2*MARGIN;
        textHeight = (int)(REL_TEXTHEIGHT * getSize().width);

        System.out.println("MicroSim2_0.makeGlobalLayout:"
                      + "Applet-size="+(getSize().width) + " X " 
                      + (getSize().height)
                      + ", Client-size="+usedWidth + " X " 
                      + usedHeight);


        int buttWinHeight = (int)(2.2*textHeight+3*MARGIN);
        int buttWinTop = getInsets().top;

        int textWinHeight = (SHOW_TEXT) ? TEXTWINDOW_HEIGHT : 0;
        int textWinTop = (usedHeight + getInsets().top) - textWinHeight;

	// not clear why I must subtract addtl. getInsets().top below
        int simWinHeight = usedHeight - buttWinHeight 
                      - textWinHeight - getInsets().top;
        int simWinTop = buttWinTop + buttWinHeight;

        int simSize = (simWinHeight<usedWidth) ? simWinHeight : usedWidth;
        int sbWidth = isCircular
            ? (int)(0.8*simSize)
            : (int)(0.7*usedWidth);
        int sbLeft  = isCircular
            ? (int)(0.5*(simSize - sbWidth)) // 0.5 = center
	    : (int)(0.95*usedWidth - sbWidth);
	int sbTop  = isCircular
            ? simWinTop + (int)(0.5*(simSize - SBWINDOW_HEIGHT))
            : ((STRAIGHT_ROAD) && (choice_Szen==2)) 
                    ? simWinTop+ (int)(0.1*simWinHeight)
                    : simWinTop+ (int)(RADIUS_M*simCanvas.getScale())
                               - (int)(0.5*SBWINDOW_HEIGHT);


	setLayout (null);

        pButtons.setBounds(getInsets().left, buttWinTop,
                                  usedWidth, buttWinHeight/2);
	this.add(pButtons);

	if(true){   // obviously no dynamic layout possible!
           pScrollbars.setBounds(sbLeft,sbTop,
                                sbWidth,SBWINDOW_HEIGHT);
	   this.add(pScrollbars);
           if(choice_Szen==1){
	       //if(CHOICE_SZEN_INIT==1){
             cardLayout.show(pScrollbars,"closedSystem");}
           else if(choice_Szen==2){
             cardLayout.show(pScrollbars,"onRamp");}
           else if(choice_Szen==6){
             cardLayout.show(pScrollbars,"Lanechange");}
           else{cardLayout.show(pScrollbars,"Source");}
	}
        if(SHOW_TEXT){
          textCanvas.setBounds(getInsets().left,textWinTop,
                                      usedWidth,textWinHeight);
	  this.add(textCanvas);
	}
        simCanvas.setBounds(getInsets().left,simWinTop,
                                   usedWidth,simWinHeight);
	this.add(simCanvas);
    }
    // end makeGlobalLayout

    private final static boolean sbBroken =
	(new Scrollbar(Scrollbar.HORIZONTAL,20,10,0,20).getValue() != 20);

    private Scrollbar getSB (int min, int max, int init){
	final int inc = 1;
	return new Scrollbar(Scrollbar.HORIZONTAL, init, inc, min,
			     max +(sbBroken?inc:0));
    }

    
    public boolean handleEvent (Event evt) {
	//    System.out.println("MicroSim2_0.handleEvent(evt)");
	switch (evt.id) {
	case Event.SCROLL_LINE_UP:
	case Event.SCROLL_LINE_DOWN:
	case Event.SCROLL_PAGE_UP:
	case Event.SCROLL_PAGE_DOWN:
	case Event.SCROLL_ABSOLUTE:
	int i_warpfactor = (int)(1000*TIMESTEP_S/tsleep_ms); //!!Veraend Sb
        String str_warpfactor 
           = String.valueOf(i_warpfactor) 
           + "."
	   + String.valueOf((int)(10000*TIMESTEP_S/tsleep_ms-10*i_warpfactor));

	    if (evt.target == sb_simSpeed1) {
	      tsleep_ms = (int)(Math.exp( - 0.01*sb_simSpeed1.getValue()));
	      label_simSpeed1.setText (
                str_warpfactor +str_framerate);
   	    } 
	    else if (evt.target == sb_simSpeed2) {
	      tsleep_ms = (int)(Math.exp( - 0.01*sb_simSpeed2.getValue()));
	      label_simSpeed2.setText (
                str_warpfactor +str_framerate);
   	    } 
	    else if (evt.target == sb_simSpeed3) {
	      tsleep_ms = (int)(Math.exp( - 0.01*sb_simSpeed3.getValue()));
	      label_simSpeed3.setText (
                str_warpfactor +str_framerate);
   	    } 
	    else if (evt.target == sb_simSpeed6) {
	      tsleep_ms = (int)(Math.exp( - 0.01*sb_simSpeed6.getValue()));
	      label_simSpeed6.setText (
                str_warpfactor +str_framerate);
   	    } 
	    else if (evt.target == sb_density) {
		int dens_invkm = sb_density.getValue();
		if (dens_invkm != 1000*density) {
                    density = 0.001*dens_invkm;
		    label_density.setText (
                       String.valueOf (dens_invkm)+str_vehperkm);
		}
	    } 
	    else if (evt.target == sb_qIn2) {
		int newval = sb_qIn2.getValue();
		double q = ((double)(newval))/3600.0;
		if (q != qIn) {
		    qIn = q;      
		    label_qIn2.setText (String.valueOf (newval)
                    +str_vehperh);
		}
	    } 
	    else if (evt.target == sb_qIn3) {
		int newval = sb_qIn3.getValue();
		double q = ((double)(newval))/3600.0;
		if (q != qIn) {
		    qIn = q;      
		    label_qIn3.setText (String.valueOf (newval)
                    +str_vehperh);
		}
	    } 
	    else if (evt.target == sb_qRamp) {
		int newval = sb_qRamp.getValue();
		double q = ((double)(newval))/3600.0;
		if (q != qRamp) {
		    qRamp = q;      
		    label_qRamp.setText (String.valueOf (newval)
                    +str_vehperh);
		}
	    } 
	    else if (evt.target == sb_p_factorRamp){
		int newval = sb_p_factorRamp.getValue();
		double p =  newval/100.0;
		if (p != p_factorRamp) {
		    p_factorRamp = p;
		    label_p_factorRamp.setText (String.valueOf (p)
                   + " ");
		}
	    }
	    else if (evt.target == sb_deltaBRamp){
		int newval = sb_deltaBRamp.getValue();
		double p =  newval/100.0;
		if (p != deltaBRamp) {
		    deltaBRamp = p;
		    label_deltaBRamp.setText (String.valueOf (p)
                   + " m/s^2");
		}
	    }
	    else if (evt.target == sb_perTr1) {
		int newval = sb_perTr1.getValue();
		double pT =  newval/100.0;
		if (pT != perTr) {
		    perTr = pT;
		    label_perTr1.setText (String.valueOf (newval)+" %");
		}
	    }

	// Implementation of the CACC Scroll bar change
		else if (evt.target == sb_CACC_percentage) 
		{
			int newval = sb_CACC_percentage.getValue();
			double pT =  newval/100.0;
			if (pT != CACC_rate) 
			{
				CACC_rate = pT;
				label_CACC_percentage.setText (String.valueOf (newval)+" %");
			}
		}



	    else if (evt.target == sb_perTr35) {
		int newval = sb_perTr35.getValue();
		double pT =  newval/100.0;
		if (pT != perTr) {
		    perTr = pT;
		    label_perTr35.setText (String.valueOf (newval)+" %");
		}
	    }
	    else if (evt.target == sb_v0_limit) {
		int newval = (int)(0.1*sb_v0_limit.getValue() + 0.5);
		v0_limit = 10*newval/3.6;
		label_v0_limit.setText (String.valueOf (10*newval)+" km/h");
	    }
	    else if (evt.target == sb_p_factor){
		int newval = sb_p_factor.getValue();
		double p =  newval/100.0;
		if (p != p_factor) {
		    p_factor = p;
		    label_p_factor.setText (String.valueOf (p));
		}
	    }
	    else if (evt.target == sb_deltaB){
		int newval = sb_deltaB.getValue();
		double p =  newval/100.0;
		if (p != deltaB) {
		    deltaB = p;
		    label_deltaB.setText (String.valueOf (p)+" m/s^2");
		}
	    }
	    if(choice_Szen!=2){
              simCanvas.newValues(choice_Szen, density, qIn, perTr, 
			v0_limit, p_factor, deltaB, tsleep_ms, CACC_rate);}
            else{
	    simCanvas.newValues2(qIn, perTr, 
            p_factor, deltaB, qRamp, p_factorRamp, deltaBRamp, tsleep_ms, CACC_rate);
	    }
	}
	return super.handleEvent(evt);
    }

    public boolean action(Event event, Object arg){
        System.out.println("MicroSim2_0.action(...)");

	int i_warpfactor = (int)(1000*TIMESTEP_S/tsleep_ms); //1000 new scen
        String str_warpfactor  
	 = String.valueOf(i_warpfactor) 
	 + "."
	 + String.valueOf((int)(10000*TIMESTEP_S/tsleep_ms-10*i_warpfactor));

	if (event.target==start_button){
            stop();        // stop necessary!
	    start();
	    return true;
	}
	if (event.target==stop_button){
	    stop(); 
	    return true;
	}

	if (event.target==button1){
	  
		stop();
		choice_Szen=2;

		qIn = Q_INIT2/3600.;
		qRamp = QRMP_INIT2/3600.;
		int truckPerc2 = (int)(100*FRAC_TRUCK_INIT);
		perTr = truckPerc2/100.; // no slider bar in this scenario//!!!

		System.out.println("truckPerc2="+truckPerc2+" perTr="+perTr);

		int flow2_invh = (int)(3600*qIn);
		int flowRamp2_invh = (int)(3600*qRamp);
		p_factorRamp=0.;
		deltaBRamp=DELTABRAMP_INIT;   // negative shift threshold for onramp!
		System.out.println("p_factorRamp="+p_factorRamp);

		sb_qIn2.setValue(flow2_invh);
		label_qIn2.setText (String.valueOf (flow2_invh)+str_vehperh);

		sb_qRamp.setValue(flowRamp2_invh);
		label_qRamp.setText (String.valueOf(flowRamp2_invh)+str_vehperh);

		sb_p_factorRamp.setValue( (int)(100*p_factorRamp));
		label_p_factorRamp.setText (String.valueOf(p_factorRamp));

		sb_deltaBRamp.setValue( (int)(100*deltaBRamp));
		label_deltaBRamp.setText (String.valueOf(deltaBRamp));

		sb_simSpeed2.setValue((int)(100*Math.log(1./tsleep_ms)));
		label_simSpeed2.setText (str_warpfactor+str_framerate);


		makeGlobalLayout();
		cardLayout.show(pScrollbars,"onRamp");

		if(SHOW_TEXT)
		{
			textCanvas.update(choice_Szen);
		}


		// newvalues2!!
		simCanvas.newValues2(
			qIn, perTr, p_factor, deltaB, qRamp, p_factorRamp, deltaBRamp, tsleep_ms, CACC_rate);
		simCanvas.start(choice_Szen,density);
		return true;
  
  
	    }

	if (event.target==button2){
	    stop();
	    choice_Szen=2;

            qIn = Q_INIT2/3600.;
            qRamp = QRMP_INIT2/3600.;
            int truckPerc2 = (int)(100*FRAC_TRUCK_INIT);
	        perTr = truckPerc2/100.; // no slider bar in this scenario//!!!

         System.out.println("truckPerc2="+truckPerc2+" perTr="+perTr);

        int flow2_invh = (int)(3600*qIn);
        int flowRamp2_invh = (int)(3600*qRamp);
	    p_factorRamp=0.;
	    deltaBRamp=DELTABRAMP_INIT;   // negative shift threshold for onramp!
         System.out.println("p_factorRamp="+p_factorRamp);

	    sb_qIn2.setValue(flow2_invh);
	    label_qIn2.setText (String.valueOf (flow2_invh)+str_vehperh);

	    sb_qRamp.setValue(flowRamp2_invh);
	    label_qRamp.setText (String.valueOf(flowRamp2_invh)+str_vehperh);

	    sb_p_factorRamp.setValue( (int)(100*p_factorRamp));
	    label_p_factorRamp.setText (String.valueOf(p_factorRamp));

	    sb_deltaBRamp.setValue( (int)(100*deltaBRamp));
	    label_deltaBRamp.setText (String.valueOf(deltaBRamp));

        sb_simSpeed2.setValue((int)(100*Math.log(1./tsleep_ms)));
        label_simSpeed2.setText (str_warpfactor+str_framerate);


	    makeGlobalLayout();
	    cardLayout.show(pScrollbars,"onRamp");

	    if(SHOW_TEXT){
	      textCanvas.update(choice_Szen);
	    }


	    // newvalues2!!
	    simCanvas.newValues2(
                     qIn, perTr, p_factor, deltaB, qRamp, p_factorRamp, deltaBRamp, tsleep_ms, CACC_rate);
	    simCanvas.start(choice_Szen,density);
	    return true;
	 }

	if (event.target==button3){
	    stop();
	    choice_Szen=3;         // Closing of one lane

	    p_factor=0.25;
	    deltaB=0.1;
	    v0_limit = V0_INIT_KMH/3.6;
        int flow3_invh = Q_INIT3;
        qIn = flow3_invh/3600.;
        int truckPerc3 = (int)(100*FRAC_TRUCK_INIT);
	    perTr = truckPerc3/100.;

	    sb_qIn3.setValue(flow3_invh);
	    label_qIn3.setText (String.valueOf (flow3_invh)+str_vehperh);

	    sb_perTr35.setValue(truckPerc3);
	    label_perTr35.setText (String.valueOf (truckPerc3)+" %");

        sb_simSpeed3.setValue((int)(100*Math.log(1./tsleep_ms)));
	    label_simSpeed3.setText(str_warpfactor+str_framerate);

        sb_v0_limit.setValue((int)(v0_limit*3.6));
	    label_v0_limit.setText (String.valueOf( (int)(v0_limit*3.6)) +" km/h");

	    makeGlobalLayout();

	    if(SHOW_TEXT){
	      textCanvas.update(choice_Szen);
	    }
	    cardLayout.show(pScrollbars,"Source");
	    simCanvas.newValues(choice_Szen, density, 
                      qIn, perTr, v0_limit, p_factor, deltaB,
                      tsleep_ms, CACC_rate); 
	    simCanvas.start(choice_Szen,0.0);
	    return true;
	}

	if (event.target==button4){
	    stop();
	    choice_Szen=4;            // uphill gradient

	    double p_factor=0.25;
	    double deltaB=0.1;
            int flow4_invh = Q_INIT4;
            qIn = flow4_invh/3600.;
	    v0_limit = V0_INIT_KMH/3.6;
            int truckPerc4 = (int)(100*FRAC_TRUCK_INIT);
	    perTr = truckPerc4/100.;

	    sb_qIn3.setValue(flow4_invh);
	    label_qIn3.setText (String.valueOf (flow4_invh)+str_vehperh);

	    sb_perTr35.setValue(truckPerc4);
	    label_perTr35.setText (String.valueOf (truckPerc4)+" %");

            sb_simSpeed3.setValue((int)(100*Math.log(1./tsleep_ms)));
	    label_simSpeed3.setText(str_warpfactor+str_framerate);

            sb_v0_limit.setValue((int)(v0_limit*3.6));
	    label_v0_limit.setText (String.valueOf( 
               (int)(v0_limit*3.6)) +" km/h");



	    makeGlobalLayout();

	    if(SHOW_TEXT){
	      textCanvas.update(choice_Szen);
	    }

	    cardLayout.show(pScrollbars,"Source");
	    simCanvas.newValues(choice_Szen, density, 
                      qIn, perTr, v0_limit, p_factor, deltaB,
                      tsleep_ms,CACC_rate);
	    simCanvas.start(choice_Szen,0.0);
	    return true;
	    }

	if (event.target==button5){
	    stop();
	    choice_Szen=5;          // Traffic lights

            int flow5_invh = Q_INIT5;
            qIn = flow5_invh/3600.;
            int truckPerc5 = (int)(100*FRAC_TRUCK_INIT);
	    perTr = truckPerc5/100.;
	    v0_limit = V0_INIT_KMH/3.6;

	    sb_qIn3.setValue(flow5_invh);
	    label_qIn3.setText (String.valueOf (flow5_invh)+str_vehperh);

	    sb_perTr35.setValue(truckPerc5);
	    label_perTr35.setText (String.valueOf (truckPerc5)+" %");

            sb_simSpeed3.setValue((int)(100*Math.log(1./tsleep_ms)));
	    label_simSpeed3.setText(str_warpfactor+str_framerate);

            sb_v0_limit.setValue((int)(v0_limit*3.6));
	    label_v0_limit.setText (String.valueOf( 
               (int)(v0_limit*3.6)) +" km/h");



	    makeGlobalLayout();
 	    if(SHOW_TEXT){
	      textCanvas.update(choice_Szen);
	    }

	    cardLayout.show(pScrollbars,"Source");

	    simCanvas.newValues(choice_Szen, density, 
                      qIn, perTr, v0_limit, p_factor, deltaB,
                      tsleep_ms, CACC_rate);
	    simCanvas.start(choice_Szen,0.0);
	    return true;
	}

	if (event.target==button6){
	    stop();
	    choice_Szen=6;            // Lanechange slalom

	    p_factor=0.25;
	    deltaB=0.1;

	    sb_p_factor.setValue( (int)(100*p_factor));
	    label_p_factor.setText (String.valueOf(p_factor));

            sb_deltaB.setValue((int)(100*deltaB));
	    label_deltaB.setText (String.valueOf (deltaB)+" m/s^2");

            sb_simSpeed6.setValue((int)(100*Math.log(1./tsleep_ms)));
	    label_simSpeed6.setText(str_warpfactor+str_framerate);

	    makeGlobalLayout();
	    if(SHOW_TEXT){
		//      textCanvas.setSZ(1);
	      textCanvas.update(choice_Szen);
	    }
	    cardLayout.show(pScrollbars,"Lanechange");

	    simCanvas.newValues(choice_Szen, density, 
                      qIn, perTr, v0_limit, p_factor, deltaB,
                      tsleep_ms, CACC_rate);
	    simCanvas.start(choice_Szen,0.0);
	    return true;
	}

	else return super.action(event, arg);
    }

 



	// This initializes the variables of the Scroll bars.
    public void start(){
       System.out.println("MicroSim2_0.start()");
       if(choice_Szen!=2){
          simCanvas.newValues(choice_Szen, density, qIn, perTr, 
	                      v0_limit, p_factor, deltaB, tsleep_ms, CACC_rate);}
       else{
	  simCanvas.newValues2(qIn, perTr, p_factor, deltaB, qRamp, p_factorRamp, deltaBRamp, tsleep_ms, CACC_rate);
	    }      
       makeGlobalLayout();
       if(true){System.out.println("MicroSim2_0.start()");}
       if(false){
           System.out.print("start() => before simCanvas.start(choice_Szen=");
           System.out.println(choice_Szen+", density="+density+")"); 
       }
       simCanvas.start(choice_Szen,density);
    }



    public void stop(){
       System.out.println("MicroSim2_0.stop()");
       simCanvas.stop();
    }
    public void destroy(){
       stop();
    }
} 

