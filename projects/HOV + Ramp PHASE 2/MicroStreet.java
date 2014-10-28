import java.awt.*;
import java.util.Vector;  // container for arbitrary elements;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
/**
Representation of a 4-lanes road section for one direction.   
The main elements of MicroStreet are 
<ul>
<li> street, a vector of Moveable's representing the vehicles,
<li> The update method invoked in every time step. Among others, it calls all methods mentioned below.
<li> Methods for moving the vehicles (translate), accelerating them (accelerate) and performing the lane changes (changeLanes).
<li> A sorting routine sort for rearranging the vehicle order in street in the order of decreasing longitudinal positions
<li> The method ioFlow implementing the upstream and downstream boundary conditions (inflow and outflow).
</ul>
<br><br>
The realization of an on ramp, the class Onramp, is derived from this class.
*/

public class MicroStreet implements Constants{

  static final int LEFT=0; 
  static final int RIGHT=1;


    // vector of Moveables
    //final int IMAXINIT = (int)(0.001*RADIUS_M * 4 * Math.PI * DENS_MAX_INVKM + 10);
    final int IMAXINIT=100;
    protected Vector street = new Vector(IMAXINIT);

    // vector of data of Moveables for output (only model is missing)
    public Vector positions =  new Vector(IMAXINIT);
    public Vector velocities =  new Vector(IMAXINIT);
    public Vector numbers = new Vector(IMAXINIT);
    public Vector lanes = new Vector(IMAXINIT);
    public Vector colors =new Vector(IMAXINIT);
    public Vector lengths=new Vector(IMAXINIT);

    // additional vectors for output
    public Vector distances = new Vector(IMAXINIT);
    public Vector old_pos = new Vector(IMAXINIT);
    public Vector old_lanes = new Vector(IMAXINIT);
    public Vector old_numbers = new Vector(IMAXINIT);
    
    //to count the flow rate
    int exit__counter = 0;
    int imaxinit = (int)(0.001*RADIUS_M * 4 * Math.PI * DENS_MAX_INVKM + 200) * 5 ; // * 4 in order to fix the crash when we have too many agents in the system at one time (imax > imaxinit)
    int   [] nr = new int [imaxinit+1];
    
    // for flow calculation
    ArrayList list5=new ArrayList(); //  create the index of the cars reaching the flow exit point

	private MicroStreet microstreet;

    // info if cars are removed in closed system

    public boolean circleCarsRemoved=false;

	// For Randomness
	Random generator = new Random();

	int flowCounter = 0;
	
    


    // floating car data
    public double fcd = 0.0;            // distance
    public double fcvd = 0.0;           // approaching rate
    public double fcvel = 0.0;          // v
    public double fcacc = 0.0;          // acceleration
    public int fcnr=0;
    public boolean red=false;

    // longitudinal models; 
    // sync* types only needed for choice_Szen==4; defined there 
    protected MicroModel idmCar =new IDMCar();
    protected MicroModel idmCar2 =new IDMCar2();
    protected MicroModel idmTruck=new IDMTruck();
    protected MicroModel sync1Car;
    protected MicroModel sync2Car;
    protected MicroModel sync1Truck;
    protected MicroModel sync2Truck;

    // lane-change models  (p, db, smin, bsave)

    //!!! truck (=> vw for better impl!)
    protected LaneChange polite=new LaneChange(P_FACTOR_TRUCK, DB_TRUCK, MAIN_SMIN,MAIN_BSAVE, BIAS_RIGHT_TRUCK);


    //!!! car (=> vw for better impl!)
    protected LaneChange inconsiderate=new LaneChange(P_FACTOR_CAR, DB_CAR, MAIN_SMIN,MAIN_BSAVE, BIAS_RIGHT_CAR);

    // neighbours
    protected Moveable vL,vR,hL,hR;

    // can be modified interactively

    // mt apr05: seed 42 eingefuehrt wie in 3dsim 
    protected Random random=new Random(42); // for truck perc. and veh. numbers
    protected int    choice_Szen;
    protected int    choice_Geom; // circle or U
    protected double roadLength;          // length depends on choice_Szen 
    protected double uppos; // location where flow-conserving bottleneck begins
    

    // Source: nin = integral of inflow mod 1
    protected double nin=0.0;

    public MicroStreet(double length, double density, 
                       double p_factor, double deltaB,
		       int floatcar_nr, int choice_Szen){

	roadLength             = length;
	uppos            = 0.5*roadLength;
	this.choice_Szen = choice_Szen;
	this.choice_Geom = ((choice_Szen==1)||(choice_Szen==6)) ? 0 : 1;
	inconsiderate.set_p(p_factor);
	inconsiderate.set_db(deltaB);
	double mult=((choice_Geom==0)&&CLOCKWISE) ? (-1) : 1;
	polite.set_biasRight(mult*BIAS_RIGHT_TRUCK);
	inconsiderate.set_biasRight(mult*BIAS_RIGHT_CAR);

	fcnr             = floatcar_nr;

        //System.out.println("MicroStreet(args) cstr: roadLength="+roadLength);


	 

	// choice_Szen 4: Flow-conserving bottlenecks: feature in translate!
	// but initialize the 4 sync types here

	if(choice_Szen==4){
            sync1Car  = new IDMSync();
            sync2Car  = new IDMSyncdown();         // bottleneck region
            sync1Truck= new IDMTruckSync();
            sync2Truck= new IDMTruckSyncdown();    // bottleneck region

	    street.insertElementAt(new Car(5.0, sync1Car.Veq(roadLength), 0, sync1Car, polite, 
                       PKW_LENGTH_M, Color.red, 0), 0);
	}

    }
    

    // ################# end constructor #############################


    public double length(){ return roadLength; }
    public void setLength(double roadLength){
         this.roadLength = roadLength; }

