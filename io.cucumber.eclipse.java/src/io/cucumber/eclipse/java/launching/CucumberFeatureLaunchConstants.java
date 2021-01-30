package io.cucumber.eclipse.java.launching;

public interface CucumberFeatureLaunchConstants {

	public static final String CUCUMBER_API_CLI_MAIN = io.cucumber.core.cli.Main.class.getName();
	// TODO adjust
//	public static final String CUCUMBER_FEATURE_RUNNER = "Cucumber Feature Runner";
	public static final String ATTR_FEATURE_PATH = "cucumber feature";
	public static final String ATTR_GLUE_PATH = "glue path";
	public static final String DEFAULT_CLASSPATH = "classpath:";
//	public static final String CUCUMBER_FEATURE_LAUNCH_CONFIG_TYPE = "cucumber.eclipse.launching.localCucumberFeature";
	public static final String ATTR_IS_MONOCHROME = "is monochrome?";
	public static final String ATTR_IS_PRETTY = "is pretty formatter?";
	public static final String ATTR_IS_HTML = "is html formatter?";
	public static final String ATTR_IS_JSON = "is json formatter?";
	public static final String ATTR_IS_PROGRESS = "is progress formatter?";
	public static final String ATTR_IS_USAGE = "is usage formatter";
	public static final String ATTR_IS_JUNIT = "is junit formatter";
	public static final String ATTR_IS_RERUN = "is rerun formatter";
	public static final String ATTR_INTERNAL_LAUNCHER = "internal_launcher";
	public static final boolean DEFAULT_INTERNAL_LAUNCHER = true;
	

}
