package com.sa.npopa.samples.ccs.v01;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentManager
implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger(AgentManager.class);	

	private boolean running;
	private int id;
	private Agent agent;

	public AgentManager( int id )
	{
		this.id=id;
	}


	@Override
	public void run()
	{

		while ( running )
		{
			LOG.trace("AgentManager "+this.id +" running.");
			if(this.id == 1){ //only one thread deals with offline agents?
				CallQueue.checkAndDropExpiredCalls();

				try {
					Agent offlineagent=AgentListOffline.getAgent();
					if ( offlineagent != null ) //if we have an agent let's see what it is up to
					{	
						if(offlineagent.isShiftStarted()){ //add it to the online list
							LOG.info("Online agent " + offlineagent.toString());

							offlineagent.setStatus(AgentStatus.AVAILABLE);
							offlineagent.setLastBreak(System.currentTimeMillis());
							AgentList.putAgent(offlineagent);

						}else{
							//put it back to the offline list
							AgentListOffline.putAgent(offlineagent);
						}
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			agent=AgentList.getAgent();
			if ( agent != null ) //if we have an agent let's see what it is up to
			{	
				if(agent.isOffline()) { //move to offline list
					try {
						AgentListOffline.putAgent(agent);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					if(!agent.isLocked()){ //need to see if I still need this locks
						agent.setLock(true);
						agent.work();
						agent.setLock(false);

					}
					try {
						AgentList.putAgent(agent);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}
	}

	public void start()
	{
		running = true;
		new Thread(this).start();
		LOG.trace("AgentManager "+this.id +" started.");
	}

	public void stop()
	{
		running = false;
	}

}