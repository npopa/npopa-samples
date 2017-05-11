package com.sa.npopa.samples.ccs.v02;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallQueue
{
	private static final Logger LOG = LoggerFactory.getLogger(CallQueue.class);
    private static CallQueue instance;

    private int counter;

    private LinkedBlockingQueue<Call> queue;

    public static void queueCall( int duration )
    {
        try
        {
            Call call = new Call( getInstance().counter++, duration );
            LOG.debug( "Queueing call " + call.getNumber() + " with a duration of " + call.getDuration() + " seconds" );
            getInstance().queue.put(call);
        }
        catch ( InterruptedException e )
        {
            LOG.debug( "There was an error queueing the call" );
        }
    }

    public static Call retrieveCall()
    {
        Call call = getInstance().queue.poll();
        if ( call != null )
        {
            LOG.debug( "Retrieving call " + call.getNumber() );
        }
        return call;
    }

    private static CallQueue getInstance()
    {
        if ( instance == null )
        {
            instance = new CallQueue();
        }
        return instance;
    }

    private CallQueue()
    {
        this.queue = new LinkedBlockingQueue<Call>();
        this.counter = 1;
    }
}