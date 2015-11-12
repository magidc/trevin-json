package org.trevin.json.mapper;

import org.trevin.json.deserialization.custom.JsonCustomReferenceDeserializerModifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Adding trevin-json module to com.fasterxml.jackson.databind.ObjectMapper
 * 
 * @author magidc
 *
 */
public class TrevinObjectMapperInitializer {
    public void loadTrevinModule(ObjectMapper objectMapper) {
	addTrevinModules(objectMapper);
    }

    private void addTrevinModules(ObjectMapper objectMapper) {
	SimpleModule simpleModule = new SimpleModule();
	simpleModule.setDeserializerModifier(new JsonCustomReferenceDeserializerModifier());
	objectMapper.registerModule(simpleModule);
    }

}