package cucumber.eclipse.editor.editors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gherkin.formatter.Formatter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.ExamplesTableRow;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

public class PopupMenuFindStepFormatter implements Formatter {

	private List<String> resolvedStepNames = new ArrayList<String>();

	private int stepLineNumber;

	public PopupMenuFindStepFormatter(int stepLineNumber) {
		this.stepLineNumber = stepLineNumber;
	}

	@Override
	public void background(Background background) { }

	@Override
	public void close() { }

	@Override
	public void done() { }

	@Override
	public void eof() { }

	@Override
	public void examples(Examples examples) {
		if (resolvedStepNames.isEmpty()) {
			return;
		}
		ExamplesTableRow examplesHeader = examples.getRows().get(0);
		for (int i = 1; i < examples.getRows().size(); i++) {
			ExamplesTableRow exampleLine = examples.getRows().get(i);
			String resolvedStep = getResolvedStepForExample(examplesHeader, exampleLine);
			if (!resolvedStepNames.contains(resolvedStep)) {
				resolvedStepNames.add(resolvedStep);
			}
		}
	}

	@Override
	public void feature(Feature feature) { }

	@Override
	public void scenario(Scenario scenario) { }

	@Override
	public void scenarioOutline(ScenarioOutline scenarioOutline) { }

	@Override
	public void step(Step step) {
		String stepString = step.getKeyword() + step.getName();
		if (stepLineNumber == step.getLine()) {
			resolvedStepNames.add(stepString);
		}
	}

	@Override
	public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) { }

	@Override
	public void uri(String uri) { }
	
	private Map<String, String> getExampleVariablesMap(ExamplesTableRow header, ExamplesTableRow values) {
		Map<String, String> result = new LinkedHashMap<String, String>();
		for (int i = 0; i < header.getCells().size(); i++) {
			result.put(header.getCells().get(i), values.getCells().get(i));
		}
		return result;
	}

	private String getResolvedStepForExample(ExamplesTableRow examplesHeader, ExamplesTableRow exampleLine) {
		Map<String, String> variables = getExampleVariablesMap(examplesHeader, exampleLine);
		String stepString = resolvedStepNames.get(0);
		if (variables != null) {
			for (Map.Entry<String, String> variable : variables.entrySet()) {
				stepString = stepString.replace("<" + variable.getKey() + ">", variable.getValue());
			}
		}
		return stepString;
		
	}

	public List<String> getResolvedStepNames() {
		return resolvedStepNames;
	}

	@Override
	public void endOfScenarioLifeCycle(Scenario arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startOfScenarioLifeCycle(Scenario arg0) {
		// TODO Auto-generated method stub
		
	}
}
