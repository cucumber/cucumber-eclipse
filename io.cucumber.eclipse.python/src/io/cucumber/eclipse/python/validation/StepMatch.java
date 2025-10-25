package io.cucumber.eclipse.python.validation;

/**
 * Represents a matched step with its location in the Python step definition file
 */
public class StepMatch {
	private final int featureLine;
	private final String stepText;
	private final String stepFile;
	private final int stepLine;
	private final String stepPattern;

	public StepMatch(int featureLine, String stepText, String stepFile, int stepLine, String stepPattern) {
		this.featureLine = featureLine;
		this.stepText = stepText;
		this.stepFile = stepFile;
		this.stepLine = stepLine;
		this.stepPattern = stepPattern;
	}

	public int getFeatureLine() {
		return featureLine;
	}

	public String getStepText() {
		return stepText;
	}

	public String getStepFile() {
		return stepFile;
	}

	public int getStepLine() {
		return stepLine;
	}

	public String getStepPattern() {
		return stepPattern;
	}

	@Override
	public String toString() {
		return "StepMatch{featureLine=" + featureLine + ", stepFile=" + stepFile + ", stepLine=" + stepLine + "}";
	}
}
