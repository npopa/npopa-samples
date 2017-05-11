package com.sa.npopa.samples.ccs.v02;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlumeHttpLogger implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(FlumeHttpLogger.class);
	private List<String> messages;

	private boolean running;

	public FlumeHttpLogger() {

	}

	@Override
	public void run() {

		while (running) {
			messages = LogQueue.drainLogs(100);
			if (messages.size() > 0) {
				JSONArray events = new JSONArray();
				for (String message : messages) {
					LOG.info(message);

					JSONObject headers = new JSONObject();
					JSONObject event = new JSONObject();
					event.put("headers", headers); //empty 
					event.put("body", message);
					events.add(event);

				}
				//POST to flume
				HttpPost request = new HttpPost("http://npopa-cm.vpc.cloudera.com:6686");
				CloseableHttpClient httpClient = HttpClientBuilder.create().build();
				request.addHeader("content-type", "application/json");
				request.addHeader("Accept", "application/json");
				request.addHeader("Connection", "close");
				try {

     				request.setEntity(new StringEntity(events.toString()));
     				LOG.info("Sending to flume");
     				CloseableHttpResponse response = httpClient.execute(request);
					LOG.info("Done sending to flume");
					EntityUtils.consume(response.getEntity());
					response.close();
					request.releaseConnection();

				} catch (Exception ex) {
					// handle exception here
					ex.printStackTrace();
				} 
				try {
						httpClient.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
			} else {
				sleep();
			}
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