package org.trevin.json.deserialization.custom.cache;

public class EntityBean {
    private boolean isReference;
    private Object entity;

    public EntityBean(boolean isReference, Object entity) {
	super();
	this.isReference = isReference;
	this.entity = entity;
    }

    public EntityBean(Object entity) {
	super();
	this.isReference = false;
	this.entity = entity;
    }

    public boolean isReference() {
	return isReference;
    }

    public void setReference(boolean isReference) {
	this.isReference = isReference;
    }

    public Object getEntity() {
	return entity;
    }

    public void setEntity(Object entity) {
	this.entity = entity;
    }

}
