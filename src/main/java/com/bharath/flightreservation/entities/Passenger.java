package com.bharath.flightreservation.entities;

import javax.persistence.Entity;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
public class Passenger extends AbstractEntity {

	@NotBlank
	@Size(max = 256)
	private String firstName;

	@NotBlank
	@Size(max = 256)
	private String lastName;

	@Size(max = 256)
	private String middleName;

	@Email
	@Size(max = 50)
	private String email;

	@Size(max = 10)
	private String phone;

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

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Override
	public String toString() {
		return "Passenger [id=" + getId() + ", firstName=" + firstName + ", lastName=" + lastName + "]";
	}

}
