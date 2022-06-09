package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;
import java.util.List;

public class Pickle implements Serializable{

	public String id;
    public String uri;
    public String name;
    public String language;
    public java.util.List<PickleStep> steps;
    public java.util.List<PickleTag> tags;
    public java.util.List<String> astNodeIds;

    
    public static class PickleStep implements Serializable{
        public PickleStepArgument argument;
        public java.util.List<String> astNodeIds;
        public String id;
        public String text;
		public String type;
		public PickleStep(PickleStepArgument argument, List<String> astNodeIds, String id,String type, String text) {
			this.argument = argument;
			this.astNodeIds = astNodeIds;
			this.id = id;
			this.text = text;
			this.type = type;
		}
    }
    
    public static class PickleStepArgument implements Serializable {
        public PickleDocString docString;
        public PickleTable dataTable;
		public PickleStepArgument(PickleDocString docString, PickleTable dataTable) {
			this.docString = docString;
			this.dataTable = dataTable;
		}
    }
    
    public static class PickleDocString implements Serializable{
        public String mediaType;
        public String content;
		public PickleDocString(String mediaType, String content) {
			this.mediaType = mediaType;
			this.content = content;
		}
    }
    
    public static class PickleTable implements Serializable{
        public java.util.List<PickleTableRow> rows;

		public PickleTable(List<PickleTableRow> rows) {
			this.rows = rows;
		}
    }
    
    public static class PickleTableRow implements Serializable {
        public java.util.List<PickleTableCell> cells;

		public PickleTableRow(List<PickleTableCell> cells) {
			this.cells = cells;
		}
    }
    
    public static class PickleTableCell implements Serializable{
        public String value;

		public PickleTableCell(String value) {
			this.value = value;
		}
    }
    
    public static class PickleTag implements Serializable{
        public String name;
        public String astNodeId;
		public PickleTag(String name, String astNodeId) {
			this.name = name;
			this.astNodeId = astNodeId;
		}
    }

	public Pickle(String id, String uri, String name, String language, List<PickleStep> steps, List<PickleTag> tags,
			List<String> astNodeIds) {
		this.id = id;
		this.uri = uri;
		this.name = name;
		this.language = language;
		this.steps = steps;
		this.tags = tags;
		this.astNodeIds = astNodeIds;
	}
}
