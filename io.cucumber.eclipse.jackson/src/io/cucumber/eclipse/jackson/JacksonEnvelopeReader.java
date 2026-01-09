package io.cucumber.eclipse.jackson;

import java.io.IOException;

import org.osgi.service.component.annotations.Component;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.ConstructorDetector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import io.cucumber.eclipse.editor.EnvelopeReader;
import io.cucumber.messages.types.Envelope;

/**
 * Jackson-based implementation of {@link EnvelopeReader}. Provides
 * deserialization of Cucumber message envelopes from JSON. This is a workaround
 * for https://github.com/cucumber/cucumber-jvm/pull/3102 as we otherwise clash
 * with usages of jackson in Cucumber itself when running in the embedded mode
 */
@Component
public class JacksonEnvelopeReader implements EnvelopeReader {

	private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
			.addModule(new Jdk8Module())
			.addModule(new ParameterNamesModule(Mode.PROPERTIES))
			.defaultPropertyInclusion(JsonInclude.Value.construct(Include.NON_ABSENT, Include.NON_ABSENT))
			.constructorDetector(ConstructorDetector.USE_PROPERTIES_BASED)
			.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
			.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
			.enable(DeserializationFeature.USE_LONG_FOR_INTS)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
			.build();

	@Override
	public Envelope readEnvelope(byte[] buffer, int length) throws IOException {
		return OBJECT_MAPPER.readerFor(Envelope.class).readValue(buffer, 0, length);
	}
}
