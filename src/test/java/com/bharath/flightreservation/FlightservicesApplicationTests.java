package com.bharath.flightreservation;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FlightservicesApplicationTests {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	private static final String FLIGHT_JSON = "{\"flightNumber\":\"AA1\",\"operatingAirlines\":\"American Airlines\","
			+ "\"departureCity\":\"AUS\",\"arrivalCity\":\"NYC\",\"dateOfDeparture\":\"2018-02-05\","
			+ "\"estimatedDepartureTime\":\"2018-02-05T03:14:07\"}";

	private long createFlight() throws Exception {
		return idOf(mvc.perform(post("/flights").contentType(MediaType.APPLICATION_JSON).content(FLIGHT_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.dateOfDeparture").value("2018-02-05"))
				.andReturn());
	}

	private long idOf(MvcResult result) throws Exception {
		JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
		return node.get("id").asLong();
	}

	@Test
	void flightCrud() throws Exception {
		long id = createFlight();

		mvc.perform(get("/flights/" + id)).andExpect(status().isOk())
				.andExpect(jsonPath("$.flightNumber").value("AA1"));

		mvc.perform(get("/flights").param("from", "AUS").param("to", "NYC").param("departureDate", "2018-02-05"))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
		mvc.perform(get("/flights").param("from", "AUS").param("to", "NYC").param("departureDate", "2018-02-06"))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
		mvc.perform(get("/flights").param("from", "AUS")).andExpect(status().isBadRequest());

		mvc.perform(put("/flights/" + id).contentType(MediaType.APPLICATION_JSON)
				.content(FLIGHT_JSON.replace("AA1", "AA9"))).andExpect(status().isOk())
				.andExpect(jsonPath("$.flightNumber").value("AA9"));

		mvc.perform(delete("/flights/" + id)).andExpect(status().isNoContent());
		mvc.perform(get("/flights/" + id)).andExpect(status().isNotFound());
	}

	@Test
	void flightValidation() throws Exception {
		mvc.perform(post("/flights").contentType(MediaType.APPLICATION_JSON).content("{\"flightNumber\":\"X\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void passengerCrud() throws Exception {
		long id = idOf(mvc.perform(post("/passengers").contentType(MediaType.APPLICATION_JSON)
				.content("{\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"email\":\"jane@example.com\"}"))
				.andExpect(status().isCreated()).andReturn());

		mvc.perform(get("/passengers")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

		mvc.perform(put("/passengers/" + id).contentType(MediaType.APPLICATION_JSON)
				.content("{\"firstName\":\"Janet\",\"lastName\":\"Doe\"}")).andExpect(status().isOk())
				.andExpect(jsonPath("$.firstName").value("Janet"));

		mvc.perform(delete("/passengers/" + id)).andExpect(status().isNoContent());
		mvc.perform(get("/passengers/" + id)).andExpect(status().isNotFound());
	}

	@Test
	void reservationCrud() throws Exception {
		long flightId = createFlight();

		long id = idOf(mvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
				.content("{\"flightId\":" + flightId + ",\"passengerFirstName\":\"John\",\"passengerLastName\":\"Smith\","
						+ "\"passengerEmail\":\"john@example.com\",\"cardNumber\":\"4111111111111111\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.checkedIn").value(false))
				.andExpect(jsonPath("$.flight.id").value(flightId))
				.andExpect(jsonPath("$.passenger.firstName").value("John"))
				.andReturn());

		mvc.perform(get("/reservations")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

		// Original endpoint: id in the body
		mvc.perform(put("/reservations").contentType(MediaType.APPLICATION_JSON)
				.content("{\"id\":" + id + ",\"checkedIn\":true,\"numberOfBags\":2}")).andExpect(status().isOk())
				.andExpect(jsonPath("$.checkedIn").value(true)).andExpect(jsonPath("$.numberOfBags").value(2));

		// Partial update by path: checkedIn stays true
		mvc.perform(put("/reservations/" + id).contentType(MediaType.APPLICATION_JSON)
				.content("{\"numberOfBags\":3}")).andExpect(status().isOk())
				.andExpect(jsonPath("$.checkedIn").value(true)).andExpect(jsonPath("$.numberOfBags").value(3));

		// A flight with reservations can't be deleted
		mvc.perform(delete("/flights/" + flightId)).andExpect(status().isConflict());

		mvc.perform(delete("/reservations/" + id)).andExpect(status().isNoContent());
		mvc.perform(get("/reservations/" + id)).andExpect(status().isNotFound());
		mvc.perform(delete("/flights/" + flightId)).andExpect(status().isNoContent());
	}

	@Test
	void reservationErrors() throws Exception {
		mvc.perform(get("/reservations/999")).andExpect(status().isNotFound());
		mvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
				.content("{\"flightId\":999,\"passengerFirstName\":\"A\",\"passengerLastName\":\"B\"}"))
				.andExpect(status().isNotFound());
		mvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deletingPassengerRemovesTheirReservations() throws Exception {
		long flightId = createFlight();
		long reservationId = idOf(mvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
				.content("{\"flightId\":" + flightId + ",\"passengerFirstName\":\"A\",\"passengerLastName\":\"B\"}"))
				.andExpect(status().isCreated()).andReturn());
		long passengerId = mapper.readTree(mvc.perform(get("/reservations/" + reservationId)).andReturn()
				.getResponse().getContentAsString()).at("/passenger/id").asLong();

		mvc.perform(delete("/passengers/" + passengerId)).andExpect(status().isNoContent());
		mvc.perform(get("/reservations/" + reservationId)).andExpect(status().isNotFound());
	}

}
