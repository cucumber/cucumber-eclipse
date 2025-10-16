package io.cucumber.eclipse.editor.syntaxhighlight;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;

public enum GherkinColors {
	COMMENT("cucumber.eclipse.editor.presentation.gherkin_comment_colour"),
	KEYWORD("cucumber.eclipse.editor.presentation.gherkin_keyword_colour"),
	PLACEHOLDER("cucumber.eclipse.editor.presentation.gherkin_placeholder_colour"),
	STRING("cucumber.eclipse.editor.presentation.gherkin_string_colour"),
	NUMERIC("cucumber.eclipse.editor.presentation.gherkin_numeric_literal_colour"),
	STEP("cucumber.eclipse.editor.presentation.gherkin_step_colour"),
	TAG("cucumber.eclipse.editor.presentation.gherkin_tag_colour"),
	DEFAULT("cucumber.eclipse.editor.presentation.gherkin_text_colour");

	private final String id;

	private GherkinColors(String id) {
		this.id = id;
	}

	public void setColor(RGB rgb) {
		getColorRegistry().put(id, rgb);
	}

	public Color getColor() {
		return getColorRegistry().get(id);
	}

	private ColorRegistry getColorRegistry() {
		return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
	}

}
