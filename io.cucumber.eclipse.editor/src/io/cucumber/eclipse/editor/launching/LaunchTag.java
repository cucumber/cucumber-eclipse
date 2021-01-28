package io.cucumber.eclipse.editor.launching;

/**
 * 
 * represents a tag that is either to be included or excluded
 * 
 * @author christoph
 *
 */
public class LaunchTag {
	private String tag;
	private boolean include;

	public LaunchTag(String tag, boolean include) {
		this.tag = tag;
		this.include = include;
	}

	public String tag() {
		return tag;
	}

	public boolean include() {
		return include;
	}

	public boolean exclude() {
		return !include;
	}
}
