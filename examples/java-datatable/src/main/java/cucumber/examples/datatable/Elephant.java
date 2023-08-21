package cucumber.examples.datatable;

import java.util.Arrays;

public class Elephant extends Animals {
	
	private String color;
	private String lifespan;
	private String trunk;
	private String tusk;
	
	public Elephant(){
		super();
		availableData = Arrays.asList("Color","Lifespan","Trunk","Tusk");
	}
	
	public String getColor() {
		return color;
	}

	public String getLifespan() {
		return lifespan;
	}

	public String getTrunk() {
		return trunk;
	}

	public String getTusk() {
		return tusk;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public void setLifespan(String lifespan) {
		this.lifespan = lifespan;
	}

	public void setTrunk(String trunk) {
		this.trunk = trunk;
	}

	public void setTusk(String tusk) {
		this.tusk = tusk;
	}

}
