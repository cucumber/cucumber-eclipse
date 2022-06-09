package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;
import java.util.List;

public class GherkinDocument implements Serializable {
	public String uri;
	public Feature feature;
	public java.util.List<Comment> comments;

	public static class Feature implements Serializable  {
		public final Location location;
		public final java.util.List<Tag> tags;
		public final String language;
		public final String keyword;
		public final String name;
		public final String description;
		public final java.util.List<FeatureChild> children;
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
		public final Location location;
		public final String name;
		public final String id;
		public Tag(Location location, String name, String id) {
			this.location = location;
			this.name = name;
			this.id = id;
		}
	}

	public static class FeatureChild implements Serializable {
		public final Rule rule;
		public final Background background;
		public final Scenario scenario;
		public FeatureChild(Rule rule, Background background, Scenario scenario) {
			this.rule = rule;
			this.background = background;
			this.scenario = scenario;
		}
	}

	public static class Rule implements Serializable {
		public final Location location;
		public final java.util.List<Tag> tags;
		public final String keyword;
		public final String name;
		public final String description;
		public final java.util.List<RuleChild> children;
		public final String id;
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
		public final Background background;
		public final Scenario scenario;
		public RuleChild(Background background, Scenario scenario) {
			this.background = background;
			this.scenario = scenario;
		}

	}

	public static class Background implements Serializable {
		public final Location location;
		public final String keyword;
		public final String name;
		public final String description;
		public final java.util.List<Step> steps;
		public final String id;
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
		public final Location location;
		public final String keyword;
		public final String text;
		public final DocString docString;
		public final DataTable dataTable;
		public final String id;
		public final String keywordType;
		public Step(Location location, String keyword,String keywordType, String text, DocString docString, DataTable dataTable,
				String id) {
			this.location = location;
			this.keyword = keyword;
			this.keywordType = keywordType;
			this.text = text;
			this.docString = docString;
			this.dataTable = dataTable;
			this.id = id;
		}

	}

	public static class Comment implements Serializable {
	    public final Location location;
	    public final String text;
		public Comment(Location location, String text) {
			this.location = location;
			this.text = text;
		}
	}
	
	public static class DataTable implements Serializable {
	    public final Location location;
	    public final java.util.List<TableRow> rows;
		public DataTable(Location location, List<TableRow> rows) {
			this.location = location;
			this.rows = rows;
		}
	}
	
	public static class TableRow implements Serializable {
	    public final Location location;
	    public final java.util.List<TableCell> cells;
	    public final String id;
		public TableRow(Location location, List<TableCell> cells, String id) {
			this.location = location;
			this.cells = cells;
			this.id = id;
		}
	}
	
	public static class TableCell implements Serializable {
	    public final Location location;
	    public final String value;
		public TableCell(Location location, String value) {
			this.location = location;
			this.value = value;
		}
	}
	
	public static class DocString implements Serializable {
	    public final Location location;
	    public final String mediaType;
	    public final String content;
	    public final String delimiter;
		public DocString(Location location, String mediaType, String content, String delimiter) {
			this.location = location;
			this.mediaType = mediaType;
			this.content = content;
			this.delimiter = delimiter;
		}
	}
	
	public static class Scenario implements Serializable {
	    public final Location location;
	    public final java.util.List<Tag> tags;
	    public final String keyword;
	    public final String name;
	    public final String description;
	    public final java.util.List<Step> steps;
	    public final java.util.List<Examples> examples;
	    public final String id;
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
	    public final Location location;
	    public final java.util.List<Tag> tags;
	    public final String keyword;
	    public final String name;
	    public final String description;
	    public final TableRow tableHeader;
	    public final java.util.List<TableRow> tableBody;
	    public final String id;
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
