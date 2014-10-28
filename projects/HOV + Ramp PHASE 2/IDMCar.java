import java.lang.Math;
import java.awt.*;
import java.util.Random;

public class IDMCar extends IDM implements MicroModel{
	
    public IDMCar(){
    	//geoTriangleDist triDist = new geoTriangleDist();
    	//v0 = triDist.geoTriangleDist(23.5, 30.6, 30.6);
    	
    	Random r = new Random();
    	v0 = r.nextGaussian() * 1.39 + 31.94;   // a uniform distribution between 100km/hr and 120 km/hr
    	
    	//v0 = 33.33;  // 120km/hr
		delta=4.0;
		a=0.5;  //.5 just increase a for now
		b=3.;  //3.0
		s0=2;
		//T = 0.8;
		//T = 1.;
		T= r.nextGaussian() * 0.10 + 0.9;       //a random between 0.8s (younger) and 1s (older)
		
		sqrtab=Math.sqrt(a*b);
		initialize();
	}
}

  