package com.sa.npopa.samples.flume;

import java.util.LinkedList;
import java.util.List;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.FlumeException;
import org.apache.flume.conf.ComponentConfiguration;
import org.apache.flume.sink.hbase.HbaseEventSerializer;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Row;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

public class JsonHBaseSerializer implements HbaseEventSerializer {
	
	private static final Logger LOGGER = LoggerFactory
			.getLogger(JsonHBaseSerializer.class);
  private byte[] cf;
  private byte[] payload;
  private byte[] payloadColumn;
  private byte[] payloadKey;
  private byte[] incrementRow;
  private byte[] incCol;
  String pKey;
  private JSONParser parser;
  public static final String REGEX = "\\s*,\\s*";
  private String[] pKey_attributes;

  public JsonHBaseSerializer(){

  }
  
  @Override
  public void initialize(Event event, byte[] cf) {
    this.payload = event.getBody();
    //LOGGER.info("Received: " + this.payload.length + " bytes.");
    //we have the key now it is time to parse the Body to extract it
	String attribute_value=null;
	String composedKey=null;
	JSONObject _json = null;
	StringBuilder builder = new StringBuilder();

	try {
		_json = (JSONObject) parser.parse(new String(event.getBody(), Charsets.UTF_8));
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	for (String pKey_attribute : pKey_attributes){
		attribute_value = (String) _json.get(pKey_attribute);
		if (attribute_value != null)
			builder.append(attribute_value);
	}
	composedKey=builder.toString();
	if (composedKey != null)
		this.payloadKey = composedKey.getBytes();
    this.cf = cf;

  }

  @Override
  public List<Row> getActions() throws FlumeException {
    List<Row> actions = new LinkedList<Row>();
    if(payloadColumn != null){
      try {
        Put put = new Put(payloadKey);
        put.add(cf, payloadColumn, payload);
        actions.add(put);
      } catch (Exception e){
        throw new FlumeException("Could not get row key!", e);
      }

    }
    return actions;
  }

@Override
  public void configure(Context context) {
	this.parser = new JSONParser();
	incrementRow =
	            context.getString("incrementRow", "incRow").getBytes(Charsets.UTF_8);
    String pCol = context.getString("payloadColumn", "pCol");
    pKey = context.getString("payloadKey", "pKey");
    pKey_attributes = pKey.split(REGEX);
    String incColumn = context.getString("incrementColumn","iCol");
    if(pCol != null && !pCol.isEmpty()) {
      payloadColumn = pCol.getBytes(Charsets.UTF_8);
    }
    if(pCol != null && !pCol.isEmpty()) {  //this needs to be fixed. Add an else
        payloadKey = pKey.getBytes(Charsets.UTF_8);
    }
    if(incColumn != null && !incColumn.isEmpty()) {
        incCol = incColumn.getBytes(Charsets.UTF_8);
    }


  }


 @Override
  public void configure(ComponentConfiguration conf) {
    // TODO Auto-generated method stub
  }
@Override
public void close() {
	// TODO Auto-generated method stub
	
}
@Override
public List<Increment> getIncrements(){
  List<Increment> incs = new LinkedList<Increment>();
  if(incCol != null) {
    Increment inc = new Increment(incrementRow);
    inc.addColumn(cf, incCol, 1);
    incs.add(inc);
  }
  return incs;
}

}
