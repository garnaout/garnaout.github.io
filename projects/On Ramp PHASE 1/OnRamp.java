import java.awt.*;
import java.util.Vector;  // container for arbitrary elements;
import java.util.Random;

public class OnRamp extends MicroStreet{


    private double rampLength;
    //   private double p_factorRamp;
    //   private double deltaBRamp;  
                    //  Lane-changing params to change to main  
                    // once on main road, use mainroad.inconsiderate
    private LaneChange laneChangeModel;
    private int choice_BC;
    private double mergingPos;
    private MicroStreet mainroad;
    private Moveable me, frontVeh;              // on on-ramp
    private Moveable frontVehMain, backVehMain; // on main road
    private int i_insertmain;


    public OnRamp(MicroStreet mainroad, double totalLength, 
                  double rampLength, double mergingPosAtMainroad, 
                       double p_factorRamp, double deltaBRamp, double CACC_rate){
    
	super(totalLength, 0., p_factorRamp, deltaBRamp, 0, 2);
	// -> roadlength=totalLength, choice_Szen=2,
	//    ramp-lanechanging parameters also in inconsiderate.b, -.db
	// (not used)
        laneChangeModel = new LaneChange(p_factorRamp, deltaBRamp);

	//  this.p_factorRamp = p_factorRamp;
	//  this.deltaBRamp = deltaBRamp;
	this.mainroad = mainroad;
	this.mergingPos = mergingPosAtMainroad; //centered (m)
	this.rampLength = rampLength;   //  (m)
	this.choice_BC = 1;      // open BC

    // set standing obstacles marking end of on-ramp
	street.addElement(new Obstacle(roadLength, 0, 1., 0));

	// start with one car at the beginning
	street.addElement
	      (new Car(5., idmCar.Veq(roadLength), 0, idmCar, inconsiderate, 
              PKW_LENGTH_M, Color.red, 2));
    }
    



    public void update(double dt, double qIn, double perTr,
           double p_factorRamp, double deltaBRamp, double CACC_rate){

	// used in SimCanvas.java

        laneChangeModel.set_p(p_factorRamp);
        laneChangeModel.set_db(deltaBRamp);

        old_pos=setPos();        // need old info for detectors and drawing

	accelerate(dt);
	translate(dt, choice_Szen);

	// no sink/source between determining positions and old pos etc!
	// -> no bookkeeping necessary for graph. output!

	positions=setPos();
	velocities=setVel();
	colors=setColors();
	lengths=setLengths();

	inFlow(dt, qIn, perTr); 
	mergeToMainRoad(laneChangeModel, mergingPos); 
    }


    protected void accelerate(double dt){

	int imax=street.size()-1;
	for (int i=imax; i>=1; i--){

            Moveable me       = (Moveable)street.elementAt(i);
            Moveable frontVeh = (Moveable)street.elementAt(i-1);

	    me.accelerate(dt,  frontVeh);
	}
    }


    private void mergeToMainRoad(LaneChange laneChangeModel,
               double mergingPos){
 
        final double bmax=3.;  // maximum safe braking deceleration
        final double dmin=5.;  // minimum distance necessary to change   // originally 5.
        final double offsetMain =  mergingPos - (roadLength-0.5*rampLength);
	int imax=street.size()-1;
	int nvehmain=mainroad.street.size();

	if(false){
        //System.out.println("in mergeToMainRoad: "+"1 obstacle + "+(imax) + " veh(s) on ramp, "+nvehmain + " on mainroad;"+" offset="+(int)(offsetMain));
	}

	if(imax>=1){  // at least one real vehicle on the ramp lane
         for (int i=1; i<=imax; i++){
          me = (Car)(street.elementAt(i)); 
          double x = me.position();
         
       //   System.out.println("POSITION >>>>>>>> "+ x);
       //  System.out.println("Distance to change >>>>>>> "+ (roadLength - rampLength));
          
          
          if (x > roadLength - rampLength){ // action only in merging region

	    //  System.out.println(" mergeToMainRoad: veh in ramp region!"+" i="+i+" x="+x);

          frontVeh = (Moveable)street.elementAt(i-1);
          
       //   System.out.println(" FRONT VEH >>>>>>" +frontVeh);
	      
          setNeighboursOnMainRoad (i, offsetMain); 
        // -> virtual vehicles frontVehMain, backVehMainfRamp, 
	    // position i_insertmain
	    //  positions are given in the ramp system!

		// do actual change if incentive criterion fulfilled
	    me.setLaneChange(laneChangeModel); // update lane-change params
	    if(me.change (frontVeh, frontVehMain, backVehMain)){

      
                
	           me.setLane(0); // right lane on future main road
               me.setPosition(me.position()+offsetMain);
               me.setLaneChange(mainroad.inconsiderate);
               mainroad.street.insertElementAt(me, i_insertmain);
               street.removeElementAt(i);
               imax--;
               
	      
	    }
	  }
	 }
	}

	/*
      System.out.println("End mergeToMainRoad:");
      System.out.println(" Vehicles on main road:");
      for (int i=0; i<mainroad.street.size(); i++){
         double pos = ((Moveable) mainroad.street.elementAt(i)).position();
         System.out.println("  i="+i+", pos="+pos);
      }
      System.out.println(" Vehicles on  on-ramp:");
      for (int i=0; i<street.size(); i++){
         double pos = ((Moveable) street.elementAt(i)).position();
         System.out.println("  i="+i+", pos="+pos);
      }
	*/
    }


