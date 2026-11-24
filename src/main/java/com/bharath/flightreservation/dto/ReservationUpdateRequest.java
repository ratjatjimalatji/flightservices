package com.bharath.flightreservation.dto;

import javax.validation.constraints.Min;

/**
 * Partial update: fields left out (null) keep their current value.
 */
public class ReservationUpdateRequest {

	/** Only used by the legacy PUT /reservations endpoint (id in the body). */
	private Long id;
	private Boolean checkedIn;
	@Min(0)
	private Integer numberOfBags;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	@Override
	public String toString() {
		return "ReservationUpdateRequest [id=" + id + ", checkedIn=" + checkedIn + ", numberOfBags=" + numberOfBags
				+ "]";
	}

}
