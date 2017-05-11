package com.sa.npopa.samples.util;

import java.util.Calendar;

public class Test1 {

    private static double hourlyCallLoad[]={70,60,40,30,26,20,26,34,46,66,70,90,86,90,80,100,114,106,114,110,106,100,90,74};
	public static void main(String[] args) throws Exception {
		/**
		double callsPerSec;
		for (int hour=0; hour<24;hour++){
			double nowTPS = hourlyCallLoad[hour];
			double laterTPS = hour == 23 ? hourlyCallLoad[0]:hourlyCallLoad[hour+1];
			for (int minute=0;minute<60;minute++){
			 callsPerSec = nowTPS + minute*(laterTPS-nowTPS)/60;
			int sleep = (int) ((1/callsPerSec)*1000);
			System.out.println("Hour:"+hour+" Minute:" +minute+" Sleep: "+ sleep);
			}
			
		}
		*/
		
		//long test=0x0000000000000001L;	//RINGING
		//long test=0x0000000000000100L;	//ANSWERED
		//long test=0x0000000000010000L;	//ON_HOLD
		//long test=0x0000000001000000L;	//OFF_HOLD
		//long test=0x0000000100000000L;	//HUNGUP		
		//long test=0x0000010000000000L;	//COMPLETED
		//long test=0x0001000000000000L;	//DROP
		long test=0x0100000000000000L;	//METRICS
		
		
		System.out.println(test);
		

	}
	
}
