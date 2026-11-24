package com.bharath.flightreservation.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bharath.flightreservation.entities.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	boolean existsByFlightId(Long flightId);

	List<Reservation> findByPassengerId(Long passengerId);

}