    // make actual state available in form of vectors over all vehicles;
    // protected methods set public vectors to be used for graphical output
	protected Vector setPos()
	{
		Vector temp  = new Vector(IMAXINIT);
		int    imax = street.size();
		for (int i=0; i<imax; i++)
		{
			double pos= ((Moveable)(street.elementAt(i))).position();
			temp.insertElementAt( new Double(pos), i);
		}
		return temp;
	}

	protected Vector setDistances()
	{  // neglect gaps in front of first/last veh
		// on either lane (i<=iFrontCarsBoth)
		Vector temp  = new Vector(IMAXINIT);
		int    imax = street.size();

		int iFirstLeft = firstIndexOnLane(0);
		int iFirstRight = firstIndexOnLane(1);
		int iFirstLeft2 = firstIndexOnLane(3);
		int iFirstRight2 = firstIndexOnLane(2);
		
		int iFrontCarsBoth = geo_first(iFirstLeft, iFirstRight, iFirstLeft2, iFirstRight2);
		//int iFrontCarsBoth = (iFirstLeft>iFirstRight) ? iFirstLeft : iFirstRight;
		
		for (int i=0; i<= iFrontCarsBoth+1; i++)
		{  
			// placeholder
			temp.insertElementAt( new Double(-1.), i);
		}
		for (int i=iFrontCarsBoth+1; i<imax; i++)
		{
			int lane = ((Integer) lanes.elementAt(i)).intValue();
			int iFront = nextIndexOnLane(lane, i);
			double distance = ((Double) positions.elementAt(iFront)).doubleValue()- ((Double) positions.elementAt(i)).doubleValue();
			temp.insertElementAt( new Double(distance), i);
		}
		return temp;
	}

	
	public int geo_first(int iFirstLeft,int iFirstRight,int iFirstLeft2,int iFirstRight2)
	{
		int result;

		if(iFirstLeft>iFirstRight && iFirstLeft > iFirstLeft2 && iFirstLeft > iFirstRight2) 
		 {
			result = iFirstLeft;
			return result; 
		 }
	
		if(iFirstLeft2>iFirstRight && iFirstLeft2 > iFirstLeft && iFirstLeft2 > iFirstRight2) 
		{
			result = iFirstLeft2;
			return result; 
		}
		
		if(iFirstRight>iFirstLeft && iFirstRight > iFirstLeft2 && iFirstRight > iFirstRight2) 
		{
			result = iFirstRight;
			return result; 
		}
		else
		{
			result = iFirstRight2;
			return result; 
		}
	
	
	}


	protected Vector setNr()
	{
		Vector temp=new Vector(IMAXINIT);
		int imax=street.size();
		for (int i=0; i<imax; i++)
		{
			int nr =((Moveable)(street.elementAt(i))).NR(); 
			temp.insertElementAt(new Integer(nr), i);
		}
		return temp;
	}

	protected Vector setVel()
	{
		Vector temp=new Vector(IMAXINIT);
		int imax=street.size();
		for (int i=0; i<imax; i++)
		{
			double vel =((Moveable)(street.elementAt(i))).velocity(); 
			temp.insertElementAt(new Double(vel), i);
		}
		return temp;
	}

	protected Vector setLanes()
	{
		Vector temp=new Vector(IMAXINIT);
		int imax=street.size();
		for (int i=0; i<imax; i++)
		{
			//MUST CHANGE HERE
			int lane =((Moveable)(street.elementAt(i))).lane();
			//int lane = generator.nextInt( 4 ); // cars keep changing positions randomly	

			temp.insertElementAt(new Integer(lane), i);
		}
		return temp;
	}

	protected Vector setColors()
	{
		Vector temp=new Vector(IMAXINIT);
		int imax=street.size();
		for (int i=0; i<imax; i++)
		{
			Color c =((Moveable)(street.elementAt(i))).color();
			temp.insertElementAt(c, i);
		}
		return temp;
	}

	protected Vector setLengths()
	{
		Vector temp=new Vector(IMAXINIT);
		int imax=street.size();
		for (int i=0; i<imax; i++)
		{
			Double Len =new Double(((Moveable)(street.elementAt(i))).length());
			temp.insertElementAt(Len, i);
		}
		return temp;
	}


	public void update(double dt, int choice_Szen,
		double density, double qIn, double perTr, 
		double p_factor, double deltaB, double CACC_rate)
	{

		// used in SimCanvas.java

		old_pos=setPos();        // need old info for detectors and drawing
		old_lanes=setLanes();  
		old_numbers=setNr();  
		if (choice_Szen==3)
		{    // particularly aggressive for lane closing
			inconsiderate.set_p(0.);
			polite.set_db(DB_TRUCK3);
			inconsiderate.set_db(DB_CAR3);
			inconsiderate.set_bsave(6.);
		}
		else
		{
			inconsiderate.set_p(p_factor);
			polite.set_db(DB_TRUCK);
			inconsiderate.set_db(DB_CAR);
		}
		// choice_BC=0: per. BC; otherwise open BC
		int choice_BC=((choice_Szen==1)||(choice_Szen==6)) ? 0 : 1;

		// Main action: accelerate, changeLanes, translate, sort!
		// At least one vehicle must be on each lane

		if (true)
		{
			//if ((firstIndexOnLane(0)>=0)&&(firstIndexOnLane(1)>=0)){
			insertBCCars(choice_BC);   // virtual boundary cars
			accelerate(dt);
			changeLanes(dt);           // turn on/off overtaking
			clearBCCars();
		}
		
		translate(dt, choice_Szen);
		run_algorithm(dt, choice_Szen);
		//test();
		
		


		// For Statistical Analysis (Results)
	//	showResults();

		sort();

		positions=setPos();
		velocities=setVel();
		numbers=setNr();
		lanes=setLanes();

		colors=setColors();
		lengths=setLengths();
		distances = setDistances();

		// source terms !! export of positions etc before such that
		// old_positions and positions etc must 
		// always have same vehicle number!! -> extra var circleCarsRemoved

		

		ioFlow(dt, qIn, perTr, choice_BC, CACC_rate); //needs positions etc
		if(choice_Szen==1){ adaptToNewDensity(density,perTr,CACC_rate); }

		circleCarsRemoved = ((choice_Geom==0) && (street.size()<old_pos.size()));

	}


