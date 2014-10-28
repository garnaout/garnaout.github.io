import java.lang.Math;
import java.awt.*;
import java.util.Random;

public class IDMCar2 extends IDM implements MicroModel{ 	//    the characteristics of a CACC vehicle
    public IDMCar2(){

    	//geoTriangleDist triDist = new geoTriangleDist();
    	//v0 = triDist.geoTriangleDist(23.5, 30.6, 30.6);
    	Random r = new Random();
    	v0 = r.nextGaussian() * 1.39 + 31.94;   // a UNIFORM distribution between 100km/hr and 120 km/hr
    	//v0=30.6;  // means 130 km/hr
		delta=4.0;
		a=0.5;  //1
		b=3.0;  //1.0
		s0=2.0;
		T=0.5;  // reduced because of CACC communication via DSRC
		sqrtab=Math.sqrt(a*b);
		initialize();
    }
}


 