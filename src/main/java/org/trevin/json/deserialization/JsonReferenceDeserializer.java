package org.trevin.json.deserialization;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

import org.trevin.json.model.proxy.ObjectProxy;
import org.trevin.json.model.proxy.ProxyFacade;
import org.trevin.json.model.reference.ClassKeyReference;
import org.trevin.json.model.reference.helper.ReferenceHelper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;

import net.sf.cglib.proxy.Factory;

public class JsonReferenceDeserializer extends BeanDeserializer {
    private static final long serialVersionUID = 4396351173607933971L;
    private final Map<Integer, Factory> proxyEntityFactoryPool;
    private final Map<String, Collection<ClassKeyReference>> classKeyReferenceMap;
    private ProxyFacade proxyFacade;
    private boolean isReference;

    public JsonReferenceDeserializer(BeanDeserializer defaultDeserializer,
	    Map<Integer, Factory> proxyJSONModelEntityFactoryPool,
	    Map<String, Collection<ClassKeyReference>> classKeyReferenceMap, ProxyFacade proxyFacade) {
	super(defaultDeserializer, defaultDeserializer.getObjectIdReader());
	this.proxyEntityFactoryPool = proxyJSONModelEntityFactoryPool;
	this.classKeyReferenceMap = classKeyReferenceMap;
	this.proxyFacade = proxyFacade;
    }

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
	Object entity = referenceDeserialize(jsonParser, deserializationContext);

	int keyHashCode = getKeyHashCode(entity);
	Factory factory;

	if (proxyEntityFactoryPool.containsKey(keyHashCode)) {
	    factory = proxyEntityFactoryPool.get(keyHashCode);
	    ObjectProxy objectProxy = ((ObjectProxy) factory.getCallback(0));
	    if (objectProxy.getCoreObject() != null && !isReference)
		objectProxy.setCoreObject(entity);

	} else {
	    factory = proxyFacade.getProxyFactory(entity);
	    proxyEntityFactoryPool.put(keyHashCode, factory);
	}
	isReference = false;
	return factory;
    }

    private int getKeyHashCode(Object entity) throws IOException {

	if (!classKeyReferenceMap.containsKey(entity.getClass().getName()))
	    ReferenceHelper.addClassKeyReferences(entity.getClass(), classKeyReferenceMap);

	int code = entity.getClass().getName().hashCode();

	for (ClassKeyReference classKeyReference : classKeyReferenceMap.get(entity.getClass().getName())) {
	    try {
		Object keyValue = classKeyReference.getGetterMethod().invoke(entity);
		if (keyValue == null)
		    continue;
		code += getKeyHashCode(keyValue);

	    } catch (IllegalAccessException e) {
		throw new IOException(e);
	    } catch (IllegalArgumentException e) {
		throw new IOException(e);
	    } catch (InvocationTargetException e) {
		throw new IOException(e);
	    }
	}

	return code;
    }
    // Overwriting FasterXML Jackson implementations
    // ------------------------------------------------------------
    /*
     * /********************************************************** /*
     * JsonDeserializer implementation
     * /**********************************************************
     */

    public Object referenceDeserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
	// common case first
	if (p.isExpectedStartObjectToken()) {
	    if (_vanillaProcessing) {
		return vanillaDeserialize(p, ctxt, p.nextToken());
	    }
	    p.nextToken();
	    if (_objectIdReader != null) {
		return deserializeWithObjectId(p, ctxt);
	    }
	    return deserializeFromObject(p, ctxt);
	}
	JsonToken t = p.getCurrentToken();
	return referenceDeserializeOther(p, ctxt, t);
    }

    private Object referenceDeserializeOther(JsonParser p, DeserializationContext ctxt, JsonToken t)
	    throws IOException {
	// and then others, generally requiring use of @JsonCreator
	switch (t) {
	case VALUE_STRING:
	    return deserializeFromString(p, ctxt);
	case VALUE_NUMBER_INT:
	    return deserializeFromNumber(p, ctxt);
	case VALUE_NUMBER_FLOAT:
	    return deserializeFromDouble(p, ctxt);
	case VALUE_EMBEDDED_OBJECT:
	    return deserializeFromEmbedded(p, ctxt);
	case VALUE_TRUE:
	case VALUE_FALSE:
	    return deserializeFromBoolean(p, ctxt);
	case START_ARRAY:
	    // these only work if there's a (delegating) creator...
	    return deserializeFromArray(p, ctxt);
	case FIELD_NAME:
	case END_OBJECT: // added to resolve [JACKSON-319], possible related
			 // issues
	    if (_vanillaProcessing) {
		return vanillaDeserialize(p, ctxt, t);
	    }
	    if (_objectIdReader != null) {
		return deserializeWithObjectId(p, ctxt);
	    }
	    return deserializeFromObject(p, ctxt);
	default:
	    throw ctxt.mappingException(handledType());
	}
    }

    /*
     * /********************************************************** /* Concrete
     * deserialization methods
     * /**********************************************************
     */

    /**
     * Streamlined version that is only used when no "special" features are
     * enabled.
     */
    private final Object vanillaDeserialize(JsonParser p, DeserializationContext ctxt, JsonToken t) throws IOException {
	final Object bean = _valueInstantiator.createUsingDefault(ctxt);
	// [databind#631]: Assign current value, to be accessible by custom
	// serializers
	p.setCurrentValue(bean);
	if (p.hasTokenId(JsonTokenId.ID_FIELD_NAME)) {
	    String propName = p.getCurrentName();
	    do {
		p.nextToken();
		SettableBeanProperty prop = _beanProperties.find(propName);
		if (propName.equals(ReferenceHelper.IS_REFERENCE_FIELD_NAME)) {
		    isReference = true;
		    continue;
		}
		if (prop != null) { // normal case
		    try {
			prop.deserializeAndSet(p, ctxt, bean);
		    } catch (Exception e) {
			wrapAndThrow(e, bean, propName, ctxt);
		    }
		    continue;
		}

		handleUnknownVanilla(p, ctxt, bean, propName);
	    } while ((propName = p.nextFieldName()) != null);
	}
	return bean;
    }

}