package org.trevin.json.serialization.custom;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.trevin.json.deserialization.custom.helper.ReferenceHelper;
import org.trevin.json.deserialization.custom.key.ClassKeyReference;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

public class JsonCustomReferenceSerializer extends JsonSerializer<Object> {
    private static final Map<String, Collection<ClassKeyReference>> classKeyReferenceMap = new HashMap<String, Collection<ClassKeyReference>>();
    private final ReferenceHelper referenceHelper = new ReferenceHelper();

    @Override
    public void serialize(Object entity, JsonGenerator jsonGenerator, SerializerProvider provider)
	    throws IOException, JsonProcessingException {
	if (entity instanceof Collection) {
	    jsonGenerator.writeStartArray();
	    jsonGenerator.writeEndArray();
	} else {
	    jsonGenerator.writeStartObject();
	    serializeEntity(entity, jsonGenerator, provider);
	    jsonGenerator.writeEndObject();
	}
    }

    private void serializeEntity(Object entity, JsonGenerator jsonGenerator, SerializerProvider provider)
	    throws IOException {
	serializeEntity(entity, "", jsonGenerator, provider);
    }

    private void serializeEntity(Object entity, String prefix, JsonGenerator jsonGenerator, SerializerProvider provider)
	    throws IOException {
	if (isEmpty(provider, entity))
	    return;

	if (!classKeyReferenceMap.containsKey(entity.getClass().getName()))
	    referenceHelper.addClassKeyReferences(entity.getClass(), classKeyReferenceMap);
	if (classKeyReferenceMap.get(entity.getClass().getName()).isEmpty()) {
	    jsonGenerator.writeObjectField(prefix, entity);
	    return;
	}

	for (ClassKeyReference classKeyReference : classKeyReferenceMap.get(entity.getClass().getName())) {
	    try {
		Object keyObject = classKeyReference.getGetterMethod().invoke(entity);

		if (keyObject != null) {
		    serializeEntity(keyObject, getFieldName(prefix, classKeyReference.getFieldName()), jsonGenerator,
			    provider);
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

    private String getFieldName(String prefix, String fieldName) {
	return prefix.isEmpty() ? fieldName : prefix + "_" + fieldName;
    }

    @Override
    public void serializeWithType(Object entity, JsonGenerator jsonGenerator, SerializerProvider provider,
	    TypeSerializer typeSerializer) throws IOException, JsonProcessingException {
	jsonGenerator.writeStartObject();
	jsonGenerator.writeNumberField(ReferenceHelper.IS_REFERENCE_FIELD_NAME, 1);
	jsonGenerator.writeStringField(typeSerializer.getPropertyName(),
		typeSerializer.getTypeIdResolver().idFromValue(entity));
	serializeEntity(entity, jsonGenerator, provider);
	jsonGenerator.writeEndObject();
    }
}