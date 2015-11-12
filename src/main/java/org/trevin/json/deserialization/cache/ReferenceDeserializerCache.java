package org.trevin.json.deserialization.cache;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.trevin.json.model.reference.ClassKeyReference;
import org.trevin.json.model.reference.helper.ReferenceHelper;

import com.fasterxml.jackson.databind.deser.SettableBeanProperty;

public class ReferenceDeserializerCache {
    private final Map<String, Collection<ClassKeyReference>> classKeyReferenceMap = new HashMap<String, Collection<ClassKeyReference>>();
    private final Map<EntityKey, Object> objectPool = new HashMap<EntityKey, Object>();
    private final Map<EntityKey, List<EntitySettableReference>> entitySettableReferenceMap = new HashMap<EntityKey, List<EntitySettableReference>>();
    private final Deque<SettableBeanProperty> settableBeanPropertiesStack = new ArrayDeque<SettableBeanProperty>();
    private final Deque<Object> beanStack = new ArrayDeque<Object>();

    public void pushSettableBeanProperty(SettableBeanProperty settableBeanProperty) {
	settableBeanPropertiesStack.push(settableBeanProperty);
    }

    public void popSettableBeanProperty() {
	settableBeanPropertiesStack.pop();
    }

    public void pushBean(Object bean) {
	beanStack.push(bean);
    }

    public void popBean() {
	beanStack.pop();
    }

    /**
     * Checking cache and managing the given bean
     * 
     * @last_modification: 12 de nov. de 2015
     * @author Ricardo Rodriguez <ricardo.rodcas@gmail.com>
     * @param entityBean
     * @return
     * @throws IOException
     */
    public Object checkInCache(EntityBean entityBean) throws IOException {
	Object bean = entityBean.getBean();
	EntityKey entityKey = new EntityKey(getEntityKeysHashCode(bean), bean.getClass().getName());

	// Adding a complete bean to the pool and loading it in previously found
	// references
	if (!entityBean.isReference()) {
	    objectPool.put(entityKey, bean);
	    if (entitySettableReferenceMap.containsKey(entityKey)) {
		for (EntitySettableReference entitySettableReference : entitySettableReferenceMap.get(entityKey)) {
		    entitySettableReference.setValue(bean);
		}
		// Removing queued bean references as they were already solved
		entitySettableReferenceMap.remove(entityKey);
	    }
	}
	// Replacing reference by the complete object stored in the pool
	else if (objectPool.containsKey(entityKey)) {
	    bean = objectPool.get(entityKey);
	}
	// Queuing bean reference until the complete bean is found
	else {
	    if (!entitySettableReferenceMap.containsKey(entityKey))
		entitySettableReferenceMap.put(entityKey, new ArrayList<EntitySettableReference>());
	    entitySettableReferenceMap.get(entityKey)
		    .add(new EntitySettableReference(beanStack.peek(), settableBeanPropertiesStack.peek()));
	}

	return bean;
    }

    /**
     * 
     * Generating an identifier for the bean based in the configured key
     * parameters
     * 
     * @author Ricardo Rodriguez <ricardo.rodcas@gmail.com>
     * @param entity
     * @return
     * @throws IOException
     */
    private int getEntityKeysHashCode(Object entity) throws IOException {
	if (entity instanceof String)
	    return entity.hashCode();
	if (!classKeyReferenceMap.containsKey(entity.getClass().getName()))
	    ReferenceHelper.addClassKeyReferences(entity.getClass(), classKeyReferenceMap);

	int code = 0;

	for (ClassKeyReference classKeyReference : classKeyReferenceMap.get(entity.getClass().getName())) {
	    try {
		Object keyValue = classKeyReference.getGetterMethod().invoke(entity);
		if (keyValue == null)
		    continue;
		code += getEntityKeysHashCode(keyValue);

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
}

/**
 * Managing the settable attributes of an entity instance.
 * 
 * @author Ricardo Rodriguez <ricardo.rodcas@gmail.com>
 *
 */
class EntitySettableReference {
    private final SettableBeanProperty settableBeanProperty;
    private final Object entity;

    public EntitySettableReference(Object entity, SettableBeanProperty settableBeanProperty) {
	super();
	this.entity = entity;
	this.settableBeanProperty = settableBeanProperty;
    }

    public void setValue(Object value) throws IOException {
	settableBeanProperty.set(entity, value);
    }
}

/**
 * Key holder class for entities
 * 
 * @author Ricardo Rodriguez <ricardo.rodcas@gmail.com>
 *
 */
class EntityKey {
    private Integer entityHashKey;
    private String entityClass;

    public EntityKey(Integer entityHashKey, String entityClass) {
	super();
	this.entityHashKey = entityHashKey;
	this.entityClass = entityClass;
    }

    public String getEntityClass() {
	return entityClass;
    }

    public void setEntityClass(String entityClass) {
	this.entityClass = entityClass;
    }

    public Integer getEntityHashKey() {
	return entityHashKey;
    }

    public void setEntityHashKey(Integer entityHashKey) {
	this.entityHashKey = entityHashKey;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((entityClass == null) ? 0 : entityClass.hashCode());
	result = prime * result + ((entityHashKey == null) ? 0 : entityHashKey.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	EntityKey other = (EntityKey) obj;
	if (entityClass == null) {
	    if (other.entityClass != null)
		return false;
	} else if (!entityClass.equals(other.entityClass))
	    return false;
	if (entityHashKey == null) {
	    if (other.entityHashKey != null)
		return false;
	} else if (!entityHashKey.equals(other.entityHashKey))
	    return false;
	return true;
    }

}