package com.sa.npopa.samples.ccs.v02;

public class CallCenterSimulator
{
    public static int numAgents=30;
    
    public static void main( String[] args )
    {
        for ( int i = 1; i <= numAgents; i++ )
        {
            new CCAgent(i).start();
        }

        new CallGenerator().start();
        
        //new FlumeHttpLogger().start();
        new KafkaLogger().start();
    }
}