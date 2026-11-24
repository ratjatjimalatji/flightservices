package com.bharath.flightreservation;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.bharath.flightreservation.entities.Flight;
import com.bharath.flightreservation.exception.ConflictException;
import com.bharath.flightreservation.exception.ResourceNotFoundException;
import com.bharath.flightreservation.repos.FlightRepository;
import com.bharath.flightreservation.repos.ReservationRepository;

@RestController
@CrossOrigin
@RequestMapping("/flights")
public class FlightRestController {

	private static final Logger LOGGER = LoggerFactory.getLogger(FlightRestController.class);

	private final FlightRepository flightRepository;
	private final ReservationRepository reservationRepository;

	public FlightRestController(FlightRepository flightRepository, ReservationRepository reservationRepository) {
		this.flightRepository = flightRepository;
		this.reservationRepository = reservationRepository;
	}

	/**
	 * Lists all flights, or searches when from, to and departureDate
	 * (yyyy-MM-dd) are all given.
	 */
	@GetMapping
	public List<Flight> findFlights(@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate) {
		if (from == null && to == null && departureDate == null) {
			return flightRepository.findAll();
		}
		if (from == null || to == null || departureDate == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Search needs all of: from, to, departureDate (yyyy-MM-dd)");
		}
		LOGGER.info("Searching flights from {} to {} on {}", from, to, departureDate);
		return flightRepository.findFlights(from, to, departureDate);
	}

	@GetMapping("/{id}")
	public Flight findFlight(@PathVariable Long id) {
		return getFlight(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Flight createFlight(@Valid @RequestBody Flight flight) {
		flight.setId(null);
		LOGGER.info("Creating flight: {}", flight);
		return flightRepository.save(flight);
	}

	@PutMapping("/{id}")
	@Transactional
	public Flight updateFlight(@PathVariable Long id, @Valid @RequestBody Flight request) {
		Flight flight = getFlight(id);
		flight.setFlightNumber(request.getFlightNumber());
		flight.setOperatingAirlines(request.getOperatingAirlines());
		flight.setDepartureCity(request.getDepartureCity());
		flight.setArrivalCity(request.getArrivalCity());
		flight.setDateOfDeparture(request.getDateOfDeparture());
		flight.setEstimatedDepartureTime(request.getEstimatedDepartureTime());
		LOGGER.info("Updating flight: {}", flight);
		return flightRepository.save(flight);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Transactional
	public void deleteFlight(@PathVariable Long id) {
		Flight flight = getFlight(id);
		if (reservationRepository.existsByFlightId(id)) {
			throw new ConflictException("Flight " + id + " has reservations; delete them first");
		}
		LOGGER.info("Deleting flight {}", id);
		flightRepository.delete(flight);
	}

	private Flight getFlight(Long id) {
		return flightRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Flight", id));
	}

}
