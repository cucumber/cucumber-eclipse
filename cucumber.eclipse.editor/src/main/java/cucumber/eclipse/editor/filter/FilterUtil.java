package cucumber.eclipse.editor.filter;

import java.util.Collection;

public class FilterUtil {

	public static <T> void filter(Collection<T> source, Filter<T> filter, Collection<T> destination) {
	    for (T element: source) {
	        if (filter.accept(element)) {
	            destination.add(element);
	        }
	    }
	}
	
}
