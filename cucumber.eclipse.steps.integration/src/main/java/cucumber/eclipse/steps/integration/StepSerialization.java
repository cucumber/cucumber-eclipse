package cucumber.eclipse.steps.integration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

import gherkin.formatter.model.Step;

public abstract class StepSerialization {

	public static String serialize(Step step) throws IOException {
		ObjectOutputStream objectOutputStream = null;
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(step);
			objectOutputStream.close();
			byteArrayOutputStream.close();
			
			return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
		} finally {
			objectOutputStream.close();
		}
	}
	
	public static Step deserialize(String gherkinStep) throws IOException, ClassNotFoundException {
		ObjectInputStream objectInputStream = null;
		try {
			byte[] gherkinStepBytes = Base64.getDecoder().decode(gherkinStep);
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(gherkinStepBytes);
			objectInputStream = new ObjectInputStream(byteArrayInputStream);
			return (Step) objectInputStream.readObject();
		}
		finally {
			if(objectInputStream != null) {
				objectInputStream.close();
			}
		}
		
	}
	
}
