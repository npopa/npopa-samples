package com.sa.npopa.samples.util;

import java.util.Random;

public class RandomByteArray {
     
	private  Random rand;
	
	public RandomByteArray(){
		rand = new Random();
	}
	
	public  byte[] getRandomByteArray(int size){
		  byte[] result= new byte[size];
		  rand.nextBytes(result);
		  return result;
		 }	
}