    // HIER truck => car implementieren!!

	protected void adaptToNewDensity(double density, double perTr, double CACC_rate)
	{
		int nCars_wished=(int)(density*roadLength*2.0);
		int nCars = positions.size();
		if (nCars_wished > nCars)
		{
			//System.out.println("nCars_wished="+nCars_wished+" nCars="+nCars);
		insertOneVehicle(perTr, CACC_rate);
		}
		if (nCars_wished < nCars)
		{
			//System.out.println("nCars_wished="+nCars_wished+" nCars="+nCars);
			removeOneVehicle();
		}
	}

    // Inserting cars in the model
    private void  insertOneVehicle(double perTr, double CACC_rate){

	// determine position and index of front veh
        int nveh=positions.size();
        final double mingap=10.;
        double maxgap=0.;
        int i_maxgap=0;
        double pos_maxgap; // position of vehicle which maxgap in front
        int lane_maxgap;   // lane of vehicle which maxgap in front

        int nleft=0;
        int nright=0;

		for (int i=0; i<nveh; i++)
		{
			Moveable me = (Moveable)(street.elementAt(i));
			if(me.lane()==LEFT){nleft++;}
			else{nright++;}
		//	System.out.println("i="+i+" lane="+me.lane()+" pos="+(int)me.position());

			double gap = ((Double)distances.elementAt(i)).doubleValue();
			if (gap>maxgap)
			{
				maxgap=gap; 
				i_maxgap=i;
			}
		}

		if(nleft<2)
		{
			//System.out.println("nleft<2!!!");
			maxgap=roadLength;
			pos_maxgap = 0;
			lane_maxgap =LEFT;
			i_maxgap=nveh;
		}
		else if(nright<2)
		{
			//System.out.println("nright<2!!!");
			maxgap=roadLength;
			pos_maxgap = 0;
			lane_maxgap = RIGHT;
			i_maxgap=nveh;
		}
		else
		{
			pos_maxgap = ((Double) positions.elementAt(i_maxgap)).doubleValue();
			lane_maxgap = ((Integer) lanes.elementAt(i_maxgap)).intValue();
		}
      //  System.out.println("MicroStreet.insertOneVehicle: maxgap="+(int)maxgap
        //                   + " index="+i_maxgap
          //                 + " pos="+pos_maxgap
            //               + " lane="+lane_maxgap);


	    // insert vehicle if sufficient gap
        if (maxgap>mingap){
           double     rand      = random.nextDouble()*1.0;
		   double     rand2     = random.nextDouble()*1.0;
	       int        randInt   = Math.abs(random.nextInt());
           MicroModel modelNew  = (rand<perTr) ? idmTruck :idmCar;  // HERE CREATE A NEW OJECT FOR EACH VEHICLE
           
            
           LaneChange changemodelNew =  (rand<perTr) ? polite : inconsiderate; // cars are inconsiderate and trucks are polite
           double     posNew    = pos_maxgap + 0.5 * maxgap;
           double     vNew      = modelNew.Veq(0.5*maxgap);
           double     lNew      = (rand<perTr) ? LKW_LENGTH_M : PKW_LENGTH_M;
           Color      colorNew  = (rand<perTr) ? Color.black : Color.red;


    ////// NEW CACC RELATED //////////
			if (colorNew == Color.red && rand2 <= CACC_rate)
			{
				colorNew  = Color.blue;
			 
			}
			
			if( colorNew  == Color.blue)
			{
				  street.insertElementAt((new Car(posNew, vNew, 3 ,modelNew, changemodelNew,lNew, colorNew, randInt)),i_maxgap);
				
			}
			////// NEW CACC RELATED //////////
			else
			{
				 street.insertElementAt((new Car(posNew, vNew, lane_maxgap, modelNew, changemodelNew, lNew, colorNew, randInt)),i_maxgap);
			}
	}

    }

	private void  removeOneVehicle()
	{
		int  indexToRemove = Math.abs(random.nextInt()) % (positions.size());
		street.removeElementAt(indexToRemove);
	}

	protected void insertBCCars(int choice_BC)
	{

		// virtual cars so that acceleration, lanechange etc 
		// always defined (they need in general all next neighbours)

		int i_rd=firstIndexOnLane(1);  // index right downstream vehicle
		int i_ld=firstIndexOnLane(0);  // index left  downstream vehicle
		int i_ru=lastIndexOnLane(1);  //  index right  upstream vehicle
		int i_lu=lastIndexOnLane(0);  //  index left   upstream vehicle
		//System.out.println("MicroStreet.insertBCCars: i_rd="+i_rd+" i_ru="+i_ru);

		double upLeftPos=(i_lu>-1) ? ((Moveable)(street.elementAt(i_lu))).position():0;
		double upRightPos=(i_ru>-1)? ((Moveable)(street.elementAt(i_ru))).position():0;    
		double upLeftVel=(i_lu>-1) ? ((Moveable)(street.elementAt(i_lu))).velocity():0;    
		double upRightVel=(i_ru>-1)? ((Moveable)(street.elementAt(i_ru))).velocity():0;

		double downLeftPos=(i_ld>-1) ? ((Moveable)(street.elementAt(i_ld))).position():roadLength;
		double downRightPos=(i_rd>-1)? ((Moveable)(street.elementAt(i_rd))).position():roadLength; 
		double downLeftVel=(i_ld>-1) ? ((Moveable)(street.elementAt(i_ld))).velocity():roadLength;    
		double downRightVel=(i_rd>-1)? ((Moveable)(street.elementAt(i_rd))).velocity():roadLength;

		if (choice_BC==0)
		{ // periodic BC
	
			street.insertElementAt(new BCCar(upLeftPos+roadLength, upLeftVel, 0, idmCar, PKW_LENGTH_M),0);
			street.insertElementAt(new BCCar(upRightPos+roadLength, upRightVel, 1, idmCar, PKW_LENGTH_M),0);
			int imax=street.size();
			street.insertElementAt(new BCCar(downLeftPos-roadLength, downLeftVel, 0, idmCar, PKW_LENGTH_M),imax);
			imax++;
		    street.insertElementAt(new BCCar(downRightPos-roadLength, downRightVel, 1, idmCar, PKW_LENGTH_M),imax);
		}
		if (choice_BC==1)
		{ // open BC
			double dx = 200.;  // distance of the boundary cars
			street.insertElementAt(new BCCar(downLeftPos+dx, downLeftVel, 0, idmCar, PKW_LENGTH_M),0);
			street.insertElementAt(new BCCar(downRightPos+dx, downRightVel, 1, idmCar, PKW_LENGTH_M),0);
			int imax=street.size();
			street.insertElementAt(new BCCar(upLeftPos-dx, upLeftVel, 0, idmCar, PKW_LENGTH_M),imax);
			imax=street.size();
			street.insertElementAt(new BCCar(upRightPos-dx, upRightVel, 1, idmCar, PKW_LENGTH_M),imax);
		}
	}

