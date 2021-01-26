package io.cucumber.eclipse.java.plugins;

import java.util.Objects;

/**
 * Represents a cucumber code location and parses the provided string into
 * usable pieces
 * 
 * @author christoph
 *
 */
public final class CucumberCodeLocation {

	private final String type;
	private final String methodName;
	private final String[] parameter;
	private String location;

	public CucumberCodeLocation(String location) {
		// examples:
		// io.cucumber.examples.java.RpnCalculatorSteps.the_result_is(double)
		// io.cucumber.examples.java.RpnCalculatorSteps.thePreviousEntries(java.util.List<io.cucumber.examples.java.RpnCalculatorSteps$Entry>)
		// io.cucumber.examples.java.RpnCalculatorSteps.a_calculator_I_just_turned_on()
		// io.cucumber.examples.java.RpnCalculatorSteps.I_press(java.lang.String)
		// io.cucumber.examples.java.RpnCalculatorSteps.adding(int,int)
		this.location = location;
		int braceIndex = location.indexOf('(');
		if (braceIndex > 0) {
			String typeInfo = location.substring(0, braceIndex);
			int indexOf = typeInfo.lastIndexOf('.');
			type = indexOf > 0 ? typeInfo.substring(0, indexOf) : "";
		} else {
			type = "";
		}
		{
			String methodInfo = location.substring(type.length() + 1);
			int indexOf = methodInfo.indexOf('(');
			if (indexOf > 0) {
				methodName = methodInfo.substring(0, indexOf);
				int endIndex = methodInfo.indexOf(')', indexOf);
				if (endIndex > 0) {
					String parameterInfo = methodInfo.substring(indexOf + 1, endIndex);
					if (parameterInfo.isBlank()) {
						parameter = new String[0];
					} else {
						parameter = parameterInfo.split(",");
						for (int i = 0; i < parameter.length; i++) {
							parameter[i] = parameter[i].trim();
						}
					}
				} else {
					parameter = new String[0];
				}
			} else {
				methodName = "";
				parameter = new String[0];
			}
		}
	}

	public String getTypeName() {
		return type;
	}

	public String getMethodName() {
		return methodName;
	}

	public String[] getParameter() {
		return parameter;
	}

	@Override
	public int hashCode() {
		return Objects.hash(location);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CucumberCodeLocation other = (CucumberCodeLocation) obj;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return location;
	}
}
