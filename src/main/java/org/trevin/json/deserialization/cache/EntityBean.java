package org.trevin.json.deserialization.cache;

public class EntityBean {
    private boolean isReference;
    private Object bean;

    public EntityBean(boolean isReference, Object bean) {
	super();
	this.isReference = isReference;
	this.bean = bean;
    }

    public EntityBean(Object bean) {
	super();
	this.isReference = false;
	this.bean = bean;
    }

    public boolean isReference() {
	return isReference;
    }

    public void setReference(boolean isReference) {
	this.isReference = isReference;
    }

    public Object getBean() {
	return bean;
    }

    public void setBean(Object bean) {
	this.bean = bean;
    }

}
