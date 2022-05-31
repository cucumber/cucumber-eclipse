package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;
import java.util.List;

public class SourceReference implements Serializable{
    public String uri;
    public JavaMethod javaMethod;
    public JavaStackTraceElement javaStackTraceElement;
    public Location location;
    
    public static class JavaMethod implements Serializable{
        public String className;
        public String methodName;
        public java.util.List<String> methodParameterTypes;
		public JavaMethod(String className, String methodName, List<String> methodParameterTypes) {
			this.className = className;
			this.methodName = methodName;
			this.methodParameterTypes = methodParameterTypes;
		}
    }
    
    public static class JavaStackTraceElement implements Serializable{
        public String className;
        public String fileName;
        public String methodName;
		public JavaStackTraceElement(String className, String fileName, String methodName) {
			this.className = className;
			this.fileName = fileName;
			this.methodName = methodName;
		}
    }

	public SourceReference(String uri, JavaMethod javaMethod, JavaStackTraceElement javaStackTraceElement,
			Location location) {
		this.uri = uri;
		this.javaMethod = javaMethod;
		this.javaStackTraceElement = javaStackTraceElement;
		this.location = location;
	}
    
}