package org.trevin.json.mapper;

import org.trevin.json.deserialization.JsonReferenceDeserializerModifier;
import org.trevin.json.model.proxy.ProxyFacade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Adding trevin-json module to com.fasterxml.jackson.databind.ObjectMapper
 * 
 * @author magidc
 *
 */
public class TrevinObjectMapperInitializer {
    public static void loadTrevinModule(ObjectMapper objectMapper) {
	SimpleModule simpleModule = new SimpleModule();
	simpleModule.setDeserializerModifier(new JsonReferenceDeserializerModifier(new ProxyFacade()));
	objectMapper.registerModule(simpleModule);
    }
}