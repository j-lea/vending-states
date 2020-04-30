package com.example.vendingstates;

import java.util.Map;

import com.example.vendingstates.VendingStateMachineConfig.VendingEvent;

class EventRequest {
	private VendingEvent event;
	private Map<String, String> params;

	public VendingEvent getEvent() {
		return event;
	}

	public void setEvent(VendingEvent event) {
		this.event = event;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}
}