	// Lanes are set from 0 to 3 (4 total) from up (right/outer) til bottom (left/inner)
	protected void changeLanes(double dt)
	{
		final double bsave=5.;  // maximum save braking deceleration
		final double dmin=5.;   // minimum distance necessary to change
		int imax=street.size()-1;
		Moveable fOld, fNew, bNew;

		for (int i=2; i<imax; i++)
		{
			Moveable me = (Moveable)(street.elementAt(i));
			//System.out.println("---------------------- dt: -------------"+ dt);

			if (me.timeToChange(dt))
			{
				int lane = me.lane();
		
				//int      newLane  = ((lane==0) ? 1 : 0);
			    int newLane = testLane2(lane);

				setNeighbours(i,newLane);                   // -> vR, hR, vL, hL
				
				if( me.lane() == 0 || me.lane() == 1)
				{
					fOld = vL; // front vehicle own lane
					fNew = vR; // front vehicle new lane
					bNew = hR; // back vehicle new lane
				}
				
				else
				{
					fOld = (lane==2) ? vL : vR; // front vehicle own lane
					fNew = (lane==2) ? vR : vL; // front vehicle new lane
					bNew = (lane==2) ? hR : hL; // back vehicle new lane
				
				}

				 
				 
				if(me.color() == Color.blue)
				{
					// do nothing - no changing lanes for CACC vehicles
				}
				else
				{
					if(me.change (fOld, fNew, bNew)) // if changing lane is allowed (from LaneChange.java changeOK function)
					{
						((Moveable)(street.elementAt(i))).setLane(newLane);
					}	
				}
				
			}
		}
	}

	protected void accelerate(double dt)
	{
		int imax=street.size()-2;

		// floating car data
		fcd=0.0; 
		fcvd=0.0;
		fcvel=0.0;
		fcacc=0.0;

		// Counting loop goes backwards to implement parallel update!
		for (int i=imax-1; i>=2; i--)
		{
			Moveable me       = (Moveable)street.elementAt(i);
			int      lane     = me.lane();
			int      act_nr   = me.NR();
			int      next_ind = nextIndexOnLane(lane, i);
			Moveable frontVeh = (Moveable)street.elementAt(next_ind);

			// if actual car = floating car, gather "detector data"
			if (act_nr==fcnr)
			{ 
				fcd   = frontVeh.position()-me.position();
				fcvd  = frontVeh.velocity()-me.velocity();
				fcvel = me.velocity();
				me.accelerate(dt, frontVeh);
				double vNew=me.velocity();
				fcacc = (vNew-fcvel)/dt;
			}

				// Otherwise, do just the acceleration

			else 
			{
				me.accelerate(dt,  frontVeh);
			}
		}
	}

	protected void clearBCCars()
	{

		street.removeElementAt(0);
		street.removeElementAt(0);
		int imax=street.size();
		imax--;
		street.removeElementAt(imax);
		imax--;
		street.removeElementAt(imax);
	}

	protected void test()
	{
		// without sorting
		int imax=street.size()-1;
		for (int i=imax; i>=0; i--)
		{
			Moveable me  = (Moveable)street.elementAt(i);
			
			if(i == 2)
			{ 
				Moveable me2  = (Moveable)street.elementAt(2);
				me2.setColor(Color.yellow);			
			}
	
		}
	}

