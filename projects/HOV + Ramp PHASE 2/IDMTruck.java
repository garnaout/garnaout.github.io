import java.lang.Math;
import java.awt.*;
import java.util.Random;

public class IDMTruck extends IDM implements MicroModel{

    public IDMTruck(){
	//    System.out.println("in Cstr of class IDMTruck (no own ve calc)");
 
    	Random r = new Random();
    	v0 = r.nextGaussian() * 2.35 + 23.53;   // a uniform distribution between 90km/hr and 110 km/hr
        delta=4.0;
        a=0.3;
        b=2.0;
        s0=2.0;
        T=1.7;         
        sqrtab=Math.sqrt(a*b);
	initialize();
    }
}

 