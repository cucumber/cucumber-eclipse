package cucumber.examples.datatable;

import java.util.Arrays;

public class Cat extends Animals{
	
	private String color;
	private String lifespan;
	private String whiskers;

	public Cat() {
		super();
		availableData = Arrays.asList("Color","Lifespan","Whiskers");
	}
	
	
	public String getColor() {
		return color;
	}
	public String getLifespan() {
		return lifespan;
	}
	
	public String getWhiskers() {
		return whiskers;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public void setLifespan(String lifespan) {
		this.lifespan = lifespan;
	}

	public void setWhiskers(String whiskers) {
		this.whiskers = whiskers;
	}

}