	protected void run_algorithm(double dt, int choice_Szen)
	{
		
		// without sorting
		int imax=street.size();
		for (int i=0; i<imax; i++)
		{
			Moveable me  = (Moveable)street.elementAt(i);
			
			
			
			// Scenario 2: written by Georges Arnaout  - CONTROLS THE CACC cars (CACC ALGORITHM)
			///////////////////////////////////////////////////////// BEGINING OF CODE //////////////////////////////	
			if(choice_Szen==2)
			{ 
				
				Moveable tempo_car  = (Moveable)street.elementAt(i);
						
				if((((Moveable)(street.elementAt(i))).color() == Color.blue))
				{
					
					//1- Get speed of intelligent car  			
					double mySpeed = ((Moveable)street.elementAt(i)).velocity();   // the speed of this intelligent car
					//System.out.println("---------------------- Intelligent Car #: -------------"+ i);
					int      myLane     = ((Moveable)street.elementAt(i)).lane();  // the lane on which is this car is on
					int      myNR       = ((Moveable)street.elementAt(i)).NR();
					//System.out.println("---------------------- Int Car is on lane: -------------"+ myLane);
					
					//2- Get speed of car ahead 
					int    next_ind = nextIndexOnLane(myLane, i);
					double frontSpeed = ((Moveable)street.elementAt(next_ind)).velocity();// the speed of the vehicle directly in front of the intelligent vehicle
					//System.out.println("---------------------- Front Car #: -------------"+ next_ind);
					double realSpeed = (mySpeed * 3600 )/1000 ;
					//double safety_gap = 0.5 * ((realSpeed * 1000)/3600);
					double safety_gap = 0.5 * mySpeed;
					//System.out.println("---------------------- TTTTTTTTTTESSSSSTTTTT : -------------"+ safety_gap );
					

					//3- Get separating distance between intelligent car and car ahead
					//double separatingDistance = ((Double) positions.elementAt(next_ind)).doubleValue()- ((Double) positions.elementAt(i)).doubleValue();
					double separatingDistance = ((Moveable)street.elementAt(i)).position() - ((Moveable)street.elementAt(next_ind)).position();
					//System.out.println("---------------------- Separating Distance: -------------"+ separatingDistance);
					//System.out.println("---------------------- and my speed: -------------"+ realSpeed);
					//double testing = idmCar.Veq(roadLength);
					//System.out.println("---------------------- position: -------------"+ ((Moveable)street.elementAt(i)).position()); // ranging between 0 and 2282.64
					
					//4- Change speed of blue car/ CACC vehicle (increase or decrease)
					
					///////////////// ACC ALGORITHM /////////////////////////////////////////////////////////PHASE I ////////////////////////
					if(mySpeed < frontSpeed && separatingDistance <= 120 && ((Moveable)(street.elementAt(next_ind))).color() != Color.blue  // in fact ACC sensor range is up to 120 meters (ref http://www.usautoparts.net/bmw/technology/acc.htm) 
						&& myLane ==((Moveable)(street.elementAt(next_ind))).lane())
					{
						((Moveable)street.elementAt(i)).setVelocity(mySpeed - 0.0133); // slow down by x (to be determined later and optimized)  
					}
					else if(((Moveable)(street.elementAt(next_ind))).color() == Color.blue)  // precedent car is a CACC so form platoon (if platoon is smaller than 5)
					{
						 
				       ((Moveable)street.elementAt(i)).setModel(idmCar2);  
				     //  System.out.println("MicroStreet.ioFlow: idmCar.v0="+((IDMCar)(idmCar)).v0);
					    // To make speed here equal to the speed of car in front at all times
				       if(mySpeed < frontSpeed && separatingDistance <= safety_gap && myLane ==((Moveable)(street.elementAt(next_ind))).lane())
				    		   {
				    	         ((Moveable)street.elementAt(i)).setVelocity(frontSpeed);
				    	         
				    		   }
				       else
				       {
				    	   ((Moveable)street.elementAt(i)).setVelocity(mySpeed + 0.0133); // to approach the cacc vehicle   
				    	   
				       }
				         //System.out.println("---------------------- Gap: -------------"+ separatingDistance);
					} 
					else
					{
						((Moveable)street.elementAt(i)).setVelocity(mySpeed + 0.0133);  // increase speed by x (to be determined later and optimized)
					}

					// INTELLIDRIVE ALGORITHM /////////////////////////////////////////////////////////PHASE II //////////////////////// NOT USED IN CACC

					// Get speeds of cars in the range of 40 meters
					double range_of_detection = ((Moveable)street.elementAt(i)).position() + 50.;  // the range of detection , might be changed later - optimized
					double minimum_speed = mySpeed; // initial minimum speed - to be changed
					double tempo_speed = mySpeed;
					int next = 0;					 


				}// if color == blue
				
				 
				
			} //choice_Szen==2

			///////////////////////////////////////////////////////// END OF CODE //////////////////////////////	
		
		}
		
	}




	protected int translate(double dt, int choice_Szen)
	{

		// without sorting
		int imax=street.size();
		for (int i=0; i<imax; i++)
		{
			Moveable me  = (Moveable)street.elementAt(i);
			double x_old = me.position();
			me.translate(dt);
			double x_new = me.position();
		
			 

		 

			if (choice_Szen==4)
			{
				if ((x_old<=uppos)&&(x_new>uppos))
				{
					if (me.model()==sync1Truck)
					{
						me.setModel(sync2Truck);	
					}
					else
					{
						me.setModel(sync2Car);
					}
				}
			}
		}
		return street.size();
	}



	public void showResults()
	{
		int imax=street.size();
		double time1, time2, avg_time_spent;
		//int myIndex;
		double average_speed = 0;
		double average_dist = 0;
		double Variance = 0;
		double total = 0.; // to sum the total speeds to subsequently obtain the average speed
		double total2 = 0.; // to sum the total sep distances to subsequently obtain the average sep distance (safety gap)
		double sum =0.;
		double denominator = 0;  // for the variance
		double numerator = 0;    // for the variance

		for (int i=0; i<imax; i++) // for the average
		{		
			double mySpeed    = ((Moveable)street.elementAt(i)).velocity();
			double myPosition = ((Moveable)street.elementAt(i)).position();
			Moveable me  = (Moveable)street.elementAt(i);
			//nr[i]        = ((Integer) numbers.elementAt(i)).intValue();
			//myIndex = nr[i];            // Get the car's index (to avoid looping repetitions)

			
			//System.out.println("---------------------- myPosition:  -------------" + myPosition);
			total += mySpeed;
			average_speed = total/imax;
		
			// the TIME is called from SimCanvas.java (line 1414)
		
		}
		
		

		for (int i=0; i<imax; i++) // for the variance
		{
			double mySpeed    = ((Moveable)street.elementAt(i)).velocity();
			double myPosition = ((Moveable)street.elementAt(i)).position();
			Moveable me  = (Moveable)street.elementAt(i);
			// Variance of the speeds
			denominator = imax -1;
			numerator = square(((Moveable)street.elementAt(i)).velocity() - average_speed  ); // numerator without the sum
        //	System.out.println("---------------------- my speed: -------------"+ ((Moveable)street.elementAt(i)).velocity());
			sum += numerator;
			
			
			//FLOW GENERATION
			if( (int)myPosition >= 2650  && (int)myPosition  <= 2850 )       // Flow collecting point (exit of flow)
			{
				if( list5.contains( me )  )  
				{
					break;
				}					
			
			   else
			   {
				//System.out.println("---------GEORGY-----------");
				list5.add(me); //add indexes of exiting agents to list5 array
				
				 
				
				// Count the number of exiting vehicles
				exit__counter++;
			   }
		    }
			
		}
		
		// calculate the Variance
		Variance = sum / denominator;	
				
		System.out.println("---------------------- Average Speed: ------------------"+ average_speed);
		System.out.println("---------------------- Variance: ------------------"+ Variance);
		System.out.println("---------------------- Number of Vehicles: -------------"+ imax );
		System.out.println("---------------------- Number of agents that exited: "+exit__counter);
	}



