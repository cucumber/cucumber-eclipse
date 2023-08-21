package cucumber.examples.datatable;

import java.util.Arrays;
import java.util.List;

public class Animals {
	
	protected List<String> availableData;
	private List<String> availableDataForAnimals = Arrays.asList("Color","Lifespan","Whiskers","Trunk","Tusk");
	private String food;
	
	public Animals() {
		super();
	}
	
	public List<String> getAvailableData() {
		return availableData;
	}

	public List<String> getAvailableDataForAnimals() {
		return availableDataForAnimals;
	}
	
	public String getFood() {
		return food;
	}

	public void setFood(String food) {
		this.food = food;
	}
	
}
