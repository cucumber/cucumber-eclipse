package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

public final class Envelope implements Serializable{
	public Attachment attachment;
	public GherkinDocument gherkinDocument;
	public Hook hook;
	public Meta meta;
	public ParameterType parameterType;
	public ParseError parseError;
	public Pickle pickle;
	public Source source;
	public StepDefinition stepDefinition;
	public TestCase testCase;
	public TestCaseFinished testCaseFinished;
	public TestCaseStarted testCaseStarted;
	public TestRunFinished testRunFinished;
	public TestRunStarted testRunStarted;
	public TestStepFinished testStepFinished;
	public TestStepStarted testStepStarted;
	public UndefinedParameterType undefinedParameterType;
	
	public Envelope(Attachment attachment, GherkinDocument gherkinDocument, Hook hook, Meta meta,
			ParameterType parameterType, ParseError parseError, Pickle pickle, Source source,
			StepDefinition stepDefinition, TestCase testCase, TestCaseFinished testCaseFinished,
			TestCaseStarted testCaseStarted, TestRunFinished testRunFinished, TestRunStarted testRunStarted,
			TestStepFinished testStepFinished, TestStepStarted testStepStarted,
			UndefinedParameterType undefinedParameterType) {
		super();
		this.attachment = attachment;
		this.gherkinDocument = gherkinDocument;
		this.hook = hook;
		this.meta = meta;
		this.parameterType = parameterType;
		this.parseError = parseError;
		this.pickle = pickle;
		this.source = source;
		this.stepDefinition = stepDefinition;
		this.testCase = testCase;
		this.testCaseFinished = testCaseFinished;
		this.testCaseStarted = testCaseStarted;
		this.testRunFinished = testRunFinished;
		this.testRunStarted = testRunStarted;
		this.testStepFinished = testStepFinished;
		this.testStepStarted = testStepStarted;
		this.undefinedParameterType = undefinedParameterType;
	}
}
