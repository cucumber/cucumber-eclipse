package io.cucumber.eclipse.java.builder;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;

/**
 * Property tester to check if a project has the Cucumber builder configured.
 * 
 * @author cucumber-eclipse
 */
public class CucumberBuilderPropertyTester extends PropertyTester {

	private static final String HAS_CUCUMBER_BUILDER = "hasCucumberBuilder";

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (HAS_CUCUMBER_BUILDER.equals(property) && receiver instanceof IProject) {
			IProject project = (IProject) receiver;
			return CucumberFeatureBuilder.hasBuilder(project);
		}
		return false;
	}
}
