package cucumber.eclipse.editor.editors;


import static java.util.Collections.emptySet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.junit.Test;

import cucumber.eclipse.editor.Activator;
import cucumber.eclipse.editor.markers.IMarkerManager;
import cucumber.eclipse.editor.preferences.ICucumberPreferenceConstants;
import cucumber.eclipse.editor.steps.IStepProvider;
import cucumber.eclipse.editor.tests.TestFile;
import cucumber.eclipse.steps.integration.IStepListener;
import cucumber.eclipse.steps.integration.Step;
import gherkin.parser.Parser;

public class GherkinErrorMarkerTest {
    
    @Test
    public void stepMarksMissingStepNameLine() throws BadLocationException {
        String source = "Feature: x\n"
                + "\n"
                + "  Scenario: x\n"
                + "    Given "; // keyword at line 3, position 4 - 9
        Document document = new Document(source);
        final AtomicInteger actualCharStart = new AtomicInteger();
        final AtomicInteger actualCharEnd = new AtomicInteger();
        
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setValue(ICucumberPreferenceConstants.PREF_CHECK_STEP_DEFINITIONS, true);
        
        new Parser(new GherkinErrorMarker(newStepProvider(),
                new IMarkerManager() {
            public void removeAll(String type, IFile file) {
            }
            
            public void add(String type, IFile file, int severity, String message, int lineNumber, int charStart, int charEnd) {
                actualCharStart.set(charStart);
                actualCharEnd.set(charEnd);
            }
        }, new TestFile(), document)).parse(source, "", 0);
        
        assertThat("charStart", actualCharStart.get(), is(document.getLineOffset(3)));
        assertThat("charEnd", actualCharEnd.get(), is(document.getLineOffset(3) + 10));
    }

	@Test
	public void stepMarksOnlyUnmatchedStep() throws BadLocationException {
		String source = "Feature: x\n"
				+ "\n"
				+ "  Scenario: x\n"
				+ "    Given x\n"; // step name at line 3, position 10
		Document document = new Document(source);
		final AtomicInteger actualLineNumber = new AtomicInteger();
		final AtomicInteger actualCharStart = new AtomicInteger();
		final AtomicInteger actualCharEnd = new AtomicInteger();

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setValue(ICucumberPreferenceConstants.PREF_CHECK_STEP_DEFINITIONS, true);

		new Parser(new GherkinErrorMarker(newStepProvider(),
				new IMarkerManager() {
					public void removeAll(String type, IFile file) {
					}

					public void add(String type, IFile file, int severity, String message, int lineNumber, int charStart, int charEnd) {
						actualLineNumber.set(lineNumber);
						actualCharStart.set(charStart);
						actualCharEnd.set(charEnd);
					}
				}, new TestFile(), document)).parse(source, "", 0);

		assertThat("lineNumber", actualLineNumber.get(), is(4)); // marker line numbers are 1-based
		assertThat("charStart", actualCharStart.get(), is(document.getLineOffset(3) + 10));
		assertThat("charEnd", actualCharEnd.get(), is(document.getLineOffset(3) + 11));
	}

	@Test
	public void stepMarksNothingWhenOff() throws BadLocationException {
		String source = "Feature: x\n"
				+ "\n"
				+ "  Scenario: x\n"
				+ "    Given x\n";
		Document document = new Document(source);

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setValue(ICucumberPreferenceConstants.PREF_CHECK_STEP_DEFINITIONS, false);

		new Parser(new GherkinErrorMarker(newStepProvider(),
				new IMarkerManager() {
					public void removeAll(String type, IFile file) {
					}

					public void add(String type, IFile file, int severity, String message, int lineNumber, int charStart, int charEnd) {
						assertThat("lookedForStep", false, is(true));
					}
				}, new TestFile(), document)).parse(source, "", 0);
	}

	private IStepProvider newStepProvider() {
		return new IStepProvider() {
			@Override
			public Set<Step> getStepsInEncompassingProject() {
				return emptySet();
			}

			@Override
			public void addStepListener(IStepListener listener) {
			}

			@Override
			public void removeStepListener(IStepListener listener) {
			}
		};
	}
}
