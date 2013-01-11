package cucumber.eclipse.editor.tests;

public class GherkinTestFixtures {
	public static final String unformatted_feature =
				"" +
				"# yadda yadda \n" +
		        " Feature: Hello\n" +
		        "     Big    \n" +
		        "       World  \n" +
		        "               Scenario Outline:\n" +
		        "        Given I have an empty stack\n" +
		        "    When I pøsh <x> onto the stack";
	
	public static final String formatted_feature =
		   "# yadda yadda\n"
		 + "Feature: Hello\n"
		 + "    Big    \n"
		 + "      World\n"
		 + "\n"
		 + "  Scenario Outline: \n"
		 + "    Given I have an empty stack\n"
		 + "    When I pøsh <x> onto the stack\n";
	
}
