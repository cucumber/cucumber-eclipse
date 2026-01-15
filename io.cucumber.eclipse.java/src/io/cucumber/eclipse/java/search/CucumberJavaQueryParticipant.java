package io.cucumber.eclipse.java.search;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.ui.search.ElementQuerySpecification;
import org.eclipse.jdt.ui.search.IMatchPresentation;
import org.eclipse.jdt.ui.search.IQueryParticipant;
import org.eclipse.jdt.ui.search.ISearchRequestor;
import org.eclipse.jdt.ui.search.QuerySpecification;
import org.eclipse.search.ui.text.Match;

import io.cucumber.eclipse.editor.ResourceHelper;
import io.cucumber.eclipse.editor.document.GherkinEditorDocument;
import io.cucumber.eclipse.editor.document.GherkinEditorDocumentManager;
import io.cucumber.eclipse.java.Activator;
import io.cucumber.eclipse.java.JDTUtil;
import io.cucumber.eclipse.java.plugins.CucumberCodeLocation;
import io.cucumber.eclipse.java.plugins.MatchedStep;
import io.cucumber.eclipse.java.preferences.CucumberJavaPreferences;
import io.cucumber.eclipse.java.validation.GlueSteps;
import io.cucumber.eclipse.java.validation.JavaGlueJob;
import io.cucumber.eclipse.java.validation.JavaGlueStore;

/**
 * Search participant that finds feature file references to Cucumber step definitions.
 * <p>
 * This participant integrates with Eclipse's Java search to show where step definition
 * methods are used in Gherkin feature files. When searching for references to a method
 * annotated with Cucumber annotations (Given, When, Then, etc.), this participant:
 * <ol>
 * <li>Identifies all relevant Java projects in the search scope</li>
 * <li>Collects all feature files from those projects</li>
 * <li>Uses the existing glue validation infrastructure to access matched steps</li>
 * <li>Reports matches back to the search as references in feature files</li>
 * </ol>
 * </p>
 * 
 * @author christoph
 */
public class CucumberJavaQueryParticipant implements IQueryParticipant {

