/**
 * Provides quick fixes (marker resolutions) for various Cucumber editor markers.
 * 
 * <p>This package contains implementations of {@link org.eclipse.ui.IMarkerResolutionGenerator}
 * that offer quick fixes for issues detected in Gherkin feature files.</p>
 * 
 * <h2>Language Support Detection</h2>
 * <p>The {@link io.cucumber.eclipse.editor.quickfix.LanguageSupportMarkerResolutionGenerator}
 * provides quick fixes when enhanced language support is available but not installed.
 * This is detected by checking project natures:
 * <ul>
 *   <li>Java projects (org.eclipse.jdt.core.javanature) - suggests io.cucumber.eclipse.java</li>
 *   <li>Python projects (org.python.pydev.pythonNature) - suggests io.cucumber.eclipse.python</li>
 * </ul>
 * </p>
 * 
 * @since 3.0.0
 */
package io.cucumber.eclipse.editor.quickfix;
