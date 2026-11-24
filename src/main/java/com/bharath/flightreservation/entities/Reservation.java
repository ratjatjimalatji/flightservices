package com.bharath.flightreservation.entities;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class Reservation extends AbstractEntity {

	private Boolean checkedIn;

	// Nullable in the database, so it must not be a primitive.
	private Integer numberOfBags;

	@OneToOne
	private Passenger passenger;

	// Many reservations can be made on the same flight.
	@ManyToOne
	private Flight flight;

	public Boolean getCheckedIn() {
		return checkedIn;
	}

	public void setCheckedIn(Boolean checkedIn) {
		this.checkedIn = checkedIn;
	}

	public Integer getNumberOfBags() {
		return numberOfBags;
	}

	public void setNumberOfBags(Integer numberOfBags) {
		this.numberOfBags = numberOfBags;
	}

	public Passenger getPassenger() {
		return passenger;
	}

	public void setPassenger(Passenger passenger) {
		this.passenger = passenger;
	}

	public Flight getFlight() {
		return flight;
	}

	public void setFlight(Flight flight) {
		this.flight = flight;
	}

	@Override
	public String toString() {
		return "Reservation [id=" + getId() + ", checkedIn=" + checkedIn + ", numberOfBags=" + numberOfBags
				+ ", passengerId=" + (passenger == null ? null : passenger.getId()) + ", flightId="
				+ (flight == null ? null : flight.getId()) + "]";
	}

}