	@Override
	public void search(ISearchRequestor requestor, QuerySpecification querySpecification, IProgressMonitor monitor)
			throws CoreException {
		
		// Only handle element queries (not pattern-based searches)
		if (!(querySpecification instanceof ElementQuerySpecification)) {
			return;
		}
		
		ElementQuerySpecification elementQuery = (ElementQuerySpecification) querySpecification;
		IJavaElement element = elementQuery.getElement();
		
		// Only process methods - check if it's a Cucumber step definition happens during matching
		if (!(element instanceof IMethod)) {
			return;
		}
		
		IMethod method = (IMethod) element;
		
		// Early exit: check if the method has any Cucumber annotations
		try {
			if (!JDTUtil.hasCucumberAnnotation(method)) {
				return;
			}
		} catch (JavaModelException e) {
			// If we can't read annotations, log warning and continue
			// to avoid false negatives
			ILog.get().warn("Could not read annotations for method: " + method.getElementName(), e);
		}
		
		// Get the JavaGlueStore service from Activator
		JavaGlueStore glueStore = Activator.getJavaGlueStore();
		if (glueStore == null) {
			// Service not available, can't perform search
			return;
		}
		
		// Step 1: Collect all relevant projects from the search scope
		IJavaSearchScope scope = querySpecification.getScope();
		Set<IJavaProject> relevantProjects = collectRelevantProjects(scope);
		
		if (relevantProjects.isEmpty()) {
			return;
		}
		
		monitor.beginTask("Searching Cucumber feature files", relevantProjects.size());
		
		try {
			// Process each project
			for (IJavaProject javaProject : relevantProjects) {
				if (monitor.isCanceled()) {
					return;
				}
				
				monitor.subTask("Searching in project: " + javaProject.getElementName());
				
				// Step 2: Collect all feature files in the project
				Set<IFile> featureFiles = collectFeatureFiles(javaProject.getProject());
				
				if (featureFiles.isEmpty()) {
					monitor.worked(1);
					continue;
				}
				
				// Step 3: Create editor documents for all feature files
				Collection<GherkinEditorDocument> documents = new HashSet<>();
				
				for (IFile featureFile : featureFiles) {
					if (monitor.isCanceled()) {
						return;
					}
					
					GherkinEditorDocument editorDoc = GherkinEditorDocumentManager.get(featureFile, false);
					if (editorDoc != null) {
						documents.add(editorDoc);
					}
				}
				
				if (documents.isEmpty()) {
					monitor.worked(1);
					continue;
				}
				
				// Step 4: Try to match using glue store (fast path for already-validated documents)
				Collection<GherkinEditorDocument> unmatchedDocuments = 
						searchInGlueStore(requestor, glueStore, javaProject, method, documents, monitor);
				
				// Step 5: For documents not found in glue store, search by running Cucumber
				if (!unmatchedDocuments.isEmpty()) {
					searchInFeatureFiles(requestor, javaProject, method, unmatchedDocuments, monitor);
				}
				
				monitor.worked(1);
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Searches for references using the existing glue store (fast path).
	 * <p>
	 * This method checks the JavaGlueStore for already-validated step matches.
	 * This is fast because it reuses existing validation results from open editors.
	 * </p>
	 * 
	 * @param requestor the search requestor to report matches to
	 * @param glueStore the glue store containing validated step matches
	 * @param javaProject the Java project being searched
	 * @param method the target step definition method
	 * @param documents all documents to search
	 * @param monitor progress monitor
	 * @return collection of documents that couldn't be matched (not in glue store)
	 */
	private Collection<GherkinEditorDocument> searchInGlueStore(ISearchRequestor requestor, JavaGlueStore glueStore,
			IJavaProject javaProject, IMethod method, Collection<GherkinEditorDocument> documents, 
			IProgressMonitor monitor) {
		
		Collection<GherkinEditorDocument> unmatchedDocuments = new HashSet<>();
		
		try {
			for (GherkinEditorDocument editorDoc : documents) {
				if (monitor.isCanceled()) {
					break;
				}
				
				// Get matched steps from the glue store
				Collection<MatchedStep<?>> matchedSteps = glueStore.getMatchedSteps(editorDoc.getDocument());
				
				if (matchedSteps == null || matchedSteps.isEmpty()) {
					// No matches in glue store - needs full validation
					unmatchedDocuments.add(editorDoc);
					continue;
				}
				
				// Check if this document's steps match our target method
				checkMatchedSteps(requestor, javaProject, method, editorDoc, matchedSteps, monitor);
			}
			
		} catch (Exception e) {
			ILog.get().error("Error searching in glue store for project: " + 
					javaProject.getElementName(), e);
		}
		
		return unmatchedDocuments;
	}

	/**
	 * Searches for references by running Cucumber validation (slow path).
	 * <p>
	 * This method is called for documents that aren't in the glue store
	 * (e.g., feature files not yet opened in an editor). It will run
	 * Cucumber in dry-run mode to validate and match steps.
	 * </p>
	 * 
	 * @param requestor the search requestor to report matches to
	 * @param javaProject the Java project being searched
	 * @param method the target step definition method
	 * @param documents documents to search that weren't in glue store
	 * @param monitor progress monitor
	 */
	private void searchInFeatureFiles(ISearchRequestor requestor, IJavaProject javaProject, 
			IMethod method, Collection<GherkinEditorDocument> documents, IProgressMonitor monitor) {
		
		try {
			// Get project preferences (but ignore validation plugins - we only want matches)
			CucumberJavaPreferences preferences = CucumberJavaPreferences.of(javaProject.getProject());
			
			// Run Cucumber validation to get matched steps
			// We pass empty set for validation plugins since we only care about finding matches, not validation
			Map<GherkinEditorDocument, GlueSteps> glueStepsByDocument = 
					JavaGlueJob.validateGlue(documents, javaProject, preferences, Collections.emptySet(), monitor);
			
			// Check each document's matched steps
			for (Map.Entry<GherkinEditorDocument, GlueSteps> entry : glueStepsByDocument.entrySet()) {
				if (monitor.isCanceled()) {
					break;
				}
				
				GherkinEditorDocument editorDoc = entry.getKey();
				GlueSteps glueSteps = entry.getValue();
				
				// Get matched steps from validation results
				Collection<MatchedStep<?>> matchedSteps = glueSteps.matchedSteps();
				
				if (matchedSteps != null && !matchedSteps.isEmpty()) {
					// Check if this document's steps match our target method
					checkMatchedSteps(requestor, javaProject, method, editorDoc, matchedSteps, monitor);
				}
			}
			
		} catch (Exception e) {
			ILog.get().error("Error searching in feature files for project: " + 
					javaProject.getElementName(), e);
		}
	}

	/**
	 * Checks matched steps to see if any reference the target method.
	 * <p>
	 * This is the common logic used by both fast path (glue store) and slow path (Cucumber validation).
	 * </p>
	 * 
	 * @param requestor the search requestor to report matches to
	 * @param javaProject the Java project being searched
	 * @param method the target step definition method
	 * @param editorDoc the document being checked
	 * @param matchedSteps the matched steps for this document
	 * @param monitor progress monitor
	 */
	private void checkMatchedSteps(ISearchRequestor requestor, IJavaProject javaProject, 
			IMethod method, GherkinEditorDocument editorDoc, Collection<MatchedStep<?>> matchedSteps,
			IProgressMonitor monitor) {
		
		for (MatchedStep<?> matchedStep : matchedSteps) {
			if (monitor.isCanceled()) {
				break;
			}
			
			CucumberCodeLocation codeLocation = matchedStep.getCodeLocation();
			
			if (codeLocation != null) {
				// Try to resolve the method from the code location
				try {
					IMethod[] resolvedMethods = JDTUtil.resolveMethod(javaProject, codeLocation, monitor);
					
					// Check if our target method is in the resolved methods
					if (resolvedMethods != null) {
						for (IMethod resolvedMethod : resolvedMethods) {
							if (method.equals(resolvedMethod)) {
								// Create a match at the step location in the feature file
								int lineNumber = matchedStep.getLocation().getLine();
								Match match = new CucumberStepMatch(editorDoc.getResource(), lineNumber, matchedStep);
								requestor.reportMatch(match);
								break;
							}
						}
					}
				} catch (Exception e) {
					// Ignore resolution errors and continue
				}
			}
		}
	}

	/**
	 * Collects all Java projects that are relevant to the search scope.
	 */
	private Set<IJavaProject> collectRelevantProjects(IJavaSearchScope scope) {
		Set<IJavaProject> projects = new HashSet<>();
		
		// Get all paths enclosing the scope (projects and JARs)
		IPath[] enclosingPaths = scope.enclosingProjectsAndJars();
		
		for (IPath path : enclosingPaths) {
			// Find the resource for this path
			IResource resource = org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().findMember(path);
			
			// Check if it's a project
			if (resource != null && resource.getType() == IResource.PROJECT) {
				IProject project = (IProject) resource;
				
				// Check if it's a Java project
				if (project.isOpen()) {
					try {
						if (project.hasNature(JavaCore.NATURE_ID)) {
							IJavaProject javaProject = JavaCore.create(project);
							projects.add(javaProject);
						}
					} catch (CoreException e) {
						// Ignore projects we can't access
					}
				}
			}
		}
		
		return projects;
	}

	/**
	 * Collects all .feature files from a project, excluding derived resources.
	 */
	private Set<IFile> collectFeatureFiles(IProject project) {
		try {
			return ResourceHelper.getFeatureFilesInProject(project);
		} catch (CoreException e) {
			ILog.get().error("Error collecting feature files from project: " + project.getName(), e);
			return Collections.emptySet();
		}
	}

	@Override
	public int estimateTicks(QuerySpecification specification) {
		// Estimate work based on scope - feature file processing can be expensive
		// Return a reasonable estimate relative to 1000 ticks for Java search
		return 200;
	}

	@Override
	public IMatchPresentation getUIParticipant() {
		// Return null since we report matches against IResource (feature files)
		// which the Java search UI already knows how to display
		return null;
	}
}
