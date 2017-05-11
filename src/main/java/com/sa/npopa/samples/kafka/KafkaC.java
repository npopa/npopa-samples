package com.sa.npopa.samples.kafka;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;

import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;


import java.util.Arrays;
import java.util.Properties;

/*
 * 
 * java  -Djava.security.auth.login.config=kafka-cache.jaas -Djavax.net.debug=ssl,handshake -cp "./target/*:$CLASSPATH:." com/sa/npopa/samples/KafkaC 
 * 
 * java  -Djava.security.auth.login.config=kafka-keytab.jaas -Djavax.net.debug=ssl,handshake -cp "./target/*:$CLASSPATH:." com/sa/npopa/samples/KafkaC
 * 
 * java  -Djava.security.auth.login.config=kafka-keytab.jaas -cp "./target/*:$CLASSPATH:." com/sa/npopa/samples/KafkaC 
 * 
 */

public class KafkaC extends Configured implements Tool{
	  
	
  @Override
  public int run(String[] args) throws Exception {
    Configuration conf = getConf();

    Properties props = new Properties();
    props.put("bootstrap.servers", "npopa-mst-1.vpc.cloudera.com:9093");
    props.put("group.id", "test");
    props.put("enable.auto.commit", "true");
    props.put("auto.commit.interval.ms", "1000");
    props.put("session.timeout.ms", "30000");
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

    //security
    
    //kerberos
    props.put("security.protocol", "SASL_SSL");
    props.put("sasl.kerberos.service.name","kafka");
    
    //ssl
    props.put("ssl.truststore.location","/opt/cloudera/security/jks/truststore.jks");
    props.put("ssl.truststore.password","password");
    props.put("ssl.keystore.location","/opt/cloudera/security/jks/keystore.jks");
    props.put("ssl.keystore.password","password");
    props.put("ssl.key.password","password");  

    KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
    consumer.subscribe(Arrays.asList("topic1"));
    while (true) {
        ConsumerRecords<String, String> records =  consumer.poll(50);
        for (ConsumerRecord<String, String> record : records)
            System.out.printf("offset = %d, key = %s, value = %s", record.offset(), record.key(), record.value());
    }
  }
  
  public static void main(String[] args) throws Exception {
    int exitCode = ToolRunner.run(new KafkaC(), args);
    System.exit(exitCode);
  }

  
}



