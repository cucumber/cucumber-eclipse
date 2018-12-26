package cucumber.eclipse.steps.jdt.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import cucumber.eclipse.steps.integration.filter.Filter;

public class CompilationUnitStepDefinitionsPreferencesFilter implements Filter<ICompilationUnit> {

	private String[] filters;
	
	public CompilationUnitStepDefinitionsPreferencesFilter(String[] filters) {
		this.filters = filters;
	}
	
	@Override
	public boolean accept(ICompilationUnit element) {
		
		IPackageDeclaration packageDeclaration;
		try {
			packageDeclaration = element.getPackageDeclarations()[0];
		
			String packageName = packageDeclaration.getElementName();
			if(packageName == null) {
				packageName = "";
			}
			
			List<String> typesNames = new ArrayList<String>();
			
			IType[] types = element.getTypes();
			for (IType type : types) {
				typesNames.add(packageName + "." + type.getElementName());
			}
			
			for (String filter : filters) {
				if(typesNames.contains(filter) || Pattern.matches(filter, packageName)) {
					return true;
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}
	
}
