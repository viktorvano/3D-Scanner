package eu.cyberpunktech;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    private final static int pitchMax = 10, yawMax = 18;
    private static int pitch = pitchMax-1;
    private static int yaw = 0;
    private static int x;

    public static void main(String[] args) {
	// write your code here
        boolean direction = true;
        while(true){
            if(x == (pitchMax*yawMax)){
                pitch = pitchMax-1;
                yaw = 0;
                x = 0;
                System.exit(234);
                //points = new Coord3d[pitchMax*yawMax];
            }
            if(yaw < yawMax && direction)
            {
                try {
                    System.out.println("Index: " + x + " Pitch: " + pitch + " Yaw: " + yaw + " direction: " + direction);
                    //points[pitch*yawMax+yaw] = measureDistance(pitchMax - 1 - pitch, yaw);
                }catch (Exception e)
                {
                    System.out.println("measureDistance(" + pitch + ", " + yaw + ")");
                    e.printStackTrace();
                }
                yaw++;
                x++;
            }
            else if(yaw == yawMax && direction)
            {
                yaw--;
                if(pitch > 0)
                {
                    pitch--;
                    direction = false;
                }
            }else if(yaw >= 0 && !direction)
            {
                try {
                    System.out.println("Index: " + x + " Pitch: " + pitch + " Yaw: " + yaw + " direction: " + direction);
                    //points[pitch*yawMax+yaw] = measureDistance(pitchMax - 1 - pitch, yaw);
                }catch (Exception e)
                {
                    System.out.println("measureDistance(" + pitch + ", " + yaw + ")");
                    e.printStackTrace();
                }
                yaw--;
                x++;
            }
            else if(yaw == -1)
            {
                yaw = 0;
                if(pitch > 0)
                {
                    pitch--;
                    direction = true;
                }
            }

        }
    }
}
