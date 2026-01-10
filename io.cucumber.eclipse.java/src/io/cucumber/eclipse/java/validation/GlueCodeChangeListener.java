package io.cucumber.eclipse.java.validation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.service.debug.DebugTrace;

import io.cucumber.eclipse.editor.Tracing;
import io.cucumber.eclipse.java.JDTUtil;

/**
 * Listens for Java model changes and triggers revalidation of affected Cucumber feature files.
 * <p>
 * This listener monitors changes to Java compilation units and identifies when changes might
 * affect Cucumber step definitions. It uses a two-phase approach:
 * </p>
 * <ol>
 * <li><b>POST_RECONCILE phase</b> (during editing): Tracks structural changes to compilation units
 * that contain Cucumber annotations. These events occur while editing but before saving.</li>
 * <li><b>POST_CHANGE phase</b> (after save): When a file is saved (F_PRIMARY_RESOURCE), checks
 * if any relevant changes were recorded during reconciliation and triggers revalidation.</li>
 * </ol>
 * <p>
 * This approach avoids revalidating on every keystroke while ensuring that when a file is saved,
 * we only revalidate if something relevant actually changed.
 * </p>
 * <p>
 * The listener is designed to be package-protected and used exclusively by {@link CucumberGlueValidator}
 * to maintain separation of concerns and keep the validator class focused on validation logic.
 * </p>
 * 
 * @see CucumberGlueValidator
 * @see IElementChangedListener
 */
class GlueCodeChangeListener implements IElementChangedListener {

	/**
	 * Tracks compilation units that have had relevant structural changes during reconciliation.
	 * Key: ICompilationUnit handle identifier
	 * Value: IProject that contains the compilation unit
	 */
	private final Map<String, IProject> pendingChanges = new HashMap<>();