    private void setNeighboursOnMainRoad(int i, double offsetMain){

       // -> virtual vehicles frontVehMain, backVehMainfRamp
       // whose positions are given in the ramp system, and index
       // i_insertmain at which car is considered to be placed on main road

       //boolean debug=(i==1);
       boolean debug=false;

       final double farDistance=10000+ offsetMain;
       double x = ((Moveable)(street.elementAt(i))).position();
       int    nvehmain = mainroad.street.size();    // Street size is another way of counting the number of agents in the system
       int    imain = 0;
       double xmain = farDistance;
       int    lanemain;
       int    i_frontmain;
       int    i_backmain;

       if (nvehmain >0) {
	  
	   // determine index of last vehicle in front of ramp vehicle on main road (regardless of lane!)
         for (imain=0; ((imain<nvehmain) && (x<xmain)); imain++) ///////////// check this here
           {
              xmain = ((Moveable)(mainroad.street.elementAt(imain))).position()  - offsetMain;
              //System.out.println("XMAIN >>>>>>>"+xmain); // gives from - to +
              //System.out.println("X     >>>>>>>"+x);     // position of ramp vehicles

	         if(debug){
	     // 	 System.out.println("OnRamp:setNeighbours..for loop: imain="+imain
	     //   +" xmain="+((int)(xmain))+" nvehmain="+nvehmain);
	                  }
           }

           i_frontmain=imain-2; //!!! imain-2
           i_backmain=imain-1;   //!!! imain-1
           i_insertmain=imain-1; //!!! imain-1

	

	   // determine front vehicle on right (sic!) main lane (right=0)
	   // if no vehicle(s), i_frontmain=-1 and/or i_backmain=nvehmain
          lanemain = (i_frontmain>=0) ? ((Moveable)(mainroad.street.elementAt(i_frontmain))).lane(): 1; //////// <<<<<<<<<<<<<<<<<<<<<<< check this

           while ((i_frontmain>=0) && (lanemain==0)){
              i_frontmain--;
              lanemain = (i_frontmain>=0) ? ((Moveable)(mainroad.street.elementAt(i_frontmain))).lane() : 1;
	   }

	       //determine back vehicle on right (sic!) main lane (right=0)
           lanemain = (i_backmain<nvehmain) ? ((Moveable)(mainroad.street.elementAt(i_backmain))).lane(): 1;

            while ((i_backmain<nvehmain) && (lanemain==0)){
              i_backmain++;
              lanemain = (i_backmain < nvehmain)? ((Moveable)(mainroad.street.elementAt(i_backmain))).lane(): 1;
	    }
	   
       }

       else{ // nvehmain=0
	 
         i_frontmain = -1;
         i_backmain  = -1;
       }

       // define virtual cars.
       // must copy cars (new ...) because otherwise (pointer assignment)
       // offset action below would offset streets on mainroad!

       frontVehMain = (i_frontmain<0) // only back vehicle(s)
          ? new Car(farDistance, 0, 0, 
            idmCar, inconsiderate, 5, Color.red, 0)
          : new Car( (Car)(mainroad.street.elementAt(i_frontmain)));


       backVehMain = ((nvehmain<1)||(i_backmain >= nvehmain)) 
	      ? new Car(-farDistance, 0, 0,  // only front veh(s) or none
            idmCar, inconsiderate, 5, Color.red, 0)
          : new Car( (Car)(mainroad.street.elementAt(i_backmain)));
       if(debug){
       
       }
       // adjust positions to ramp system

       frontVehMain.setPosition(frontVehMain.position()-offsetMain);
       backVehMain.setPosition(backVehMain.position()-offsetMain);

       
    } // end setNeighboursOnMainRoad


    private void inFlow(double dt, double qIn, double perTr){

	//System.out.println("in OnRamp.inflow: qIn="+qIn);
       final double spaceMin=27; // minimum headway for new vehicle
       int lane=0;
       double space=0;
       nin=nin+qIn*dt;

       if (nin>1.0){  // new vehicle imposed by the inflow BC
         nin -= 1.0;
         int iPrev = street.size()-1;
         if (iPrev >= 0) {
            space = ((Moveable)(street.elementAt(iPrev))).position();
	 }

         else{space = roadLength;}


       	  // enough space for new vehicle to enter? (!red)
         if (!(red=(space<spaceMin))){
           double     rand      = random.nextDouble()*1.0;
     	   int        randInt   = Math.abs(random.nextInt());
           MicroModel modelNew  = (rand<perTr) ? idmTruck : idmCar;
           double     vNew      = modelNew.Veq(space);
           double     lNew      = (rand<perTr) ? LKW_LENGTH_M : PKW_LENGTH_M;
           Color      colorNew  = (rand<perTr) ? Color.black : Color.red;

	   street.insertElementAt
		 ((new Car(0.0, vNew, lane, modelNew, inconsiderate, 
                   lNew, Color.yellow, randInt)), street.size()); 
	 }
       }
    }



 


}
