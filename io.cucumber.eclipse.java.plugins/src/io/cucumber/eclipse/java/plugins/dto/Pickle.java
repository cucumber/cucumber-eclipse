package io.cucumber.eclipse.java.plugins.dto;

public class Pickle {

	public String id;
    public String uri;
    public String name;
    public String language;
    public java.util.List<PickleStep> steps;
    public java.util.List<PickleTag> tags;
    public java.util.List<String> astNodeIds;

    
    public static class PickleStep {
        public PickleStepArgument argument;
        public java.util.List<String> astNodeIds;
        public String id;
        public String text;
    }
    
    public static class PickleStepArgument {
        public PickleDocString docString;
        public PickleTable dataTable;
    }
    
    public static class PickleDocString {
        public String mediaType;
        public String content;
    }
    
    public static class PickleTable {
        public java.util.List<PickleTableRow> rows;
    }
    
    public static class PickleTableRow {
        public java.util.List<PickleTableCell> cells;
    }
    
    public static class PickleTableCell {
        public String value;
    }
    
    public static class PickleTag {
        public String name;
        public String astNodeId;
    }
}
