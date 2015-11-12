package org.trevin.json.deserialization.custom;

import java.io.IOException;

import org.trevin.json.deserialization.custom.cache.CustomReferenceDeserializerCache;
import org.trevin.json.deserialization.custom.cache.EntityBean;
import org.trevin.json.deserialization.custom.helper.ReferenceHelper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;

public class JsonCustomReferenceDeserializer extends BeanDeserializer {
    private static final long serialVersionUID = 4396351173607933971L;
    private final CustomReferenceDeserializerCache customReferenceDeserializerCache;

    public JsonCustomReferenceDeserializer(BeanDeserializer defaultDeserializer,
	    CustomReferenceDeserializerCache customReferenceDeserializerCache) {
	super(defaultDeserializer, defaultDeserializer.getObjectIdReader());
	this.customReferenceDeserializerCache = customReferenceDeserializerCache;
    }

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
	return customReferenceDeserializerCache.checkInCache(referenceDeserialize(jsonParser, deserializationContext));
    }

    // Overwriting FasterXML Jackson implementations
    // ------------------------------------------------------------
    /*
     * /********************************************************** /*
     * JsonDeserializer implementation
     * /**********************************************************
     */

    private EntityBean referenceDeserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
	// common case first
	if (p.isExpectedStartObjectToken()) {
	    if (_vanillaProcessing) {
		return vanillaDeserialize(p, ctxt, p.nextToken());
	    }
	    p.nextToken();
	    if (_objectIdReader != null) {
		return new EntityBean(deserializeWithObjectId(p, ctxt));
	    }
	    return new EntityBean(deserializeFromObject(p, ctxt));
	}
	JsonToken t = p.getCurrentToken();
	return referenceDeserializeOther(p, ctxt, t);
    }

    private EntityBean referenceDeserializeOther(JsonParser p, DeserializationContext ctxt, JsonToken t)
	    throws IOException {
	// and then others, generally requiring use of @JsonCreator
	switch (t) {
	case VALUE_STRING:
	    return new EntityBean(deserializeFromString(p, ctxt));
	case VALUE_NUMBER_INT:
	    return new EntityBean(deserializeFromNumber(p, ctxt));
	case VALUE_NUMBER_FLOAT:
	    return new EntityBean(deserializeFromDouble(p, ctxt));
	case VALUE_EMBEDDED_OBJECT:
	    return new EntityBean(deserializeFromEmbedded(p, ctxt));
	case VALUE_TRUE:
	case VALUE_FALSE:
	    return new EntityBean(deserializeFromBoolean(p, ctxt));
	case START_ARRAY:
	    // these only work if there's a (delegating) creator...
	    return new EntityBean(deserializeFromArray(p, ctxt));
	case FIELD_NAME:
	case END_OBJECT: // added to resolve [JACKSON-319], possible related
			 // issues
	    if (_vanillaProcessing) {
		return vanillaDeserialize(p, ctxt, t);
	    }
	    if (_objectIdReader != null) {
		return new EntityBean(deserializeWithObjectId(p, ctxt));
	    }
	    return new EntityBean(deserializeFromObject(p, ctxt));
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
    private final EntityBean vanillaDeserialize(JsonParser p, DeserializationContext ctxt, JsonToken t)
	    throws IOException {
	final Object bean = _valueInstantiator.createUsingDefault(ctxt);
	// [databind#631]: Assign current value, to be accessible by custom
	// serializers
	p.setCurrentValue(bean);
	boolean isReference = false;
	customReferenceDeserializerCache.pushBean(bean);
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
			customReferenceDeserializerCache.pushSettableBeanProperty(prop);
			prop.deserializeAndSet(p, ctxt, bean);
			customReferenceDeserializerCache.popSettableBeanProperty();
		    } catch (Exception e) {
			wrapAndThrow(e, bean, propName, ctxt);
		    }
		    continue;
		}

		handleUnknownVanilla(p, ctxt, bean, propName);
	    } while ((propName = p.nextFieldName()) != null);
	}
	customReferenceDeserializerCache.popBean();

	return new EntityBean(isReference, bean);
    }

}
