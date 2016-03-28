package cucumber.eclipse.editor.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class StringUtils {

	public static List<String> stringToList(String s)
	  {
	    /*List<String> result = Lists.newArrayList();*/
		List<String> result = new ArrayList();
	    if (s != null)
	    {
	      String[] a = s.split("[,]+");
	      for (int i = 0; i < a.length; i++) {
	        if (a[i].trim().length() > 0) {
	          result.add(a[i]);
	        }
	      }
	    }
	    return result;
	  }
	  
	  public static List<String> stringToNullList(String s)
	  {
	    List<String> result = stringToList(s);
	    
	    return result.isEmpty() ? null : result;
	  }
	  
	  public static boolean isEmptyString(String content)
	  {
	    return (content == null) || (content.trim().length() == 0);
	  }
	  
	  public static String listToString(Collection<String> l)
	  {
	    StringBuffer result = new StringBuffer();
	    if (l != null)
	    {
	      Iterator<String> iter = l.iterator();
	      while (iter.hasNext())
	      {
	        String s = (String)iter.next();
	        result.append(s);
	        if (iter.hasNext()) {
	          result.append(",");
	        }
	      }
	    }
	    return result.toString().trim();
	  }
}