	public double square (double n) 
	{
		return java.lang.Math.pow(n,2);
	}


	protected void ioFlow(double dt, double qIn, double perTr, int choice_BC, double CACC_rate)
	{

		// periodic BC

		if (choice_BC==0)
		{

			
			int imax=street.size()-1;
			if (imax>=0)
			{ // at least one vehicle present
				Moveable temp_car= (Moveable) street.elementAt(0);
				while (temp_car.position()>roadLength)
				{
					double pos = temp_car.position();

					// remove first vehicle
					street.removeElementAt(0);
					 

					// and insert it at the end with position reduced by roadLength
					temp_car.setPosition(pos-roadLength);
					imax=street.size();
					street.insertElementAt(temp_car,imax);
					temp_car= (Moveable) street.elementAt(0);
				}
			}
		}


		// open BC
		if (choice_BC==1)   // This is active during all my simulation
		{ 
			
			int imax=street.size()-1;
			final double spaceFree=200.;  // v0=ve(spaceFree)
			final double spaceMin=20; // minimum headway for new vehicle
			//System.out.println("MicroStreet.ioFlow: Anfang: imax="+imax);


			// outflow (exiting the system)
			if (imax>=0)
			{
				// System.out.println("MicroStreet.ioFlow: removing vehicle...");
				while ( (imax>=0) &&  // (imax>=0) first to prevent indexOutOf...!
					( ((Moveable)(street.elementAt(0))).position()>roadLength))
				{
					street.removeElementAt(0);
					//System.out.println("AGENT REMOVED / EXITED");
					imax--;
				}
			}

			// inflow
			//System.out.println("MicroStreet.ioFlow: idmCar.v0="+((IDMCar)(idmCar)).v0);
			//System.out.println("MicroStreet.ioFlow: begin inflow: imax="+imax);
			 

			// THIS IS THE NUMBER OF VEHICLES PER SECOND according to what we defined in Main Inflow 
			// for example, if qIn = 0.13 => Inflow = (0.13)*60*60 = 500 Vehicles/hour as inflow
			// dt = 0.25
			nin=nin+qIn*dt; // this regulates the traffic according to the gap
			
			
            //System.out.println("the value of nin is: "+nin);
			if (nin>1.0)
			{  // new vehicle imposed by the inflow BC
					
				nin=nin-1.0; // COMMENT OUT TO STOP THE REGULATOR implemented by Treiber
				
				int i_lu=lastIndexOnLane(0);    // lu = "left right"
				int i_ru=lastIndexOnLane(1);    // below it
				//System.out.println("test: "+i_ru);
				int i_lu2 = lastIndexOnLane(3); // below it
				int i_ru2 = lastIndexOnLane(2); // all the way down
				//System.out.println("test: "+i_ru2);


				// at least 1 vehicle on each of the 4 lanes
				if ((i_lu>=0) && (i_ru>=0) && (i_lu2>=0) && (i_ru2>=0))
				{  
					imax=street.size()-1;
					int laneLastVeh = ((Moveable)(street.elementAt(imax))).lane();
					//System.out.println("Last Vehicle: "+laneLastVeh);
					
					//					int lane  = (laneLastVeh ==0) ? 1 : 0;  // insert on other lane
					//					int iPrev = lastIndexOnLane(lane);      // index of previous vehicle // DOES NOT ACCEPT 2 or 3 WHY?

					//int lane = testLane2(laneLastVeh);       // test on 4 lanes
					int lane = geo_generator(laneLastVeh);       // test on 4 lanes
					int iPrev = lastIndexOnLane(lane);      // index of previous vehicle // DOES NOT ACCEPT 2 or 3 WHY?
				
					
					//System.out.println("iPrev: "+iPrev);
					double space =((Moveable)(street.elementAt(iPrev))).position();
					double vPrev =((Moveable)(street.elementAt(iPrev))).velocity(); 

					// enough space for new vehicle to enter? (!red)
					if (!(red=(space<spaceMin)))
					{
						MicroModel carmodel  =(choice_Szen!=4) ? idmCar : sync1Car;
						MicroModel truckmodel=(choice_Szen!=4) ? idmTruck : sync1Truck;
						double     rand      = random.nextDouble()*1.0;
						double     rand2      = random.nextDouble()*1.0;
						int        randInt   = Math.abs(random.nextInt());
						MicroModel modelNew  = (rand<perTr)? truckmodel : carmodel;
						LaneChange changemodelNew = (rand<perTr)? polite : inconsiderate;
						double     vNew      = modelNew.Veq(space);

						    MicroModel neo_idmCar =new IDMCar();
						    MicroModel neo_idmCar2 =new IDMCar2();
						    MicroModel neo_idmTruck=new IDMTruck();

				           
				           if (rand < perTr)
				           {
				        	   
				        	  modelNew  = idmTruck; 
				           }
				           else
				           {
				        	  
				        	   modelNew  = neo_idmCar;  // HERE CREATE A NEW OJECT FOR EACH VEHICLE
				           }
						
						if(false)
						{
							//System.out.println("MicroStreet.ioFlow: "IDM.initialize():  	+((rand<perTr) ? "Truck" : "Car") + ", vNew="+vNew);
						}

						double     lNew      = (rand<perTr) ? LKW_LENGTH_M : PKW_LENGTH_M;
						Color      colorNew  = (rand<perTr) ? Color.black : Color.red;

					////// NEW CACC RELATED //////////
						if (colorNew == Color.red && rand2 <= CACC_rate)
						{
							colorNew  = Color.blue; // all details are in the CACC algorithm section
							 
						}
						imax=street.size();
						
						if( colorNew == Color.blue)
						{
						 street.insertElementAt(new Car(0.0, 29, 3, modelNew, changemodelNew, lNew, colorNew, randInt),imax);
						}
						////// NEW CACC RELATED //////////
						else
						{
						  street.insertElementAt(new Car(0.0, 29, lane, modelNew, changemodelNew, lNew, colorNew, randInt),imax);
						}
						////// NEW CACC RELATED //////////

						//System.out.println("Index:  "+randInt);
						//street.insertElementAt(new Car(0.0, idmCar.Veq(roadLength), lane, modelNew, changemodelNew, lNew, colorNew, randInt),imax);
				        
						// Works well up to 5500/hr
						//street.insertElementAt(new Car(0.0, idmCar.Veq(roadLength), generator.nextInt( 4 ), modelNew, changemodelNew, lNew, colorNew, randInt),imax);
					}
				}
				
					// at least one lane without vehicles
				else
				{  
					//double test = CACC_rate;

					MicroModel carmodel  =(choice_Szen!=4) ? idmCar : sync1Car;
					MicroModel truckmodel=(choice_Szen!=4) ? idmTruck : sync1Truck;
					double     rand      = random.nextDouble()*1.0;
					double     rand2     = random.nextDouble()*1.0;
					int        randInt   = Math.abs(random.nextInt());
					MicroModel modelNew  = (rand<perTr) ? truckmodel : carmodel;
					double     vNew      = modelNew.Veq(spaceFree);
					double     lNew      = (rand<perTr) ? LKW_LENGTH_M : PKW_LENGTH_M;
					Color      colorNew  = (rand<perTr) ? Color.black : Color.red;
		
			   
					 
					
					////// NEW CACC RELATED /////////////////////////
					if (colorNew == Color.red && rand2 <= CACC_rate)
					{
						colorNew  = Color.blue;
						//   carmodel = idmCar2;
					}
					////// NEW CACC RELATED /////////////////////////

				
					int lane=(i_lu<0) ? 0 : 1;
					//System.out.println("Lane is: "+lane);
					//int lane = generator.nextInt( 4 );

					imax=street.size();
					//System.out.println("street.size()="+street.size());
					street.insertElementAt (new Car(0.0, vNew, generator.nextInt( 4 ), modelNew,inconsiderate, lNew, colorNew, randInt),imax);
					// street.insertElementAt (new Car(0.0, vNew, generator.nextInt( 4 ), modelNew,inconsiderate, lNew, colorNew, randInt),imax);
					//street.insertElementAt(new Car(0.0, idmCar.Veq(roadLength), generator.nextInt( 4 ), modelNew, changemodelNew, lNew, colorNew, randInt),imax);
					
					 
				}
			}
		}
		//      System.out.println("ioFlow end: imax="+(street.size()-1));

	}

