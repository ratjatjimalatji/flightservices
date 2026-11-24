package com.bharath.flightreservation.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class Flight extends AbstractEntity {

	@NotBlank
	@Size(max = 20)
	private String flightNumber;

	@NotBlank
	@Size(max = 20)
	private String operatingAirlines;

	@NotBlank
	@Size(max = 20)
	private String departureCity;

	@NotBlank
	@Size(max = 20)
	private String arrivalCity;

	/** JSON format: "yyyy-MM-dd" */
	@NotNull
	private LocalDate dateOfDeparture;

	/** JSON format: "yyyy-MM-ddTHH:mm:ss" */
	private LocalDateTime estimatedDepartureTime;

	public String getFlightNumber() {
		return flightNumber;
	}

	public void setFlightNumber(String flightNumber) {
		this.flightNumber = flightNumber;
	}

	public String getOperatingAirlines() {
		return operatingAirlines;
	}

	public void setOperatingAirlines(String operatingAirlines) {
		this.operatingAirlines = operatingAirlines;
	}

	public String getDepartureCity() {
		return departureCity;
	}

	public void setDepartureCity(String departureCity) {
		this.departureCity = departureCity;
	}

	public String getArrivalCity() {
		return arrivalCity;
	}

	public void setArrivalCity(String arrivalCity) {
		this.arrivalCity = arrivalCity;
	}

	public LocalDate getDateOfDeparture() {
		return dateOfDeparture;
	}

	public void setDateOfDeparture(LocalDate dateOfDeparture) {
		this.dateOfDeparture = dateOfDeparture;
	}

	public LocalDateTime getEstimatedDepartureTime() {
		return estimatedDepartureTime;
	}

	public void setEstimatedDepartureTime(LocalDateTime estimatedDepartureTime) {
		this.estimatedDepartureTime = estimatedDepartureTime;
	}

	@Override
	public String toString() {
		return "Flight [id=" + getId() + ", flightNumber=" + flightNumber + ", operatingAirlines=" + operatingAirlines
				+ ", departureCity=" + departureCity + ", arrivalCity=" + arrivalCity + ", dateOfDeparture="
				+ dateOfDeparture + ", estimatedDepartureTime=" + estimatedDepartureTime + "]";
	}

}
