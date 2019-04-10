package cucumber.eclipse.backends.java;

import java.util.ArrayList;
import java.util.List;

import cucumber.eclipse.steps.integration.KeyWordProvider;
import gherkin.GherkinDialect;
import gherkin.GherkinDialectProvider;
import gherkin.ast.Location;

public class JavaKeywordsProvider implements KeyWordProvider {

	private GherkinDialectProvider dialectProvider;

	public JavaKeywordsProvider() {
		dialectProvider = new GherkinDialectProvider();
	}

	@Override
	public List<String> getStepKeyWords(String lang) {
		GherkinDialect dialect = dialectProvider.getDialect(lang, new Location(-1, -1));
		ArrayList<String> list = new ArrayList<>();
		addAll(dialect.getAndKeywords(), list);
		addAll(dialect.getButKeywords(), list);
		addAll(dialect.getGivenKeywords(), list);
		addAll(dialect.getThenKeywords(), list);
		addAll(dialect.getWhenKeywords(), list);
		return list;
	}
	
	@Override
	public List<String> getGroupingKeyWords(String lang) {
		GherkinDialect dialect = dialectProvider.getDialect(lang, new Location(-1, -1));
		ArrayList<String> list = new ArrayList<>();
		List<String> backgroundKeywords = dialect.getBackgroundKeywords();
		List<String> examplesKeywords = dialect.getExamplesKeywords();
		List<String> featureKeywords = dialect.getFeatureKeywords();
		List<String> scenarioKeywords = dialect.getScenarioKeywords();
		List<String> scenarioOutlineKeywords = dialect.getScenarioOutlineKeywords();
		addAll(backgroundKeywords, list);
		addAll(examplesKeywords, list);
		addAll(featureKeywords, list);
		addAll(scenarioKeywords, list);
		addAll(scenarioOutlineKeywords, list);
		return list;
	}

	private static void addAll(List<String> keywords, List<String> list) {
		for (String word : keywords) {
			String key = word.trim();
			if (!"*".equals(key)) {
				list.add(key);
			}
		}
	}

}