    // Sort to decreasing values of pos using the bubblesort algorithm;
    // Pairwise swaps running over all vehicles; 
    // repeat loop over vehicles until
    // sorted; typically, only 2 runs over the loop are needed
    // (one to sort; one to check)

	
	
	public int geo_generator(int laneLastVeh)   // could be improved
	{
		int lane = 0;

		if (laneLastVeh == 3)
		{
			int randoming = generator.nextInt( 3 ); //  random between 0, 1, and 2
			lane = randoming;
		}
	
		if (laneLastVeh == 2)
		{
			int randoming = generator.nextInt( 90 ); //  random between 0 and 1
			
			if (randoming < 30)
			{
				lane = 0;
			}

			if (randoming >= 30 && randoming < 60)
			{
				lane = 1;
			
			}

			if (randoming >= 60 && randoming < 90)
			{
				lane = 3;
			}		
		}
	    
		if (laneLastVeh == 1)
		{
			int randoming = generator.nextInt( 89 ); //  random between 0 and 1
			
			if (randoming < 30)
			{
				lane = 0;
			}

			if (randoming >= 30 && randoming < 60)
			{
				lane = 2;			
			}

			if (randoming >= 60 && randoming < 90)
			{
				lane = 3;
			}
			
		}

		if (laneLastVeh == 0)
		{
			int randoming = generator.nextInt( 89 ); //  random between 0 and 1
			
			if (randoming < 30)
			{
				lane = 1;
			}

			if (randoming >= 30 && randoming < 60)
			{
				lane = 2;
			}

			if (randoming >= 60 && randoming < 90)
			{
				lane = 3;
			}
		}
	
		return lane;
	}

	public int testLane2(int laneLastVeh)   // could be improved
	{
		if (laneLastVeh == 0)
		{
			int lane = 1;  
			return lane;
		}

		else if (laneLastVeh == 1)
		{
			int randoming = generator.nextInt( 2 ); //  random between 0 and 1
			int lane;

			if(randoming == 0)
			{
				lane = 0;
				return lane;
			}
			else 
			{
				lane = 2;
				return lane;
			}
			
		}

		else if (laneLastVeh == 2)
		{
			int randoming = generator.nextInt( 2 ); //  random between 0 and 1
			int lane;

			if(randoming == 0)
			{
				lane = 1;
				return lane;
			}
			else 
			{
				lane = 3;
				return lane;
			}
			
		}

		else 
		{
			int lane = 2;
			return lane;
						
		}

		
	}


	public int testLane(int laneLastVeh)
	{
		if (laneLastVeh == 0)
		{
			int lane = 1; // add one more
			return lane;
		}

		else if (laneLastVeh == 1)
		{
			int lane = 0;  // change to 0 or 3
			return lane;
		}

		else if (laneLastVeh == 2)
		{
			int lane = 3;
			return lane;
		}

		else 
		{
			int lane = 2;
			return lane;
						
		}

		
	}

