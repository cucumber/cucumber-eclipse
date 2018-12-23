package cucumber.eclipse.steps.integration.filter;

public interface Filter<T> {

	boolean accept(T element);
	
}
