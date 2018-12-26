package cucumber.eclipse.editor.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static cucumber.eclipse.editor.steps.StepDefinitionsRepository.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.junit.Before;
import org.junit.Test;

import cucumber.eclipse.editor.tests.MockFile;
import cucumber.eclipse.steps.integration.ResourceHelper;
import cucumber.eclipse.steps.integration.StepDefinition;

public class StepDefinitionsRepositoryTest {

	private StepDefinitionsRepository stepDefinitionsRepository;
	
	@Before
	public void setUp() {
		stepDefinitionsRepository = new StepDefinitionsRepository();
		
		StepDefinition stepDefinition1 = new StepDefinition();
		stepDefinition1.setText("I buy {word}");
		stepDefinition1.setLineNumber(21);
		
		StepDefinition stepDefinition2 = new StepDefinition();
		stepDefinition2.setText("I pay {int}");
		stepDefinition2.setLineNumber(12);
		
		StepDefinition stepDefinition3 = new StepDefinition();
		stepDefinition3.setText("I add {int} and {int}");
		stepDefinition3.setLineNumber(21);
		
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
		String serialization = serialize(stepDefinitionsRepository);
		assertThat(serialization, is(notNullValue()));
		
		StepDefinitionsRepository deserializedRepository = deserialize(serialization, new TestResourceHelper());
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
