package org.trevin.json.deserialization.custom;

import org.trevin.json.deserialization.custom.cache.CustomReferenceDeserializerCache;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;

public class JsonCustomReferenceDeserializerModifier extends BeanDeserializerModifier {
    private static final CustomReferenceDeserializerCache CUSTOM_REFERENCE_DESERIALIZER_CACHE = new CustomReferenceDeserializerCache();

    @Override
    public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
	    final JsonDeserializer<?> deserializer) {
	if (deserializer instanceof BeanDeserializer) {
	    return new JsonCustomReferenceDeserializer((BeanDeserializer) deserializer, CUSTOM_REFERENCE_DESERIALIZER_CACHE);
	}
	return deserializer;
    }

}