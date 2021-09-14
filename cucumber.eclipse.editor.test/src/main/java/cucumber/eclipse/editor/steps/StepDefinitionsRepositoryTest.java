package cucumber.eclipse.editor.steps;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.junit.Before;
import org.junit.Test;

import cucumber.eclipse.editor.tests.MockFile;
import io.cucumber.eclipse.editor.ResourceHelper;
import io.cucumber.eclipse.editor.StorageHelper;
import io.cucumber.eclipse.editor.steps.ExpressionDefinition;
import io.cucumber.eclipse.editor.steps.StepDefinition;
import io.cucumber.eclipse.editor.steps.StepDefinitionsRepository;

public class StepDefinitionsRepositoryTest {

	private StepDefinitionsRepository stepDefinitionsRepository;
	
	@Before
	public void setUp() {
		stepDefinitionsRepository = new StepDefinitionsRepository();
		
		StepDefinition stepDefinition1 = createStep("I buy {word}",21);
		
		StepDefinition stepDefinition2 = createStep("I pay {int}",12);
		
		StepDefinition stepDefinition3 = createStep("I add {int} and {int}", 21);
		
		Set<StepDefinition> steps = new HashSet<StepDefinition>();
		steps.add(stepDefinition1);
		steps.add(stepDefinition2);

		IFile stepDefinitionsFile = new MockFile("file1");
		stepDefinitionsRepository.add(stepDefinitionsFile, steps);

		Set<StepDefinition> steps2 = new HashSet<StepDefinition>();
		steps2.add(stepDefinition3);
		
		IFile stepDefinitionsFile2 = new MockFile("file2");
		stepDefinitionsRepository.add(stepDefinitionsFile2, steps2);

	}
	
	private StepDefinition createStep(String text, int lineNo) {
		return new StepDefinition(UUID.randomUUID().toString(), StepDefinition.NO_LABEL, new ExpressionDefinition(text, "en"), StepDefinition.NO_SOURCE, StepDefinition.NO_LINE_NUMBER, StepDefinition.NO_SOURCE_NAME, StepDefinition.NO_PACKAGE_NAME);
	}

	@Test
	public void store() {
		Set<StepDefinition> stepDefinitions = stepDefinitionsRepository.getAllStepDefinitions();
		assertThat(stepDefinitions.size(), equalTo(3));
		
		Set<IFile> stepDefinitionsFiles = stepDefinitionsRepository.getAllStepDefinitionsFiles();
		assertThat(stepDefinitionsFiles.size(), equalTo(2));
	}

	@Test
	public void reset() {
		store();
		
		stepDefinitionsRepository.reset();
		
		Set<StepDefinition> stepDefinitions = stepDefinitionsRepository.getAllStepDefinitions();
		assertThat(stepDefinitions.size(), equalTo(0));
		
		Set<IFile> stepDefinitionsFiles = stepDefinitionsRepository.getAllStepDefinitionsFiles();
		assertThat(stepDefinitionsFiles.size(), equalTo(0));
		
	}
	
	@Test
	public void identityStepDefinitionsFile() {
		
		boolean isstepDefinitionsFile = stepDefinitionsRepository.isStepDefinitionsResource(new MockFile("file2"));
		assertThat(isstepDefinitionsFile, is(true));
		
		isstepDefinitionsFile = stepDefinitionsRepository.isStepDefinitionsResource(new MockFile("file3"));
		assertThat(isstepDefinitionsFile, is(false));
		
	}

	@Test
	public void serialization() throws IOException, ClassNotFoundException {
		InputStream stream = StorageHelper.toStream(stepDefinitionsRepository, null);
		assertNotNull(stream);
		StorageHelper.RESOURCEHELPER = new TestResourceHelper();
		StepDefinitionsRepository deserializedRepository = StorageHelper.fromStream(StepDefinitionsRepository.class, stream, null);
		Set<StepDefinition> stepDefinitions = deserializedRepository.getAllStepDefinitions();
		assertThat(stepDefinitions.size(), equalTo(3));
		
		Set<IFile> stepDefinitionsFiles = deserializedRepository.getAllStepDefinitionsFiles();
		assertThat(stepDefinitionsFiles.size(), equalTo(2));
	}
	
	class TestResourceHelper extends ResourceHelper {
		@Override
		public IResource find(String path) {
			return new MockFile(path);
		}
	}
	
}
