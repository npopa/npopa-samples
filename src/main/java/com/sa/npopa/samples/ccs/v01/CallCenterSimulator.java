package com.sa.npopa.samples.ccs.v01;

/**
unset CLASSPATH
export CLASSPATH=`hbase classpath`:$CLASSPATH
export CLASSPATH=`hadoop classpath`:$CLASSPATH
export CLASSPATH=/opt/cloudera/parcels/CDH/jars/json-simple-1.1.1.jar:$CLASSPATH
export CLASSPATH=/var/lib/flume-ng/plugins.d/agent1/lib/npopa-samples-0.0.1-SNAPSHOT.jar:$CLASSPATH
export CLASSPATH=/opt/cloudera/parcels/KAFKA/lib/kafka/libs/*:$CLASSPATH
export HADOOP_CLASSPATH=$CLASSPATH:$HADOOP_CLASSPATH


kinit -kt hb580539.keytab hb580539@AD.SEC.CLOUDERA.COM
java -classpath $CLASSPATH com.sa.npopa.samples.ccs.v01.CallCenterSimulator 

*/

import java.io.IOException;

public class CallCenterSimulator
{
   //TODO - this needs params so it can be started in different ways without recompile. 
    public static void main( String[] args ) throws IOException, InterruptedException
    {
    	int numAgents=100000;
    	
    	for ( int i = 1; i <= numAgents; i++ )
        {    
    		Agent agent=new Agent(i);
    		AgentListOffline.putAgent(agent); //add all agents offline
        }   

    	int numMgr=5;
    	for (int i=0;i<numMgr;i++){
    		new AgentManager(i).start();
    	}
    	
        new CallGenerator().start(); //1  
        new KafkaLogger().start();
        new Monitor().start();
    }
}