package io.cucumber.eclipse.java.plugins;

import java.util.Arrays;
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
	private final String returnType;
	private String location;

	public CucumberCodeLocation(String location) {
		this.location = location;
		String typeInfo = prefixOfChar(location, '(', true);
		this.returnType = prefixOfChar(typeInfo, ' ', true);
		this.type = prefixOfChar(typeInfo.substring(returnType.length()), '.', false).trim();
		this.methodName = suffixOfChar(typeInfo, '.', false);
		String parameterInfo = prefixOfChar(suffixOfChar(location, '(', true), ')', true);
		if (parameterInfo.isBlank()) {
			parameter = new String[0];
		} else {
			parameter = parameterInfo.split(",");
			for (int i = 0; i < parameter.length; i++) {
				parameter[i] = parameter[i].trim();
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
		return location + " [type=" + type + ", methodName=" + methodName + ", parameter=" + Arrays.toString(parameter)
				+ ", returnType=" + returnType + "]";
	}

	private static String prefixOfChar(String string, char c, boolean first) {
		int index;
		if (first) {
			index = string.indexOf(c);
		} else {
			index = string.lastIndexOf(c);
		}
		return index > 0 ? string.substring(0, index) : "";
	}

	private static String suffixOfChar(String string, char c, boolean first) {
		int index;
		if (first) {
			index = string.indexOf(c);
		} else {
			index = string.lastIndexOf(c);
		}
		return index > 0 ? string.substring(index + 1, string.length()) : "";
	}

	public static void main(String[] args) {
		System.out.println(
				new CucumberCodeLocation("io.cucumber.examples.java.RpnCalculatorSteps.the_result_is(double)"));
		System.out.println(new CucumberCodeLocation(
				"io.cucumber.examples.java.RpnCalculatorSteps.thePreviousEntries(java.util.List<io.cucumber.examples.java.RpnCalculatorSteps$Entry>)"));
		System.out.println(new CucumberCodeLocation(
				"io.cucumber.examples.java.RpnCalculatorSteps.a_calculator_I_just_turned_on()"));
		System.out.println(
				new CucumberCodeLocation("io.cucumber.examples.java.RpnCalculatorSteps.I_press(java.lang.String)"));
		System.out.println(new CucumberCodeLocation("io.cucumber.examples.java.RpnCalculatorSteps.adding(int,int)"));
		System.out.println(
				new CucumberCodeLocation("void io.cucumber.examples.java.RpnCalculatorSteps.givenClientIsAvailable()"));
	}
}
