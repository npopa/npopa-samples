package com.sa.npopa.samples.ccs.v02;


import java.io.Serializable;
import java.util.Random;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Person
{
	private static final Logger LOG = LoggerFactory.getLogger(Person.class);

    private UUID id;
    private String firstName;
    private String lastName;
    
	public Person() {

	}

	public Person(UUID id, String firstName, String lastName) {
		super();
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Override
	public String toString() {
		return "Caller [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + "]";
	}
    
	
    

}
