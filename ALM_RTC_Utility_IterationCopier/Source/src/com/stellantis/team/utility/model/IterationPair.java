package com.stellantis.team.utility.model;

public class IterationPair {
	private String display;
	private Object obj;
	
	public IterationPair(String display, Object obj) {
		this.display = display;
		this.obj = obj;
	}

	public String getDisplay() {
		return display;
	}

	public Object getObj() {
		return obj;
	}
	
	@Override
    public String toString() {
        return display;
    }
}
