package com.example.vendingstates;

import java.util.HashMap;
import java.util.Map;

import com.example.vendingstates.VendingStateMachineConfig.VendingEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { VendingStatesApplication.class })
@WebAppConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class VendingStatesApplicationTests {

	private ObjectMapper mapper;
	private MockMvc mvc;

	@Autowired
	private WebApplicationContext context;

	@BeforeEach
	public void setup() {
		mapper = new ObjectMapper();
		mvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Test
	void initialStateIsIdle() throws Exception {
		mvc.perform(get("/snacks/state"))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("IDLE")));
	}

	@ParameterizedTest
	@ValueSource(strings = {"WALK_AWAY", "SELECT", "DROP" })
	void nonPayEvents_whenStateIsIdle_DoNotChangeState(String eventType) throws Exception {
		EventRequest event = new EventRequest(Enum.valueOf(VendingEvent.class, eventType), new HashMap<>());
		String payJson = mapper.writeValueAsString(event);

		mvc.perform(post("/snacks/events")
				.content(payJson)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		mvc.perform(get("/snacks/state"))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("IDLE")));
	}

	@Test
	void payEvent_whenStateIsIdle_AndAmountIsOver50_makesStateWaiting() throws Exception {
		String payJson = mapper.writeValueAsString(getPayEventRequest(100));

		mvc.perform(post("/snacks/events")
				.content(payJson)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		mvc.perform(get("/snacks/state"))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("WAITING")));
	}

	@Test
	void payEvent_whenStateIsIdle_AndAmountIsUnder50_DoesNotChangeState() throws Exception {
		String payJson = mapper.writeValueAsString(getPayEventRequest(20));

		mvc.perform(post("/snacks/events")
				.content(payJson)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		mvc.perform(get("/snacks/state"))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("IDLE")));
	}

	private EventRequest getPayEventRequest(int amountPaid) {
		Map<String, Object> params = new HashMap<>();
		params.put("amountPaid", amountPaid);

		return new EventRequest(VendingEvent.PAY, params);
	}

	class EventRequest {
		VendingEvent event;
		Map<String, Object> params;

		public EventRequest(VendingEvent event, Map<String, Object> params) {
			this.event = event;
			this.params = params;
		}

		public VendingEvent getEvent() {
			return event;
		}

		public Map<String, Object> getParams() {
			return params;
		}
	}
}
