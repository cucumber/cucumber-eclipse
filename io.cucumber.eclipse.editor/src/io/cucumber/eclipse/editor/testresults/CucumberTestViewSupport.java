package io.cucumber.eclipse.editor.testresults;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.text.StringMatcher;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.unittest.launcher.ITestRunnerClient;
import org.eclipse.unittest.model.ITestCaseElement;
import org.eclipse.unittest.model.ITestElement;
import org.eclipse.unittest.model.ITestRunSession;
import org.eclipse.unittest.model.ITestSuiteElement;

/**
 * Connection to the eclipse generic TestViewSupport
 * 
 * @author christoph
 *
 */
public class CucumberTestViewSupport implements org.eclipse.unittest.ui.ITestViewSupport {


	@Override
	public ITestRunnerClient newTestRunnerClient(ITestRunSession session) {
		return new CucumberTestRunnerClient(session);
	}

	@Override
	public Collection<StringMatcher> getTraceExclusionFilterPatterns() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAction getOpenTestAction(Shell shell, ITestCaseElement testCase) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAction getOpenTestAction(Shell shell, ITestSuiteElement testSuite) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAction createOpenEditorAction(Shell shell, ITestElement failure, String traceLine) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Runnable createShowStackTraceInConsoleViewActionDelegate(ITestElement failedTest) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILaunchConfiguration getRerunLaunchConfiguration(List<ITestElement> testElements) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDisplayName() {
		return "Cucumber";
	}

}
