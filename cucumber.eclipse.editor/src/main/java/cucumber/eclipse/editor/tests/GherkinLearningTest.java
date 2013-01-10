package cucumber.eclipse.editor.tests;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import static org.junit.Assert.*;

import static org.hamcrest.Matchers.*;
import org.junit.Test;

import gherkin.formatter.Formatter;
import gherkin.parser.Parser;
import gherkin.formatter.PrettyFormatter;

public class GherkinLearningTest {
	   @Test public void format() {
	        String gherkin = "" +
	        		"# yadda yadda \n" +
	                " Feature: Hello\n" +
	                "     Big    \n" +
	                "       World  \n" +
	                "               Scenario Outline:\n" +
	                "        Given I have an empty stack\n" +
	                "    When I pøsh <x> onto the stack";
		   
	       ByteArrayOutputStream o = new ByteArrayOutputStream();
	       PrintWriter out = new PrintWriter(o );
		   Formatter formatter = new PrettyFormatter(out, true, false);
	       
		   Parser parser = new Parser(formatter);
	       parser.parse(gherkin, "", 0);
	       
	     out.flush();
	    
	     String should_be = 
	    		   "# yadda yadda\nFeature: Hello\n"
	    		 + "    Big    \n"
	    		 + "      World\n"
	    		 + "\n"
	    		 + "  Scenario Outline: \n"
	    		 + "    Given I have an empty stack\n"
	    		 + "    When I pøsh <x> onto the stack\n";
	    		 
		assertThat(o.toString(), is(should_be));

	     
	   }
	   
}



