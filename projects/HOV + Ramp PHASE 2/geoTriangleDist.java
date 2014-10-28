import java.lang.Math;
import java.awt.*;
import java.util.Random;

public class geoTriangleDist {
	
	Random generator = new Random();
	double answer; 
	

    public double geoTriangleDist(double min, double mode, double max){

    	double random = generator.nextDouble();  //  random double between 0 and 1
    	double d = 0;
    	
    	
    	if ( random ==  ((mode -  min) / ( max -  min)))
        {
    		answer = Math.round(mode);
            return  answer;
        }
    	  
    	
    	else if ( random < (( mode -  min) / ( max -  min)))
    	    {
    		    answer = Math.round( min + Math.sqrt( random * ( max -  min) * ( mode -  min)));
    	        return answer;
    	    }
        
    	else {
    		
    		answer = Math.round(max - Math.sqrt((1 -  random) * ( max -  min) * ( max -  mode)));
    		return  answer;
    	}
        	
		
    }
}

 