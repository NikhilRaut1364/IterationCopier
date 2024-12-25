package com.stellantis.team.utility.model;

public class KeyValuePair {
	private String display;
    private String value;
    
    public KeyValuePair(String display, String value) {
    	this.display = display;
        this.value = value;
	}
    
    public String getDisplay() {
        return display;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return display;
    }
}
