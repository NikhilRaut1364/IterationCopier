package com.stellantis.team.utility.model;

public enum Status {
	SUCCESSFUL("SUCCESSFUL"), ERROR("ERROR"), WARNING("WARNING"), INFO("INFO");
	
	@SuppressWarnings("unused")
	private String value;
	private Status(String value) {
		this.value = value;
	}
}
