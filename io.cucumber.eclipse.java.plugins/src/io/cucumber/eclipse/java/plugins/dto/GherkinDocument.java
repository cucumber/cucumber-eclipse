package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;
import java.util.List;

public class GherkinDocument implements Serializable {
	public String uri;
	public Feature feature;
	public java.util.List<Comment> comments;

	public static class Feature implements Serializable  {
		public Location location;
		public java.util.List<Tag> tags;
		public String language;
		public String keyword;
		public String name;
		public String description;
		public java.util.List<FeatureChild> children;
		public Feature(Location location, List<Tag> tags, String language, String keyword, String name,
				String description, List<FeatureChild> children) {
			this.location = location;
			this.tags = tags;
			this.language = language;
			this.keyword = keyword;
			this.name = name;
			this.description = description;
			this.children = children;
		}
	}

	public static class Tag implements Serializable {
		public Location location;
		public String name;
		public String id;
		public Tag(Location location, String name, String id) {
			this.location = location;
			this.name = name;
			this.id = id;
		}
	}

	public static class FeatureChild implements Serializable {
		public Rule rule;
		public Background background;
		public Scenario scenario;
		public FeatureChild(Rule rule, Background background, Scenario scenario) {
			this.rule = rule;
			this.background = background;
			this.scenario = scenario;
		}
	}

	public static class Rule implements Serializable {
		public Location location;
		public java.util.List<Tag> tags;
		public String keyword;
		public String name;
		public String description;
		public java.util.List<RuleChild> children;
		public String id;
		public Rule(Location location, List<Tag> tags, String keyword, String name, String description,
				List<RuleChild> children, String id) {
			this.location = location;
			this.tags = tags;
			this.keyword = keyword;
			this.name = name;
			this.description = description;
			this.children = children;
			this.id = id;
		}

		
	}

	public static class RuleChild implements Serializable {
		public Background background;
		public Scenario scenario;
		public RuleChild(Background background, Scenario scenario) {
			this.background = background;
			this.scenario = scenario;
		}

	}

	public static class Background implements Serializable {
		public Location location;
		public String keyword;
		public String name;
		public String description;
		public java.util.List<Step> steps;
		public String id;
		public Background(Location location, String keyword, String name, String description, List<Step> steps,
				String id) {
			this.location = location;
			this.keyword = keyword;
			this.name = name;
			this.description = description;
			this.steps = steps;
			this.id = id;
		}
	}

	public static class Step implements Serializable {
		public Location location;
		public String keyword;
		public String text;
		public DocString docString;
		public DataTable dataTable;
		public String id;
		public Step(Location location, String keyword, String text, DocString docString, DataTable dataTable,
				String id) {
			this.location = location;
			this.keyword = keyword;
			this.text = text;
			this.docString = docString;
			this.dataTable = dataTable;
			this.id = id;
		}

	}

	public static class Comment implements Serializable {
	    public Location location;
	    public String text;
		public Comment(Location location, String text) {
			this.location = location;
			this.text = text;
		}
	}
	
	public static class DataTable implements Serializable {
	    public Location location;
	    public java.util.List<TableRow> rows;
		public DataTable(Location location, List<TableRow> rows) {
			this.location = location;
			this.rows = rows;
		}
	}
	
	public static class TableRow implements Serializable {
	    public Location location;
	    public java.util.List<TableCell> cells;
	    public String id;
		public TableRow(Location location, List<TableCell> cells, String id) {
			this.location = location;
			this.cells = cells;
			this.id = id;
		}
	}
	
	public static class TableCell implements Serializable {
	    public Location location;
	    public String value;
		public TableCell(Location location, String value) {
			this.location = location;
			this.value = value;
		}
	}
	
	public static class DocString implements Serializable {
	    public Location location;
	    public String mediaType;
	    public String content;
	    public String delimiter;
		public DocString(Location location, String mediaType, String content, String delimiter) {
			this.location = location;
			this.mediaType = mediaType;
			this.content = content;
			this.delimiter = delimiter;
		}
	}
	
	public static class Scenario implements Serializable {
	    public Location location;
	    public java.util.List<Tag> tags;
	    public String keyword;
	    public String name;
	    public String description;
	    public java.util.List<Step> steps;
	    public java.util.List<Examples> examples;
	    public String id;
		public Scenario(Location location, List<Tag> tags, String keyword, String name, String description,
				List<Step> steps, List<Examples> examples, String id) {
			this.location = location;
			this.tags = tags;
			this.keyword = keyword;
			this.name = name;
			this.description = description;
			this.steps = steps;
			this.examples = examples;
			this.id = id;
		}
	}
	
	public static class Examples implements Serializable {
	    public Location location;
	    public java.util.List<Tag> tags;
	    public String keyword;
	    public String name;
	    public String description;
	    public TableRow tableHeader;
	    public java.util.List<TableRow> tableBody;
	    public String id;
		public Examples(Location location, List<Tag> tags, String keyword, String name, String description,
				TableRow tableHeader, List<TableRow> tableBody, String id) {
			this.location = location;
			this.tags = tags;
			this.keyword = keyword;
			this.name = name;
			this.description = description;
			this.tableHeader = tableHeader;
			this.tableBody = tableBody;
			this.id = id;
		}
	}

	public GherkinDocument(String uri, Feature feature, List<Comment> comments) {
		this.uri = uri;
		this.feature = feature;
		this.comments = comments;
	}
}
