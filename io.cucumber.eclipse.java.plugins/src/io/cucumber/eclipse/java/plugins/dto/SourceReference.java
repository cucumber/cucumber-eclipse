package io.cucumber.eclipse.java.plugins.dto;

 public class SourceReference {
    public String uri;
    public JavaMethod javaMethod;
    public JavaStackTraceElement javaStackTraceElement;
    public Location location;
    
    public static class JavaMethod {
        public String className;
        public String methodName;
        public java.util.List<String> methodParameterTypes;
    }
    
    public final class JavaStackTraceElement {
        public String className;
        public String fileName;
        public String methodName;
    }
    
}