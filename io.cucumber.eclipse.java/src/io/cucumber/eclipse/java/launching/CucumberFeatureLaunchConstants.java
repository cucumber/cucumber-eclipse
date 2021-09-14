package io.cucumber.eclipse.java.launching;

public interface CucumberFeatureLaunchConstants {

	public static final String TYPE_ID = "cucumber.eclipse.java.launching.localCucumberFeature";

	public static final String CUCUMBER_API_CLI_MAIN = io.cucumber.core.cli.Main.class.getName();
	public static final String ATTR_FEATURE_PATH = "cucumber feature";
	public static final String ATTR_FEATURE_WITH_LINE = "cucumber feature_with_line";
	public static final String ATTR_TAGS = "cucumber tags";
	// TODO adjust
	public static final String ATTR_GLUE_PATH = "glue path";
	public static final String DEFAULT_CLASSPATH = "classpath:";
	public static final String ATTR_IS_MONOCHROME = "is monochrome?";
	public static final String ATTR_IS_PRETTY = "is pretty formatter?";
	public static final String ATTR_IS_HTML = "is html formatter?";
	public static final String ATTR_IS_JSON = "is json formatter?";
	public static final String ATTR_IS_PROGRESS = "is progress formatter?";
	public static final String ATTR_IS_USAGE = "is usage formatter";
	public static final String ATTR_IS_JUNIT = "is junit formatter";
	public static final String ATTR_IS_RERUN = "is rerun formatter";

	// TODO check if we actually should use this...
//	public static final String ATTR_INTERNAL_LAUNCHER = "internal_launcher";
//	public static final boolean DEFAULT_INTERNAL_LAUNCHER = true;

}
