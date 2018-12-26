package cucumber.eclipse.steps.integration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

public abstract class SerializationHelper {

	public static <T> String serialize(T object) throws IOException {
		ObjectOutputStream objectOutputStream = null;
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(object);
			objectOutputStream.close();
			byteArrayOutputStream.close();

			return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
		} finally {
			objectOutputStream.close();
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T deserialize(String serializedObject) throws IOException, ClassNotFoundException {
		ObjectInputStream objectInputStream = null;
		try {
			byte[] gherkinStepBytes = Base64.getDecoder().decode(serializedObject);
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(gherkinStepBytes);
			objectInputStream = new ObjectInputStream(byteArrayInputStream);
			return (T) objectInputStream.readObject();
		} finally {
			if (objectInputStream != null) {
				objectInputStream.close();
			}
		}
	}

}
