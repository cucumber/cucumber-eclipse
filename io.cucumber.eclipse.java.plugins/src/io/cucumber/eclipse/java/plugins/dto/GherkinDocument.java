package io.cucumber.eclipse.java.plugins.dto;

public class GherkinDocument {
	public String uri;
	public Feature feature;
	public java.util.List<Comment> comments;

	public static class Feature {
		public Location location;
		public java.util.List<Tag> tags;
		public String language;
		public String keyword;
		public String name;
		public String description;
		public java.util.List<FeatureChild> children;
	}

	public static class Tag {
		public Location location;
		public String name;
		public String id;
	}

	public static class FeatureChild {
		public Rule rule;
		public Background background;
		public Scenario scenario;
	}

	public static class Rule {
		public Location location;
		public java.util.List<Tag> tags;
		public String keyword;
		public String name;
		public String description;
		public java.util.List<RuleChild> children;
		public String id;

		
	}

	public static class RuleChild {
		public Background background;
		public Scenario scenario;

	}

	public static class Background {
		public Location location;
		public String keyword;
		public String name;
		public String description;
		public java.util.List<Step> steps;
		public String id;
	}

	public static class Step {
		public Location location;
		public String keyword;
		public String text;
		public DocString docString;
		public DataTable dataTable;
		public String id;

	}

	public static class Comment {
	    public Location location;
	    public String text;
	}
	
	public static class DataTable {
	    public Location location;
	    public java.util.List<TableRow> rows;
	}
	
	public static class TableRow {
	    public Location location;
	    public java.util.List<TableCell> cells;
	    public String id;
	}
	
	public static class TableCell {
	    public Location location;
	    public String value;
	}
	
	public static class DocString {
	    public Location location;
	    public String mediaType;
	    public String content;
	    public String delimiter;
	}
	
	public static class Scenario {
	    public Location location;
	    public java.util.List<Tag> tags;
	    public String keyword;
	    public String name;
	    public String description;
	    public java.util.List<Step> steps;
	    public java.util.List<Examples> examples;
	    public String id;
	}
	
	public static class Examples {
	    public Location location;
	    public java.util.List<Tag> tags;
	    public String keyword;
	    public String name;
	    public String description;
	    public TableRow tableHeader;
	    public java.util.List<TableRow> tableBody;
	    public String id;
	}
}
