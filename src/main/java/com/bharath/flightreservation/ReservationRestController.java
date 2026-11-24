package com.bharath.flightreservation;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.bharath.flightreservation.dto.ReservationRequest;
import com.bharath.flightreservation.dto.ReservationUpdateRequest;
import com.bharath.flightreservation.entities.Flight;
import com.bharath.flightreservation.entities.Passenger;
import com.bharath.flightreservation.entities.Reservation;
import com.bharath.flightreservation.exception.ResourceNotFoundException;
import com.bharath.flightreservation.repos.FlightRepository;
import com.bharath.flightreservation.repos.PassengerRepository;
import com.bharath.flightreservation.repos.ReservationRepository;

@RestController
@CrossOrigin
@RequestMapping("/reservations")
public class ReservationRestController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReservationRestController.class);

	private final ReservationRepository reservationRepository;
	private final FlightRepository flightRepository;
	private final PassengerRepository passengerRepository;

	public ReservationRestController(ReservationRepository reservationRepository, FlightRepository flightRepository,
			PassengerRepository passengerRepository) {
		this.reservationRepository = reservationRepository;
		this.flightRepository = flightRepository;
		this.passengerRepository = passengerRepository;
	}

	@GetMapping
	public List<Reservation> findReservations() {
		return reservationRepository.findAll();
	}

	@GetMapping("/{id}")
	public Reservation findReservation(@PathVariable Long id) {
		LOGGER.info("Inside findReservation() for id: {}", id);
		return getReservation(id);
	}

	/** Books a flight: creates the passenger and the reservation together. */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public Reservation saveReservation(@Valid @RequestBody ReservationRequest request) {
		LOGGER.info("Inside saveReservation() for {}", request);
		// Payment processing is not implemented; card fields are accepted but not stored.

		Long flightId = request.getFlightId();
		Flight flight = flightRepository.findById(flightId)
				.orElseThrow(() -> new ResourceNotFoundException("Flight", flightId));

		Passenger passenger = new Passenger();
		passenger.setFirstName(request.getPassengerFirstName());
		passenger.setLastName(request.getPassengerLastName());
		passenger.setPhone(request.getPassengerPhone());
		passenger.setEmail(request.getPassengerEmail());
		Passenger savedPassenger = passengerRepository.save(passenger);

		Reservation reservation = new Reservation();
		reservation.setFlight(flight);
		reservation.setPassenger(savedPassenger);
		reservation.setCheckedIn(false);
		reservation.setNumberOfBags(0);

		Reservation savedReservation = reservationRepository.save(reservation);
		LOGGER.info("Saved reservation: {}", savedReservation);
		return savedReservation;
	}

	/** Partial update (check-in / bags): fields left out keep their current value. */
	@PutMapping("/{id}")
	@Transactional
	public Reservation updateReservation(@PathVariable Long id,
			@Valid @RequestBody ReservationUpdateRequest request) {
		LOGGER.info("Inside updateReservation() for id {}: {}", id, request);
		Reservation reservation = getReservation(id);
		if (request.getNumberOfBags() != null) {
			reservation.setNumberOfBags(request.getNumberOfBags());
		}
		if (request.getCheckedIn() != null) {
			reservation.setCheckedIn(request.getCheckedIn());
		}
		return reservationRepository.save(reservation);
	}

	/** Original check-in endpoint, kept for existing clients: the id is in the request body. */
	@PutMapping
	@Transactional
	public Reservation updateReservation(@Valid @RequestBody ReservationUpdateRequest request) {
		if (request.getId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id is required");
		}
		return updateReservation(request.getId(), request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Transactional
	public void deleteReservation(@PathVariable Long id) {
		Reservation reservation = getReservation(id);
		LOGGER.info("Deleting reservation {}", id);
		reservationRepository.delete(reservation);
	}

	private Reservation getReservation(Long id) {
		return reservationRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
	}

}
