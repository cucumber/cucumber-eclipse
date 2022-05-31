package io.cucumber.eclipse.java.plugins.dto;

public class Meta {
	public String protocolVersion;
	public Product implementation;
	public Product runtime;
	public Product os;
	public Product cpu;
	public Ci ci;

	public static class Ci {
		public String name;
		public String url;
		public String buildNumber;
		public Git git;
	}

	public static class Git {
		public String remote;
		public String revision;
		public String branch;
		public String tag;
	}

	public final class Product {
		public String name;
		public String version;
	}
}