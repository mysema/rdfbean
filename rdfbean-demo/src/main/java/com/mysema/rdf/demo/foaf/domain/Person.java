package com.mysema.rdf.demo.foaf.domain;

import com.mysema.rdf.demo.generic.Resource;

/**
 *  foaf:geekCode
 *  foaf:plan
 *  foaf:img
 * 
 * 
 * @author mala
 *
 */
public class Person extends Agent {

	private String firstName;
	
	private String surname;

	private Image img;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public void setImg(Image img) {
		this.img = img;
	}

	public Image getImg() {
		return img;
	}
	
	public Resource getGenericEntity() {
	    return null;
	}
}
