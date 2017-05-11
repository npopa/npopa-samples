package com.sa.npopa.samples.util;

import java.lang.reflect.Method;

public class TestFlumeUDFs {


	public static void main(String[] args) throws Exception {

		//String parameter
		Class[] paramString = new Class[1];
		paramString[0] = String.class;

		try{
			//load the AppTest at runtime
			Class cls = Class.forName("com.sa.npopa.samples.flume.UDFs");
			Object obj = cls.newInstance();
			Method method;
			String result;
			
			method = cls.getDeclaredMethod("timestampNormalizeToDate", paramString);
			result = (String) method.invoke(obj, new String("1486586758620"));
			System.out.println(result);
			method = cls.getDeclaredMethod("timestampDeltaFromDate", paramString);
			result = (String) method.invoke(obj, new String("1486586758620"));
			System.out.println(result);

		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
