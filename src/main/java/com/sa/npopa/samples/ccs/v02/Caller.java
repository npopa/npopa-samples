package com.sa.npopa.samples.ccs.v02;


import java.io.Serializable;
import java.util.Random;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Caller
{
	private static final Logger LOG = LoggerFactory.getLogger(Caller.class);

    private Person personInfo;
    private Address addressInfo;
    private String phoneNumber;
    
    
	public Caller() {
        this.personInfo = new Person();
        this.addressInfo = new Address();
	}
	
	public Person getPersonInfo() {
		return personInfo;
	}

	public void setPersonInfo(Person personInfo) {
		this.personInfo = personInfo;
	}

	public Address getAddressInfo() {
		return addressInfo;
	}

	public void setAddressInfo(Address addressInfo) {
		this.addressInfo = addressInfo;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

    

}
