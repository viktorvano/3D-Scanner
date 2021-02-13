package com.viktor.vano.scanner3d;

import static com.viktor.vano.scanner3d.ObjectsAndVariables.*;

public class AppFunctions {
    public static void messageHandler(String message)
    {
        try{
            int tempDistance;
            String[] messages = message.split("\n");
            if(messages.length == 1)
                tempDistance = Integer.parseInt(messages[0]);
            else
                tempDistance = Integer.parseInt(messages[1]);
            distance = tempDistance;
            System.out.println("Received: " + tempDistance);
            measureFlag = true;
        }catch (Exception e)
        {
            System.out.println("Error String: " + message);
            e.printStackTrace();
        }
    }
}
