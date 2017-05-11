package com.sa.npopa.samples.ccs.v02;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallGenerator
    implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger(CallGenerator.class);

    private Random random;
    private double callsPerSec=0.1;
    private int minCallSec=100;
    private int maxCallSec=240;

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
            int duration = minCallSec + random.nextInt(maxCallSec - minCallSec); 
            LOG.debug( "Creating a call with duration " + duration + " seconds" );
            CallQueue.queueCall(duration);
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
            int sleep = (int) ((1/callsPerSec)*1000);
            Thread.sleep(sleep);
            Thread.sleep(1+random.nextInt((int) (sleep*0.1))); //add 10% jitter
        }
        catch ( InterruptedException e )
        {
            e.printStackTrace();
        }
    }
}