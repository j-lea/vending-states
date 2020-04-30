package com.example.vendingstates;

import com.example.vendingstates.VendingStateMachineConfig.VendingEvent;
import com.example.vendingstates.VendingStateMachineConfig.VendingStates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/snacks")
public class VendingMachineApi {

	@Autowired
	private StateMachine<VendingStates, VendingEvent> stateMachine;

	@GetMapping("/state")
	public String getState() {
		return stateMachine.getState().getId().toString();
	}

	@PostMapping("/events")
	public void events(@RequestBody EventRequest eventRequest) {
		stateMachine.getExtendedState()
				.getVariables()
				.putAll(eventRequest.getParams());

		stateMachine.sendEvent(eventRequest.getEvent());
	}
}
