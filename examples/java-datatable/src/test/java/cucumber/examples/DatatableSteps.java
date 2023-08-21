package cucumber.examples;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import java.util.Map;

import cucumber.examples.datatable.Animals;
import cucumber.examples.datatable.Cat;
import cucumber.examples.datatable.Elephant;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class DatatableSteps {
	
	Animals animal;

	@Given("the animal {string}") 
	public void animalCharacteristics(final String animalToSet, final DataTable tableOfParameters){
		List<Map<String, String>> animalData = tableOfParameters.asMaps(String.class, String.class);
		
		switch (animalToSet) {
		case "Cat" -> {
			 animal = new Cat();
			 animal.setFood("fish");
			 }
		case "Elephant" -> {
			animal = new Elephant();
			animal.setFood("leaves");
			}
		default -> new NoClassDefFoundError("The animal '"+ animalToSet+"' doesn't exist");
		}
		checkAnimalData(animalData);
	}

	private void checkAnimalData(final List<Map<String, String>> animalData) {
		
		for (Map<String, String> data : animalData) {
			String key = data.get("Key");
			assertThat("Key '" + key + "' isn't contain in the available data of all animals",animal.getAvailableDataForAnimals().contains(key));
			assertThat("Key '" + key + "' isn't contain in the available data of '" + animal.getClass().getSimpleName()+"'",animal.getAvailableData().contains(key));
		}
		
	}

	@Then("the food is {string}")
	public void theFoodIs(final String food) {
		assertThat(animal.getFood(),equalTo(food));
	}

}
