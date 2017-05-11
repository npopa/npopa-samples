package com.sa.npopa.samples.ccs.v01;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Monitor implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(Monitor.class);
//TODO - not sure if I still need this. Maybe implement another class for Dashboard?
	private boolean running;

	public Monitor() {

	}

	@Override
	public void run() {

		while (running) {
			LOG.info("==== Stats ====");
			LOG.info("AgentOffline queue size:"+ AgentListOffline.getSize());	
			LOG.info("AgentOffline queue stats:"+ AgentListOffline.stats());	
			LOG.info("Agent queue size:"+ AgentList.getSize());	
			LOG.info("Agent queue stats:"+ AgentList.stats());		
			LOG.info("Call queue size:"+ CallQueue.getSize());
			LOG.info("Call queue stats:"+ CallQueue.stats());
			if(CallQueue.getSize()>1000){
				LOG.info(CallQueue.dump(10));
			}
			LOG.info("KafkaLog queue size:"+ LogQueue.getSize());
			
			//LOG.info("KafkaLog queue dump:"+ LogQueue.dump());
			sleep();
	
		}
	}

	public void start() {
		running = true;
	
		new Thread(this).start();

	}

	public void stop() {
		running = false;
	}

	private void sleep() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}