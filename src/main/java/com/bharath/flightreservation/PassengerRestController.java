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

import com.bharath.flightreservation.entities.Passenger;
import com.bharath.flightreservation.exception.ResourceNotFoundException;
import com.bharath.flightreservation.repos.PassengerRepository;
import com.bharath.flightreservation.repos.ReservationRepository;

@RestController
@CrossOrigin
@RequestMapping("/passengers")
public class PassengerRestController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PassengerRestController.class);

	private final PassengerRepository passengerRepository;
	private final ReservationRepository reservationRepository;

	public PassengerRestController(PassengerRepository passengerRepository,
			ReservationRepository reservationRepository) {
		this.passengerRepository = passengerRepository;
		this.reservationRepository = reservationRepository;
	}

	@GetMapping
	public List<Passenger> findPassengers() {
		return passengerRepository.findAll();
	}

	@GetMapping("/{id}")
	public Passenger findPassenger(@PathVariable Long id) {
		return getPassenger(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Passenger createPassenger(@Valid @RequestBody Passenger passenger) {
		passenger.setId(null);
		LOGGER.info("Creating passenger: {}", passenger);
		return passengerRepository.save(passenger);
	}

	@PutMapping("/{id}")
	@Transactional
	public Passenger updatePassenger(@PathVariable Long id, @Valid @RequestBody Passenger request) {
		Passenger passenger = getPassenger(id);
		passenger.setFirstName(request.getFirstName());
		passenger.setLastName(request.getLastName());
		passenger.setMiddleName(request.getMiddleName());
		passenger.setEmail(request.getEmail());
		passenger.setPhone(request.getPhone());
		LOGGER.info("Updating passenger: {}", passenger);
		return passengerRepository.save(passenger);
	}

	/**
	 * Deleting a passenger also deletes their reservations (same behaviour as
	 * ON DELETE CASCADE in flightdb.sql).
	 */
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Transactional
	public void deletePassenger(@PathVariable Long id) {
		Passenger passenger = getPassenger(id);
		LOGGER.info("Deleting passenger {} and their reservations", id);
		reservationRepository.deleteAll(reservationRepository.findByPassengerId(id));
		passengerRepository.delete(passenger);
	}

	private Passenger getPassenger(Long id) {
		return passengerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Passenger", id));
	}

}
