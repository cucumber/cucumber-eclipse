package cucumber.eclipse.editor.filter;

public interface Filter<T> {

	boolean accept(T element);
	
}