	protected void sort()
	{ 
		boolean sorted=false;

		while (!sorted)
		{

			sorted = true;
			int imax = street.size();
			for (int i=1; i<imax; i++)
			{
				double p_back=((Moveable)street.elementAt(i)).position();
				double p_front=((Moveable)street.elementAt(i-1)).position();
				if (p_back>p_front)
				{
					sorted=false;
					Moveable temp=(Moveable) street.elementAt(i-1);
					street.setElementAt((Moveable) street.elementAt(i), i-1);
					street.setElementAt(temp,(i));
				}
			}	    
		}
	}

    // returns index of first (most downstream) vehicle on given lane;
    // if no vehicles on this lane; -1 is returned

	protected int firstIndexOnLane(int lane)
	{
		int nr_max=(street.size())-1;
		int i=0;
		boolean carFound = false;
		if (nr_max>=0)
		{
			while ((i<=nr_max) && (!carFound))
			{
				if (((Moveable)street.elementAt(i)).lane()==lane)
				{
					//  if (((((Moveable)street.elementAt(i)).lane())==lane)&&(flag==0)){
					carFound = true;
				}
				//System.out.println 
				//("first-loop:"+(new Integer(i).toString()));
				i++;
			}
		}
		return ((carFound) ? i-1 : -1);
	}

 

    // returns index of most upstream vehicle on given lane **** this function is generic (accepts 4 lanes)
	protected int lastIndexOnLane(int lane)
	{
		int nr_max=(street.size())-1;
		int i=nr_max; // total number of cars
		
		boolean carFound = false;
		if (nr_max>=0)
		{
			while ((i>=0) && (!carFound))
			{
				//System.out.println("i  "+i);
				if (((Moveable)street.elementAt(i)).lane() == lane)
				{
					carFound = true;	
				}
				//System.out.println("i lane"+((Moveable)street.elementAt(i)).lane());
				//System.out.println("given lane"+lane);
				i--;
				//System.out.println("i counter"+i);
			}
		}
		//System.out.println("i counter"+i+1);
		return ((carFound) ? i+1 : -1);
		
	}

    // !! bounds not checked
	protected int nextIndexOnLane(int lane, int ind)
	{
		//textarea.setText("In nextIndexOnLane");
		int next_ind;
		
		if (ind > 0)
		{
			next_ind =ind-1;
		}
		else
		{
			next_ind = 0;
		}
		//System.out.println("Lane"+lane);
		//System.out.println("looper:"+ind);
		
		while ((((Moveable)street.elementAt(next_ind)).lane())!=lane && next_ind > 0)
		{		
			next_ind--;
		//	System.out.println("next_ind:"+next_ind);
		}
		return next_ind;
	}

    // !! bounds not checked
	protected int prevIndexOnLane(int lane, int ind)
	{
		int imax = street.size();
		//textarea.setText("In nextIndexOnLane");
	//	System.out.println("Lane"+lane);
		int next_ind=ind+1;
		//System.out.println("Next Index"+next_ind);
		while ((((Moveable)street.elementAt(next_ind)).lane())!=lane && next_ind < imax - 1)
		{
			next_ind++;
		//	System.out.println("Next Index in loop"+next_ind);
		}
		return next_ind;
	}

    // !! assumed that neighbours are existent; otherwise OutOfBoundsException

	
	public static float Round(float Rval, int Rpl) {
		  float p = (float)Math.pow(10,Rpl);
		  Rval = Rval * p;
		  float tmp = Math.round(Rval);
		  return (float)tmp/p;
		    }
	
	

	protected void setNeighbours(int ind, int newLane)  // modified by Georges Arnaout to accomodate 4 lanes (instead of 2)
	{
		Moveable me = (Moveable)(street.elementAt(ind));
		
		//int vl, vr, hl, hr;
		int vl=nextIndexOnLane(0, ind);
		int vr=nextIndexOnLane(1, ind);
		int hl=prevIndexOnLane(0, ind);
		int hr=prevIndexOnLane(1, ind);

		if ( me.lane() == 0)  // newLane ==1
		{
			vl=nextIndexOnLane(0, ind);
			vr=nextIndexOnLane(1, ind);
			hl=prevIndexOnLane(0, ind);
			hr=prevIndexOnLane(1, ind);
		}
		
		
		else if ( me.lane() == 1 )
		{
			//int randomly = generator.nextInt( 2 ); //  random between 0 and 1
			
			
			if (newLane == 0)
			{
				vr=nextIndexOnLane(0, ind);
				vl=nextIndexOnLane(1, ind);
				hr=prevIndexOnLane(0, ind);
				hl=prevIndexOnLane(1, ind);
			}

			else // newLane ==2
			{
				vl=nextIndexOnLane(1, ind);
				vr=nextIndexOnLane(2, ind);
				hl=prevIndexOnLane(1, ind);
				hr=prevIndexOnLane(2, ind);
			}

		}
 

		else if( me.lane() == 2)
		{
			//int randomly = generator.nextInt( 2 ); //  random between 0 and 1
			
			if (newLane == 3)
			{
				vl=nextIndexOnLane(2, ind);
				vr=nextIndexOnLane(3, ind);
				hl=prevIndexOnLane(2, ind);
				hr=prevIndexOnLane(3, ind);
			}

			else // newLane == 1
			{
				vl=nextIndexOnLane(2, ind);
				vr=nextIndexOnLane(1, ind);
				hl=prevIndexOnLane(2, ind);
				hr=prevIndexOnLane(1, ind);
			}

		}

		else // newLane == 2
		{
		
			vl=nextIndexOnLane(2, ind);
			vr=nextIndexOnLane(3, ind);
			hl=prevIndexOnLane(2, ind);
			hr=prevIndexOnLane(3, ind);
		
		}


		vL = (Moveable) street.elementAt(vl);
		vR = (Moveable) street.elementAt(vr);
		hL = (Moveable) street.elementAt(hl);
		hR = (Moveable) street.elementAt(hr);
	}

    // remove the two white obstacles when traffic ight turns green
    // (only once in a simulation and only for choice_Szen==5)



    protected void open(){
	    street.removeElementAt(0);
	    street.removeElementAt(0);
    }
}
