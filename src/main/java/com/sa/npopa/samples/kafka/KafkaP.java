package com.sa.npopa.samples.kafka;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.conf.Configured;

import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.metrics.KafkaMetric;
import org.apache.kafka.common.metrics.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sa.npopa.samples.util.SampleMessage;


/*
 * 
 * java  -Djava.security.auth.login.config=kafka-cache.jaas -Djavax.net.debug=ssl,handshake -cp "./target/*:$CLASSPATH:." com/sa/npopa/samples/KafkaP 
 * 
 * java  -Djava.security.auth.login.config=kafka-keytab.jaas -Djavax.net.debug=ssl,handshake -cp "./target/*:$CLASSPATH:." com/sa/npopa/samples/KafkaP 
 * 
 * java  -Djava.security.auth.login.config=kafka-keytab.jaas -cp "./target/*:$CLASSPATH:." com/sa/npopa/samples/KafkaP 
 * 
 * export ZOOKEEPER=npsec-mst-1.vpc.cloudera.com:2181/kafka
 * kafka-topics --zookeeper $ZOOKEEPER --delete   --topic topic1
 * kafka-topics --zookeeper $ZOOKEEPER --list
 * kafka-topics --zookeeper $ZOOKEEPER --describe --topic topic1
 * kafka-topics --zookeeper $ZOOKEEPER --create   --topic topic1  --partitions 3 --replication-factor 3
 * kafka-consumer-offset-checker --zookeeper $ZOOKEEPER --topic topic1 --group flume_group
 * 
 * export CLASSPATH=/opt/cloudera/parcels/CDH/jars/*; nohup java -cp "$CLASSPATH:npopa-samples-0.0.1-SNAPSHOT.jar" com.sa.npopa.samples.kafka.KafkaP >out_`date +%s`.log 2>&1
 * 
 * echo "disable 'table1';drop 'table1'; create 'table1',{NAME => 'J', DATA_BLOCK_ENCODING => 'FAST_DIFF', COMPRESSION => 'SNAPPY', VERSIONS => '1', IS_MOB => true, MOB_THRESHOLD => 5000}, {NUMREGIONS => 16, SPLITALGO => 'HexStringSplit'};grant 'flume','RWCA','table1'"|hbase shell
 * scp /Users/npopa/Documents/workspace/npopa-samples/target/npopa-samples-0.0.1-SNAPSHOT.jar root@npsec-gw.vpc.cloudera.com:
 * 
 * 
 */

class MyCallBack implements Callback {

	  private static Logger LOG = LoggerFactory.getLogger(
			  KafkaP.class);
	  
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
            LOG.debug("message with key ["+key+"] was sent at "+startTime+" to topic [" + metadata.topic() + "] partition [" + metadata.partition() + "], "
                    + "offset [" + metadata.offset() + "] in " + elapsedTime + " ms");
           // System.out.println("message with key ["+key+"] was sent at "+startTime+" to topic [" + metadata.topic() + "] partition [" + metadata.partition() + "], "
           //         + "offset [" + metadata.offset() + "] in " + elapsedTime + " ms");
        } else {
            exception.printStackTrace();
        }
    }
}

public class KafkaP extends Configured implements Tool{
	  
	
  @Override
  public int run(String[] args) throws Exception { 
	  

	//debug stuff
	//System.setProperty("javax.net.debug", "all");
	//System.setProperty("sun.security.krb5.debug", "true");
	//System.setProperty("sun.security.jgss.debug","true"); 
	//-Djava.security.debug=gssloginconfig,configfile,logincontext,configparser
    
	//this assumes you have the jaas in the jar. if you use the hardcoded path you can comment the next 2 lines.
	URL jaas_url = ClassLoader.getSystemResource("kafka-jaas.conf");
	Random rand = new Random();
	System.setProperty("java.security.auth.login.config", jaas_url.toExternalForm());
    

    Properties all_props = new Properties();   
    Properties kafka_props = new Properties();
    
    all_props.load(KafkaP.class.getClassLoader().getResourceAsStream("KafkaP.properties"));
	    for (Enumeration<?> e = all_props.propertyNames(); e.hasMoreElements(); ) {
	        String name = (String)e.nextElement();
	        String value = all_props.getProperty(name);
	        //System.out.println("Property ["+name+"]=["+value+"]");
	        if (name.startsWith("kafka.")) {
		        name=name.replace("kafka.","");
		        //System.out.println("Found a kafka property ["+name+"]=["+value+"]");
		        kafka_props.put(name,value);
	        }
    }


  
	//System.out.println(Long.toHexString(Double.doubleToLongBits(Math.random())));
	//PrintWriter out = new PrintWriter("/tmp/temp/msgs.txt");

    long producerStartTime = System.currentTimeMillis();
    Producer<String, String> producer = new KafkaProducer<>(kafka_props);
    for(int i = 0; i < 30000000; i++)
    {
    	SampleMessage msg=new SampleMessage(90+rand.nextInt(1024*10)); 
    	ProducerRecord<String, String> record = new ProducerRecord<String, String>("topic1", msg.getKey(), msg.getMessage());
    	long startTime = System.currentTimeMillis();
    	//async
    	///*
    	producer.send(record, new MyCallBack(startTime, record.key(), record.value())); 
    	//out.println(msg.getMessage());
		
    	//*/
    	
    	
    	//sync
    	/*
    	try {
	    	RecordMetadata metadata;
	    	metadata=producer.send(record).get(2000, TimeUnit.MILLISECONDS);
	    	//System.out.println("["+record.key()+"] was sent to partition "+ metadata.partition()+" offset "+metadata.offset()+" Latency: "+ (System.currentTimeMillis()-startTime)+" ms.");
    	} catch (java.util.concurrent.TimeoutException e) {
    	   System.out.println("["+record.key()+"] Failed to get a response after "+ (System.currentTimeMillis()-startTime)+" ms.");
    	}
        */
    	
    } 
	producer.flush(); 
	//out.close();
	
    System.out.println("Producer send took "+ (System.currentTimeMillis()-producerStartTime)+" ms.");
    Map<MetricName, ? extends Metric> metrics = new LinkedHashMap<MetricName, KafkaMetric>();
    metrics=producer.metrics();
    for (MetricName metric:metrics.keySet()) {
        System.out.println("Metric "+ metric+" "+metrics.get(metric).value());
    }

 
    producer.close();
    System.out.println("Overall "+ (System.currentTimeMillis()-producerStartTime)+" ms.");

    return 0;

  }
  
  public static void main(String[] args) throws Exception {
    int exitCode = ToolRunner.run(new KafkaP(), args);
    System.exit(exitCode);
  }

  
}



