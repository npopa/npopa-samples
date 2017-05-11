package com.sa.npopa.samples.ccs.v01;

import java.util.Calendar;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallGenerator
    implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger(CallGenerator.class);
    //private double hourlyCallLoad[]={70,60,40,30,26,20,26,34,46,66,70,90,86,90,80,100,114,106,114,110,106,100,90,74};
    private double hourlyCallLoad[]={ 50,35,25,20,16,15,20,35,45,55,70,88,95,102,106,110,114,113,110,102,95,85,70,55};
    private Random random;
    private double scale=1;
    private double callsPerSec;


    private boolean running;


    public CallGenerator()
    {
        random = new Random();
    }


    @Override
    public void run()
    {
        while ( running )
        {
            Call call = new Call();
            CallQueue.queueCall(call);
            sleep();
        }
    }


    public void start()
    {
        running = true;
        new Thread(this).start();
    }

    public void stop()
    {
        running = false;
    }

    private void sleep()
    {
        try
        {
        	Calendar now = Calendar.getInstance();
        	now.get(Calendar.HOUR_OF_DAY);
        	double nowTPS = hourlyCallLoad[now.get(Calendar.HOUR_OF_DAY)];
        	double laterTPS = (Calendar.HOUR_OF_DAY == 23 ? hourlyCallLoad[0] : hourlyCallLoad[Calendar.HOUR_OF_DAY+1]);
        	callsPerSec = (nowTPS + Calendar.MINUTE*((laterTPS-nowTPS)/60))*scale;
        	
        	int sleep = (int) ((1/callsPerSec)*1000);
            Thread.sleep(sleep);

        }
        catch ( InterruptedException e )
        {
            e.printStackTrace();
        }
    }
}