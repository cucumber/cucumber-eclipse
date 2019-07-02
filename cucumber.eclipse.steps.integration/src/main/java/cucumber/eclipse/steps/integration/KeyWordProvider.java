package cucumber.eclipse.steps.integration;

import java.util.List;

/**
 * A {@link KeyWordProvider} provides keywords that can be used for content assist
 * 
 * @author Christoph Läubrich
 *
 */
public interface KeyWordProvider {

	/**
	 * Returns a list of step related keywords (And, But, Given, Then, When)
	 * @param lang the language
	 * @return a list of keywords for the specified language
	 */
	List<String> getStepKeyWords(String lang);
	
	/**
	 * returns a list a scenario related keywords (Feature, Scenario, Scenario Outline, Examples)
	 * @param lang the language
	 * @return a list a scenario related keywords (Feature, Scenario, Scenario Outline, Examples)
	 */
	List<String> getGroupingKeyWords(String lang);

}
