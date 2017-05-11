package com.sa.npopa.samples.ccs.v01;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentListOffline
{
	private static final Logger LOG = LoggerFactory.getLogger(AgentListOffline.class);

    private static int numAgents;
    private static AgentListOffline instance;
    private static int counter=0;

    private LinkedBlockingQueue<Agent> agentsList;
//maybe add few lists? one for the active ones, one for the offline ones etc
    public AgentListOffline()
    {
    	this.agentsList = new LinkedBlockingQueue<Agent>();

    }
    private static AgentListOffline getInstance()
    {
        if ( instance == null )
        {
            instance = new AgentListOffline();
        }
        return instance;
    }    
    
    public int addAgents(int num) {
    	
    	for ( int i = 1; i <= num; i++ )
        {    
    		Agent agent=new Agent(i);
    		getInstance().agentsList.add(agent);
        }   
    	this.numAgents=num;
    	return 0;
    	
    }
    
    public synchronized static Agent getAgent()
    {
    	Agent agent = getInstance().agentsList.poll();
        if (agent!=null ){
        	return agent;
        }
        return null;
    }  

    
    public synchronized static void putAgent(Agent agent) throws InterruptedException
    {
    	getInstance().agentsList.put(agent);
    }  
    
    public static int getSize()
    {
        return((getInstance().agentsList).size());
    }
    
    public static String dump(){
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append(getInstance().agentsList.toString());
    	
    	return sb.toString();
    }
    
    public static String stats(){
    	StringBuilder sb = new StringBuilder();
    	HashMap<AgentStatus, Integer> stats = new HashMap<AgentStatus, Integer>();
    	
    	Iterator<Agent> i= getInstance().agentsList.iterator();
    	while(i.hasNext()){
    		AgentStatus s=i.next().getStatus();
    		if (stats.containsKey(s)){
    			stats.put(s,(stats.get(s))+1);
    		} else {
    			stats.put(s,1);
    		}
    	}
    	sb.append(stats.toString());
    	return sb.toString();
    }
    
}