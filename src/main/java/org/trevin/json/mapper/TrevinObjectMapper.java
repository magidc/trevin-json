package org.trevin.json.mapper;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;

/**
 * Alternative ObjectMapper constructor for situations with multiple versions of
 * Jackson parser. Using this class we make sure that Trevin module is loaded
 * 
 * @author user
 *
 */
public class TrevinObjectMapper extends ObjectMapper {
    private static final long serialVersionUID = -5885426164394138192L;

    public TrevinObjectMapper() {
	super();
	TrevinObjectMapperInitializer.loadTrevinModule(this);
    }

    public TrevinObjectMapper(JsonFactory jf, DefaultSerializerProvider sp, DefaultDeserializationContext dc) {
	super(jf, sp, dc);
	TrevinObjectMapperInitializer.loadTrevinModule(this);
    }

    public TrevinObjectMapper(JsonFactory jf) {
	super(jf);
	TrevinObjectMapperInitializer.loadTrevinModule(this);
    }

    public TrevinObjectMapper(ObjectMapper src) {
	super(src);
	TrevinObjectMapperInitializer.loadTrevinModule(this);
    }

}
