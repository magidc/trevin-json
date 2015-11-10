package org.trevin.json.model.reference;

import java.lang.reflect.Method;

public class ClassKeyReference
    {
    private String fieldName;
    private Method getterMethod;

    public ClassKeyReference(String fieldName, Method getterMethod)
	{
	super();
	this.fieldName = fieldName;
	this.getterMethod = getterMethod;
	}

    public String getFieldName()
	{
	return fieldName;
	}

    public void setFieldName(String fieldName)
	{
	this.fieldName = fieldName;
	}

    public Method getGetterMethod()
	{
	return getterMethod;
	}

    public void setGetterMethod(Method getterMethod)
	{
	this.getterMethod = getterMethod;
	}

    @Override
    public String toString()
	{
	return fieldName;
	}
    }
