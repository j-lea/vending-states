package com.example.vendingstates;

import java.util.EnumSet;

import com.example.vendingstates.VendingStateMachineConfig.VendingEvent;
import com.example.vendingstates.VendingStateMachineConfig.VendingStates;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;


@Configuration
@EnableStateMachine
public class VendingStateMachineConfig extends EnumStateMachineConfigurerAdapter<VendingStates, VendingEvent> {

	enum VendingStates {
		IDLE, WAITING, DELIVERING
	}

	enum VendingEvent {
		@JsonProperty("pay") PAY,
		@JsonProperty("walk") WALK_AWAY,
		@JsonProperty("select") SELECT,
		@JsonProperty("drop") DROP
	}

	@Override
	public void configure(StateMachineConfigurationConfigurer<VendingStates, VendingEvent> config) throws Exception {
		config.withConfiguration().autoStartup(true);
	}

	@Override
	public void configure(StateMachineStateConfigurer<VendingStates, VendingEvent> states) throws Exception {
		states.withStates()
				.initial(VendingStates.IDLE)
				.states(EnumSet.allOf(VendingStates.class));
	}

	@Override
	public void configure(StateMachineTransitionConfigurer<VendingStates, VendingEvent> transitions) throws Exception {
		transitions
				.withExternal()
				.source(VendingStates.IDLE)
				.event(VendingEvent.PAY)
				.guard(moneyGuard())
				.action(showOptions())
				.target(VendingStates.WAITING)
				.and()
				.withExternal()
				.source(VendingStates.WAITING)
				.event(VendingEvent.SELECT)
				.target(VendingStates.DELIVERING)
				.and()
				.withExternal()
				.source(VendingStates.DELIVERING)
				.event(VendingEvent.DROP)
				.target(VendingStates.IDLE);
	}

	@Bean
	public Guard<VendingStates, VendingEvent> moneyGuard() {
		return context -> {
			int amountPaid = (int) context.getExtendedState().getVariables().get("amountPaid");
			return amountPaid > 50;
		};
	}

	@Bean
	public Action<VendingStates, VendingEvent> showOptions() {
		return ctx -> System.out.println("You can get a snickers, twix or bounty for that.");
	}
}
