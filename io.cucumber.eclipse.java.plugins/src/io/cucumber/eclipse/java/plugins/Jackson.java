package io.cucumber.eclipse.java.plugins;

import java.io.IOException;
import java.io.Writer;

import io.cucumber.core.internal.com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.cucumber.core.internal.com.fasterxml.jackson.core.JsonGenerator;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.DeserializationFeature;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.SerializationFeature;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.cfg.ConstructorDetector;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.json.JsonMapper;
import io.cucumber.core.internal.com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.cucumber.messages.MessageToNdjsonWriter.Serializer;
import io.cucumber.messages.types.Envelope;

final class Jackson implements Serializer {
	// Copied from io.cucumber.core.plugin.Jackson as it is package protected and
	// there seems no way to access it!
	private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .addModule(new Jdk8Module())
            .serializationInclusion(Include.NON_ABSENT)
            .constructorDetector(ConstructorDetector.USE_PROPERTIES_BASED)
            .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
            .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
            .enable(DeserializationFeature.USE_LONG_FOR_INTS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
            .build();

	@Override
	public void writeValue(Writer writer, Envelope value) throws IOException {
		Jackson.OBJECT_MAPPER.writeValue(writer, value);
	}

}
