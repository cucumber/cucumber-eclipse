package cucumber.eclipse.editor.editors;


public enum GherkinColors {
	COMMENT	("cucumber.eclipse.editor.presentation.gherkin_comment_colour"), 
	KEYWORD	("cucumber.eclipse.editor.presentation.gherkin_keyword_colour"), 
	PLACEHOLDER	("cucumber.eclipse.editor.presentation.gherkin_placeholder_colour"),
	STRING	("cucumber.eclipse.editor.presentation.gherkin_string_colour"), 
	NUMERIC	("cucumber.eclipse.editor.presentation.gherkin_numeric_literal_colour"), 
	STEP	("cucumber.eclipse.editor.presentation.gherkin_step_colour"), 
	TAG		("cucumber.eclipse.editor.presentation.gherkin_tag_colour"), 
	DEFAULT	("cucumber.eclipse.editor.presentation.gherkin_text_colour");
	
	public final String COLOR_PREFERENCE_ID;
	private GherkinColors(String id) {
		COLOR_PREFERENCE_ID = id;
	}
}
