package com.sa.npopa.samples.util;

import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

public class TestTicker {
    private static String first_letter="XYZW";
    private static String second_letter="DFGE";
    private static String third_letter="PLER"; 
    private static String fourth_letter="WMOS";  
    
	public static void main(String[] args) throws Exception {
		long now=System.currentTimeMillis();
		float price;
		Ticker[] theTickers = new Ticker[first_letter.length()*second_letter.length()*third_letter.length()*third_letter.length()];
		int k=0;
		for (int l1=0;l1<first_letter.length();l1++){
			for (int l2=0;l2<second_letter.length();l2++){
				for (int l3=0;l3<third_letter.length();l3++){
					for (int l4=0;l4<fourth_letter.length();l4++){
						theTickers[k++]=new Ticker(String.valueOf(first_letter.charAt(l1))
								                  + String.valueOf(second_letter.charAt(l2))
								                  + String.valueOf(third_letter.charAt(l3))
								                  + String.valueOf(fourth_letter.charAt(l4)));
					}
				}
			}
		}
		
		
		Ticker myTicker = new Ticker("CLDR");
		for (int i=0;i<1;i++){
			for (int j=0;j<theTickers.length;j++){
				price=theTickers[j].nextPrice();
				System.out.println(theTickers[j].toString());
				Thread.sleep(1000);
			}

		}

		

	}
	
	
	
	public static class Ticker {

		private String symbol;
	    private float min_price;
	    private float max_price;
	    private float price;
	    private float volatility;
		
	    private Random _random=new Random();

		public Ticker(String symbol) {
			super();
			this.symbol = symbol;
			this.price = _random.nextFloat()*20+10; //between $10 and $30
			this.min_price = price-price*(_random.nextFloat()*20+10)/100;
			this.max_price = price+price*(_random.nextFloat()*20+10)/100;
		    this.volatility = _random.nextFloat()*2 + 1; //up to 3% volatility
		}

		
		private float nextPrice()
		{

		    float rnd = _random.nextFloat();

		    float changePercent = 2 * volatility * rnd;

		    if (changePercent > volatility) {
		        changePercent -= (2 * volatility);
		    }
		    float changeAmount = this.price * changePercent/100;
		    float newPrice = this.price + changeAmount;

		    // Add a ceiling and floor.
		    if (newPrice < min_price) {
		        newPrice += Math.abs(changeAmount) * 2;
		    } else if (newPrice > max_price) {
		        newPrice -= Math.abs(changeAmount) * 2;
		    }
		    this.price = newPrice;
		    
		    return newPrice;

		}
		
		
		public String getSymbol() {
			return symbol;
		}

		public void setSymbol(String symbol) {
			this.symbol = symbol;
		}

		public float getMin_price() {
			return min_price;
		}

		public void setMin_price(float min_price) {
			this.min_price = min_price;
		}

		public float getMax_price() {
			return max_price;
		}

		public void setMax_price(float max_price) {
			this.max_price = max_price;
		}

		public float getPrice() {
			return price;
		}

		public void setPrice(float price) {
			this.price = price;
		}

		public float getVolatility() {
			return volatility;
		}

		public void setVolatility(float volatility) {
			this.volatility = volatility;
		}


		@Override
		public String toString() {
			return System.currentTimeMillis()/1000+"," +symbol + "," + String.format("%.2f", price) ;
		}
		
		

	}
	
	
}


/**
 * rnd = Random_Float(); // generate number, 0 <= x < 1.0
change_percent = 2 * volatility * rnd;
if (change_percent > volatility)
    change_percent -= (2 * volatility);
change_amount = old_price * change_percent;
new_price = old_price + change_amount;
 * 
 */


