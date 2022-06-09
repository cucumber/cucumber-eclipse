package io.cucumber.eclipse.java.plugins.dto;

import java.io.Serializable;

public final class Envelope implements Serializable{
	public final Attachment attachment;
	public final GherkinDocument gherkinDocument;
	public final Hook hook;
	public final Meta meta;
	public final ParameterType parameterType;
	public final ParseError parseError;
	public final Pickle pickle;
	public final Source source;
	public final StepDefinition stepDefinition;
	public final TestCase testCase;
	public final TestCaseFinished testCaseFinished;
	public final TestCaseStarted testCaseStarted;
	public final TestRunFinished testRunFinished;
	public final TestRunStarted testRunStarted;
	public final TestStepFinished testStepFinished;
	public final TestStepStarted testStepStarted;
	public final UndefinedParameterType undefinedParameterType;
	
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