	@Override
	public void elementChanged(ElementChangedEvent event) {
		int eventType = event.getType();
		
		if (eventType == ElementChangedEvent.POST_RECONCILE) {
			// During editing: track relevant changes for later
			if (Tracing.DEBUG_VALIDATION_GLUE_ENABLED) {
				DebugTrace trace = Tracing.get();
				trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "POST_RECONCILE: Tracking structural changes");
			}
			trackStructuralChanges(event.getDelta());
			if (Tracing.DEBUG_VALIDATION_GLUE_ENABLED) {
				DebugTrace trace = Tracing.get();
				trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "Currently tracking " + pendingChanges.size() + " files with changes");
			}
			
		} else if (eventType == ElementChangedEvent.POST_CHANGE) {
			// After save/build: act on previously tracked changes
			if (Tracing.DEBUG_VALIDATION_GLUE_ENABLED) {
				DebugTrace trace = Tracing.get();
				trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "POST_CHANGE: Processing saved changes");
			}
			Set<IProject> affectedProjects = processSavedChanges(event.getDelta());
			
			if (Tracing.DEBUG_VALIDATION_GLUE_ENABLED) {
				DebugTrace trace = Tracing.get();
				trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "Found " + affectedProjects.size() + " affected projects");
				for (IProject project : affectedProjects) {
					trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "  - Project: " + project.getName());
				}
			}
			
			for (IProject project : affectedProjects) {
				try {
					CucumberGlueValidator.revalidateProject(project);
				} catch (CoreException e) {
					if (Tracing.DEBUG_VALIDATION_GLUE_ENABLED) {
						DebugTrace trace = Tracing.get();
						trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "Error revalidating project " + project.getName(), e);
					}
				}
			}
		} else {
			if (Tracing.DEBUG_VALIDATION_GLUE_ENABLED) {
				DebugTrace trace = Tracing.get();
				trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "Ignoring event type: " + getEventTypeName(eventType));
			}
		}
	}

	/**
	 * Tracks structural changes during reconciliation (POST_RECONCILE events).
	 * These changes haven't been saved yet, but we record them so we can act when the file is saved.
	 * 
	 * @param delta the delta to process
	 */
	private void trackStructuralChanges(IJavaElementDelta delta) {
		IJavaElement element = delta.getElement();
		int kind = delta.getKind();

		if (Tracing.DEBUG_VALIDATION_GLUE_ENABLED) {
			DebugTrace trace = Tracing.get();
			trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "  Reconcile delta: " + element.getElementName() 
					+ " (type=" + getElementTypeName(element.getElementType()) 
					+ ", kind=" + getKindName(kind) 
					+ ", flags=" + getFlagsString(delta.getFlags()) + ")");
		}

		// Recurse into container elements
		switch (element.getElementType()) {
		case IJavaElement.JAVA_MODEL:
		case IJavaElement.JAVA_PROJECT:
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
		case IJavaElement.PACKAGE_FRAGMENT:
			for (IJavaElementDelta childDelta : delta.getAffectedChildren()) {
				trackStructuralChanges(childDelta);
			}
			break;

		case IJavaElement.COMPILATION_UNIT:
			// Check if this is a relevant structural change
			if (isStructuralChange(delta)) {
				ICompilationUnit cu = (ICompilationUnit) element;
				try {
					// Only track if it contains Cucumber glue annotations
					if (JDTUtil.hasCucumberGlueAnnotation(cu, null)) {
						IProject project = cu.getJavaProject().getProject();
						if (project != null && project.isAccessible()) {
							String key = cu.getHandleIdentifier();
							pendingChanges.put(key, project);
							if (Tracing.DEBUG_VALIDATION_GLUE_ENABLED) {
								DebugTrace trace = Tracing.get();
								trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "    -> Tracked structural change in: " + cu.getElementName());
							}
						}
					} else if (Tracing.DEBUG_VALIDATION_GLUE_ENABLED) {
						DebugTrace trace = Tracing.get();
						trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "    -> Structural change but no Cucumber annotations");
					}
				} catch (JavaModelException e) {
					if (Tracing.DEBUG_VALIDATION_GLUE_ENABLED) {
						DebugTrace trace = Tracing.get();
						trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "    -> Error checking annotations", e);
					}
				}
			} else if (Tracing.DEBUG_VALIDATION_GLUE_ENABLED) {
				DebugTrace trace = Tracing.get();
				trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "    -> Not a structural change (ignored)");
			}
			break;

		default:
			break;
		}
	}

	/**
	 * Processes saved changes (POST_CHANGE events with F_PRIMARY_RESOURCE).
	 * Checks if any saved files had relevant structural changes that were tracked during reconciliation.
	 * 
	 * @param delta the delta to process
	 * @return set of affected projects that need revalidation
	 */
	private Set<IProject> processSavedChanges(IJavaElementDelta delta) {
		Set<IProject> affectedProjects = new HashSet<>();
		collectSavedFiles(delta, affectedProjects);
		return affectedProjects;
	}

	/**
	 * Collects compilation units that were saved and had previously tracked changes.
	 * 
	 * @param delta the delta to process
	 * @param affectedProjects set to collect affected projects
	 */
	private void collectSavedFiles(IJavaElementDelta delta, Set<IProject> affectedProjects) {
		IJavaElement element = delta.getElement();
		int kind = delta.getKind();
		int flags = delta.getFlags();

		if (Tracing.DEBUG_VALIDATION_GLUE_ENABLED) {
			DebugTrace trace = Tracing.get();
			trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "  Save delta: " + element.getElementName() 
					+ " (type=" + getElementTypeName(element.getElementType()) 
					+ ", kind=" + getKindName(kind) 
					+ ", flags=" + getFlagsString(flags) + ")");
		}

		// Recurse into container elements
		switch (element.getElementType()) {
		case IJavaElement.JAVA_MODEL:
		case IJavaElement.JAVA_PROJECT:
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
		case IJavaElement.PACKAGE_FRAGMENT:
			for (IJavaElementDelta childDelta : delta.getAffectedChildren()) {
				collectSavedFiles(childDelta, affectedProjects);
			}
			break;

		case IJavaElement.COMPILATION_UNIT:
			// Check if this file was saved (F_PRIMARY_RESOURCE)
			if ((flags & IJavaElementDelta.F_PRIMARY_RESOURCE) != 0) {
				ICompilationUnit cu = (ICompilationUnit) element;
				String key = cu.getHandleIdentifier();
				
				if (Tracing.DEBUG_VALIDATION_GLUE_ENABLED) {
					DebugTrace trace = Tracing.get();
					trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "    -> File saved: " + cu.getElementName());
				}
				
				// Check if we tracked changes for this file during reconciliation
				IProject project = pendingChanges.remove(key);
				if (project != null) {
					if (Tracing.DEBUG_VALIDATION_GLUE_ENABLED) {
						DebugTrace trace = Tracing.get();
						trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "    -> Has tracked changes, adding project: " + project.getName());
					}
					affectedProjects.add(project);
				} else if (Tracing.DEBUG_VALIDATION_GLUE_ENABLED) {
					DebugTrace trace = Tracing.get();
					trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "    -> No tracked changes for this file");
				}
			}
			break;

		default:
			break;
		}
	}

	/**
	 * Determines if a delta represents a structural change during reconciliation.
	 * Only called for POST_RECONCILE events to track changes before they're saved.
	 * 
	 * @param delta the compilation unit delta
	 * @return {@code true} if this is a structural change worth tracking
	 */
	private boolean isStructuralChange(IJavaElementDelta delta) {
		int kind = delta.getKind();
		int flags = delta.getFlags();

		if (Tracing.DEBUG_VALIDATION_GLUE_ENABLED) {
			DebugTrace trace = Tracing.get();
			trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "      Checking if structural change...");
		}

		// For reconciliation, we only care about CHANGED with structural flags
		if (kind == IJavaElementDelta.CHANGED) {
			// Children changed (types, methods added/removed/modified)
			if ((flags & IJavaElementDelta.F_CHILDREN) != 0) {
				// Children flag is set, but we need to check if it's truly structural
				// Ignore if it's ONLY fine-grained changes
				if ((flags & IJavaElementDelta.F_FINE_GRAINED) != 0) {
					// It's fine-grained, but if children changed it might still be structural
					// Check if there are actual structural child changes
					boolean hasStructuralChildren = hasStructuralChildChanges(delta);
					if (Tracing.DEBUG_VALIDATION_GLUE_ENABLED) {
						DebugTrace trace = Tracing.get();
						if (hasStructuralChildren) {
							trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "      -> Structural: CHILDREN with structural modifications");
						} else {
							trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "      -> Not structural: CHILDREN but only fine-grained");
						}
					}
					return hasStructuralChildren;
				}
				if (Tracing.DEBUG_VALIDATION_GLUE_ENABLED) {
					DebugTrace trace = Tracing.get();
					trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "      -> Structural: CHILDREN changed");
				}
				return true;
			}

			// Modifiers changed (public/private, static, etc.)
			if ((flags & IJavaElementDelta.F_MODIFIERS) != 0) {
				if (Tracing.DEBUG_VALIDATION_GLUE_ENABLED) {
					DebugTrace trace = Tracing.get();
					trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "      -> Structural: MODIFIERS changed");
				}
				return true;
			}

			// Super types changed
			if ((flags & IJavaElementDelta.F_SUPER_TYPES) != 0) {
				if (Tracing.DEBUG_VALIDATION_GLUE_ENABLED) {
					DebugTrace trace = Tracing.get();
					trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "      -> Structural: SUPER_TYPES changed");
				}
				return true;
			}

			// Content changed without fine-grained flag = structural
			if ((flags & IJavaElementDelta.F_CONTENT) != 0 && (flags & IJavaElementDelta.F_FINE_GRAINED) == 0) {
				if (Tracing.DEBUG_VALIDATION_GLUE_ENABLED) {
					DebugTrace trace = Tracing.get();
					trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "      -> Structural: CONTENT changed (not fine-grained)");
				}
				return true;
			}
		}

		if (Tracing.DEBUG_VALIDATION_GLUE_ENABLED) {
			DebugTrace trace = Tracing.get();
			trace.trace(Tracing.DEBUG_VALIDATION_GLUE, "      -> Not structural");
		}
		return false;
	}

	/**
	 * Checks if a delta has structural changes in its children.
	 * This is used to determine if a FINE_GRAINED change is actually structural.
	 * 
	 * @param delta the parent delta
	 * @return true if any child has structural changes
	 */
	private boolean hasStructuralChildChanges(IJavaElementDelta delta) {
		for (IJavaElementDelta child : delta.getAffectedChildren()) {
			int childKind = child.getKind();
			int childFlags = child.getFlags();
			
			// ADDED or REMOVED children are always structural
			if (childKind == IJavaElementDelta.ADDED || childKind == IJavaElementDelta.REMOVED) {
				return true;
			}
			
			// Check for annotation changes (relevant for step definitions)
			if ((childFlags & IJavaElementDelta.F_ANNOTATIONS) != 0) {
				return true;
			}
			
			// Check for modifiers, signature changes
			if ((childFlags & IJavaElementDelta.F_MODIFIERS) != 0) {
				return true;
			}
			
			// Recurse into nested children
			if (hasStructuralChildChanges(child)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a human-readable name for the event type.
	 */
	private String getEventTypeName(int eventType) {
		switch (eventType) {
		case ElementChangedEvent.POST_CHANGE:
			return "POST_CHANGE";
		case ElementChangedEvent.POST_RECONCILE:
			return "POST_RECONCILE";
		default:
			return "UNKNOWN(" + eventType + ")";
		}
	}

	/**
	 * Returns a human-readable name for the element type.
	 */
	private String getElementTypeName(int elementType) {
		switch (elementType) {
		case IJavaElement.JAVA_MODEL:
			return "JAVA_MODEL";
		case IJavaElement.JAVA_PROJECT:
			return "JAVA_PROJECT";
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			return "PACKAGE_FRAGMENT_ROOT";
		case IJavaElement.PACKAGE_FRAGMENT:
			return "PACKAGE_FRAGMENT";
		case IJavaElement.COMPILATION_UNIT:
			return "COMPILATION_UNIT";
		case IJavaElement.TYPE:
			return "TYPE";
		case IJavaElement.METHOD:
			return "METHOD";
		case IJavaElement.FIELD:
			return "FIELD";
		default:
			return "UNKNOWN(" + elementType + ")";
		}
	}

	/**
	 * Returns a human-readable name for the delta kind.
	 */
	private String getKindName(int kind) {
		switch (kind) {
		case IJavaElementDelta.ADDED:
			return "ADDED";
		case IJavaElementDelta.REMOVED:
			return "REMOVED";
		case IJavaElementDelta.CHANGED:
			return "CHANGED";
		default:
			return "UNKNOWN(" + kind + ")";
		}
	}

	/**
	 * Returns a human-readable string for the delta flags.
	 */
	private String getFlagsString(int flags) {
		if (flags == 0) {
			return "NONE";
		}
		StringBuilder sb = new StringBuilder();
		if ((flags & IJavaElementDelta.F_CONTENT) != 0) sb.append("CONTENT ");
		if ((flags & IJavaElementDelta.F_MODIFIERS) != 0) sb.append("MODIFIERS ");
		if ((flags & IJavaElementDelta.F_CHILDREN) != 0) sb.append("CHILDREN ");
		if ((flags & IJavaElementDelta.F_MOVED_FROM) != 0) sb.append("MOVED_FROM ");
		if ((flags & IJavaElementDelta.F_MOVED_TO) != 0) sb.append("MOVED_TO ");
		if ((flags & IJavaElementDelta.F_ADDED_TO_CLASSPATH) != 0) sb.append("ADDED_TO_CLASSPATH ");
		if ((flags & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) != 0) sb.append("REMOVED_FROM_CLASSPATH ");
		if ((flags & IJavaElementDelta.F_REORDER) != 0) sb.append("REORDER ");
		if ((flags & IJavaElementDelta.F_OPENED) != 0) sb.append("OPENED ");
		if ((flags & IJavaElementDelta.F_CLOSED) != 0) sb.append("CLOSED ");
		if ((flags & IJavaElementDelta.F_SUPER_TYPES) != 0) sb.append("SUPER_TYPES ");
		if ((flags & IJavaElementDelta.F_SOURCEATTACHED) != 0) sb.append("SOURCEATTACHED ");
		if ((flags & IJavaElementDelta.F_SOURCEDETACHED) != 0) sb.append("SOURCEDETACHED ");
		if ((flags & IJavaElementDelta.F_FINE_GRAINED) != 0) sb.append("FINE_GRAINED ");
		if ((flags & IJavaElementDelta.F_ARCHIVE_CONTENT_CHANGED) != 0) sb.append("ARCHIVE_CONTENT_CHANGED ");
		if ((flags & IJavaElementDelta.F_PRIMARY_WORKING_COPY) != 0) sb.append("PRIMARY_WORKING_COPY ");
		if ((flags & IJavaElementDelta.F_CLASSPATH_CHANGED) != 0) sb.append("CLASSPATH_CHANGED ");
		if ((flags & IJavaElementDelta.F_PRIMARY_RESOURCE) != 0) sb.append("PRIMARY_RESOURCE ");
		if ((flags & IJavaElementDelta.F_AST_AFFECTED) != 0) sb.append("AST_AFFECTED ");
		if ((flags & IJavaElementDelta.F_CATEGORIES) != 0) sb.append("CATEGORIES ");
		if ((flags & IJavaElementDelta.F_ANNOTATIONS) != 0) sb.append("ANNOTATIONS ");
		if ((flags & IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED) != 0) sb.append("RESOLVED_CLASSPATH_CHANGED ");
		return sb.toString().trim();
	}

}
