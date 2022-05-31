package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

public class Meta implements Serializable{
	public String protocolVersion;
	public Product implementation;
	public Product runtime;
	public Product os;
	public Product cpu;
	public Ci ci;

	public static class Ci implements Serializable{
		public String name;
		public String url;
		public String buildNumber;
		public Git git;
		public Ci(String name, String url, String buildNumber, Git git) {
			this.name = name;
			this.url = url;
			this.buildNumber = buildNumber;
			this.git = git;
		}
	}

	public static class Git implements Serializable{
		public String remote;
		public String revision;
		public String branch;
		public String tag;
		public Git(String remote, String revision, String branch, String tag) {
			this.remote = remote;
			this.revision = revision;
			this.branch = branch;
			this.tag = tag;
		}
	}

	public static class Product implements Serializable{
		public String name;
		public String version;
		
		public Product(String name, String version) {
			this.name = name;
			this.version = version;
		}
	}

	public Meta(String protocolVersion, Product implementation, Product runtime, Product os, Product cpu, Ci ci) {
		this.protocolVersion = protocolVersion;
		this.implementation = implementation;
		this.runtime = runtime;
		this.os = os;
		this.cpu = cpu;
		this.ci = ci;
	}
}