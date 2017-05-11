package com.sa.npopa.samples.ccs.v02;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogQueue
{
	private static final Logger LOG = LoggerFactory.getLogger(LogQueue.class);
    private static LogQueue instance;

    private LinkedBlockingQueue<String> queue;

    public static void queueLog( String logMessage )
    {
        try
        {
            LOG.debug( "Queueing log " + logMessage );
            getInstance().queue.put(logMessage);
        }
        catch ( InterruptedException e )
        {
            LOG.debug( "There was an error queueing the call" );
        }
    }

    public static String retrieveLog()
    {
        String logMessage = getInstance().queue.poll();
        if ( logMessage != null )
        {
            LOG.debug( "Retrieving log " + logMessage );
        }
        return logMessage;
    }

    public static List<String> drainLogs(int maxLogs)
    {
    	List<String> batched = new ArrayList<String>( maxLogs );
    	int logMessage = getInstance().queue.drainTo(batched, maxLogs);
        return batched;
    }

    
    private static LogQueue getInstance()
    {
        if ( instance == null )
        {
            instance = new LogQueue();
        }
        return instance;
    }

    private LogQueue()
    {
        this.queue = new LinkedBlockingQueue<String>();

    }
}