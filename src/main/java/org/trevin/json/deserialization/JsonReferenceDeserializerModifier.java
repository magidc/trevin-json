package org.trevin.json.deserialization;

import org.trevin.json.deserialization.cache.ReferenceDeserializerCache;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;

public class JsonReferenceDeserializerModifier extends BeanDeserializerModifier {
    private static final ReferenceDeserializerCache referenceDeserializerCache = new ReferenceDeserializerCache();

    @Override
    public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
	    final JsonDeserializer<?> deserializer) {
	if (deserializer instanceof BeanDeserializer) {
	    return new JsonReferenceDeserializer((BeanDeserializer) deserializer, referenceDeserializerCache);
	}
	return deserializer;
    }

}