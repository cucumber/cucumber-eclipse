package io.cucumber.eclipse.editor.steps;

/**
 * A parameter for a step with additional information
 * 
 * @author christoph
 *
 */
public final class StepParameter {

	private final String parameterName;
	private final ParameterType parameterType;
	private final String[] values;

	/**
	 * @param parameterName the name of the parameter as defined in the source-code
	 * @param parameterType the type of the parameter
	 * @param values        a list of possible values that are valid for this
	 */
	public StepParameter(String parameterName, ParameterType parameterType, String[] values) {
		this.parameterName = parameterName;
		this.parameterType = parameterType;
		this.values = values;
	}

	/**
	 * @return the type of the parameter
	 */
	public ParameterType getParameterType() {
		return parameterType;
	}

	/**
	 * @return the name of the parameter as defined in the corresponding step source
	 */
	public String getParameterName() {
		return parameterName;
	}

	/**
	 * @return a array of possible (valid) values for this parameter or
	 *         <code>null</code> if not available
	 */
	public String[] getValues() {
		return values;
	}
}
