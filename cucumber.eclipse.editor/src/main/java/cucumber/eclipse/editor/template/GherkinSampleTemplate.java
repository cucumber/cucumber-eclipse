package cucumber.eclipse.editor.template;


import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import cucumber.eclipse.editor.Activator;

/**
 * @author girija.panda@nokia.com
 * Purpose: This Class will override any blank Feature File with some Sample Template
 * Steps:
 * 1. Read the  sample Feature:'FeatureTemplate.txt' exists in 'sample' directory
 * 2. Convert the file into String
 * 
 */
public class GherkinSampleTemplate {

	/*
	 * private static final String SAMPLE_FEATURE_CONTENT = "# Commented\n"
	 * +"# Sample Feature Definition File\n" +"Feature: Feature Name\n" + "\n" +
	 * "  Scenario: Scenario 1\n" + "    When some action\n" +
	 * "    Then some validation\n" + "    And some more validation\n" ;
	 */
	private static final String CHARACTERSET = "UTF-8";
	private static String sampleFilePath = "sample/FeatureTemplate.txt";
	private static String sampleFileContent = null;

	public GherkinSampleTemplate() {

	}

	// 2. To get Sample Feature File
	public static String getFeatureTemplate() {

		try {
			sampleFileContent = fileToString(sampleFilePath);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}

		return sampleFileContent;
	}

	
	// 1. Read the file and convert to String
	public static String fileToString(String filePath)
			throws FileNotFoundException {

		InputStream inputStream = null;
		byte[] byteData = null;
		
		//BufferedInputStream buffIStream = null; //Commented for DataInputStream
		// Used DataInputStream for more efficient
		DataInputStream dataIStream = null; 

		try{
				inputStream = FileLocator.openStream(Activator.getDefault().getBundle(), 
													new Path(filePath), 
													false);
				if (inputStream != null) 
				{
					byteData = new byte[inputStream.available()];
					// buffIStream = new BufferedInputStream(inputStream);
					// buffIStream.read(data);
					dataIStream = new DataInputStream(inputStream);
					dataIStream.readFully(byteData);
				}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
					if (dataIStream != null) {
						try 
						{
							dataIStream.close();
						} catch (IOException ignored) {
							ignored.getMessage();
						}
					}
			}

		return new String(byteData, Charset.forName(CHARACTERSET));
	}

}
