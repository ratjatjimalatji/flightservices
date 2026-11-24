package com.bharath.flightreservation.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class ReservationRequest {

	@NotNull
	private Long flightId;
	@NotBlank
	private String passengerFirstName;
	@NotBlank
	private String passengerLastName;
	@Email
	private String passengerEmail;
	private String passengerPhone;
	private String nameOnTheCard;
	private String cardNumber;

	private String expirationDate;
	private String securityCode;

	public Long getFlightId() {
		return flightId;
	}

	public void setFlightId(Long flightId) {
		this.flightId = flightId;
	}

	public String getPassengerFirstName() {
		return passengerFirstName;
	}

	public void setPassengerFirstName(String passengerFirstName) {
		this.passengerFirstName = passengerFirstName;
	}

	public String getPassengerLastName() {
		return passengerLastName;
	}

	public void setPassengerLastName(String passengerLastName) {
		this.passengerLastName = passengerLastName;
	}

	public String getPassengerEmail() {
		return passengerEmail;
	}

	public void setPassengerEmail(String passengerEmail) {
		this.passengerEmail = passengerEmail;
	}

	public String getPassengerPhone() {
		return passengerPhone;
	}

	public void setPassengerPhone(String passengerPhone) {
		this.passengerPhone = passengerPhone;
	}

	public String getNameOnTheCard() {
		return nameOnTheCard;
	}

	public void setNameOnTheCard(String nameOnTheCard) {
		this.nameOnTheCard = nameOnTheCard;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(String expirationDate) {
		this.expirationDate = expirationDate;
	}

	public String getSecurityCode() {
		return securityCode;
	}

	public void setSecurityCode(String securityCode) {
		this.securityCode = securityCode;
	}

	// Card details are deliberately excluded so they never end up in logs.
	@Override
	public String toString() {
		return "ReservationRequest [flightId=" + flightId + ", passengerFirstName=" + passengerFirstName
				+ ", passengerLastName=" + passengerLastName + "]";
	}

}
