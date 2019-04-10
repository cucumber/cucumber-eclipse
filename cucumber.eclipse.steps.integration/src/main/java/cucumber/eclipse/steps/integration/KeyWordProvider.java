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
	 * @param lang
	 * @return
	 */
	List<String> getStepKeyWords(String lang);
	
	/**
	 * returns a list a scenario related keywords (Feature, Scenario, Scenario Outline, Examples)
	 * @param lang
	 * @return
	 */
	List<String> getGroupingKeyWords(String lang);

}
