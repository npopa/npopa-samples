package com.sa.npopa.samples.ccs.v02;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.calcite.util.RhBase64;
import org.apache.calcite.util.RhBase64.InputStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaLogger implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(KafkaLogger.class);
	private String message;
	Producer<String, String> producer;
	private boolean running;

	public KafkaLogger() {

	}

	@Override
	public void run() {

		while (running) {
			message = LogQueue.retrieveLog();
			if (message != null) {
				LOG.info(message);
				// send to kafka
				producer.send(new ProducerRecord<String, String>("kafka-tk1", "0" ,message ));

			} else {
				sleep();
			}
		}
	}

	public void start() {
		running = true;
		
		
		Properties props = new Properties();
		props.put("bootstrap.servers", "npopa-cm.vpc.cloudera.com:9092");
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("batch.size", 16384);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

		// security

		// kerberos
		//props.put("security.protocol", "SASL_SSL");
		//props.put("sasl.kerberos.service.name", "kafka");

		producer = new KafkaProducer<>(props);
		
	
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