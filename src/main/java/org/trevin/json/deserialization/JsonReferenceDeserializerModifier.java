package org.trevin.json.deserialization;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.trevin.json.model.proxy.ProxyFacade;
import org.trevin.json.model.reference.ClassKeyReference;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;

import net.sf.cglib.proxy.Factory;

public class JsonReferenceDeserializerModifier extends BeanDeserializerModifier {
    private final Map<Integer, Factory> entityFactoryPool = new HashMap<Integer, Factory>();
    private final Map<String, Collection<ClassKeyReference>> classKeyReferenceMap = new HashMap<String, Collection<ClassKeyReference>>();

    private ProxyFacade proxyFacade;

    public JsonReferenceDeserializerModifier(ProxyFacade proxyFacade) {
	super();
	this.proxyFacade = proxyFacade;
    }

    @Override
    public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
	    final JsonDeserializer<?> deserializer) {
	if (deserializer instanceof BeanDeserializer) {
	    return new JsonReferenceDeserializer((BeanDeserializer) deserializer, entityFactoryPool,
		    classKeyReferenceMap, proxyFacade);
	}
	return deserializer;
    }

}