package cucumber.eclipse.steps.jdt.filter;

import java.util.regex.Pattern;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;

import cucumber.eclipse.steps.integration.filter.Filter;

public class MethodStepDefinitionsPreferencesFilter implements Filter<IMethod> {

	private String[] filters;
	
	public MethodStepDefinitionsPreferencesFilter(String[] filters) {
		this.filters = filters;
	}
	
	@Override
	public boolean accept(IMethod method) {
		
		IClassFile classFile = method.getClassFile();
		IJavaElement pkg = classFile.getParent();
	
		String packageName = pkg.getElementName();
		if(packageName == null) {
			packageName = "";
		}
		
		String typeName = pkg.getElementName() + "." + classFile.getElementName(); 
		
		for (String filter : filters) {
			if(typeName.equals(filter) || Pattern.matches(filter, packageName)) {
				return true;
			}
		}
		return false;
	}
	
}
