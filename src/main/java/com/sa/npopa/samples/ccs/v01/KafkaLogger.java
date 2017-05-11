package com.sa.npopa.samples.ccs.v01;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.sa.npopa.samples.util.SampleMessage;

class MyCallBack implements Callback {

	//TODO - need to publish counts per token. Maybe all log messages should have a token?
	  private static Logger LOG = LoggerFactory.getLogger(
			  MyCallBack.class);
	  
  private final long startTime;
  private final String key;
  private final String message;

  public MyCallBack(long startTime, String key, String message) {
      this.startTime = startTime;
      this.key = key;
      this.message = message;
  }

  public void onCompletion(RecordMetadata metadata, Exception exception) {
      long elapsedTime = System.currentTimeMillis() - startTime;
      if (metadata != null) {
          LOG.trace("message with key ["+key+"] was sent at "+startTime+" to topic [" + metadata.topic() + "] partition [" + metadata.partition() + "], "
                  + "offset [" + metadata.offset() + "] in " + elapsedTime + " ms");
      } else {
          exception.printStackTrace();
      }
  }
}



public class KafkaLogger implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(KafkaLogger.class);
	private LogMessage message;
	Producer<String, String> producer;
	private boolean running;

	public KafkaLogger() {

	}

	@Override
	public void run() {

		while (running) {
			message = LogQueue.retrieveLog();
			if (message != null) {
				//LOG.info(message);
				// send to kafka			
		    	ProducerRecord<String, String> record = new ProducerRecord<String, String>(message.getType(), message.getKey(), message.getMsg());
		    	long startTime = System.currentTimeMillis();
		    	producer.send(record, new MyCallBack(startTime, record.key(), record.value())); 

			} else {
				sleep();
			}
		}
	}

	public void start() throws IOException {
		running = true;
		
		
	    Properties all_props = new Properties();   
	    Properties kafka_props = new Properties();
	    
	    all_props.load(KafkaLogger.class.getClassLoader().getResourceAsStream("KafkaP.properties"));
		    for (Enumeration<?> e = all_props.propertyNames(); e.hasMoreElements(); ) {
		        String name = (String)e.nextElement();
		        String value = all_props.getProperty(name);
		        LOG.info("Property ["+name+"]=["+value+"]");
		        if (name.startsWith("kafka.")) {
			        name=name.replace("kafka.","");
			        kafka_props.put(name,value);
		        }
	    }

		
		URL jaas_url = ClassLoader.getSystemResource("kafka-jaas.conf");
		System.setProperty("java.security.auth.login.config", jaas_url.toExternalForm());
	    

		
		producer = new KafkaProducer<>(kafka_props);
		
	
		new Thread(this).start();

	}

	public void stop() {
		running = false;
	}

	private void sleep() {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}