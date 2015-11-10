package org.trevin.json.model.reference.helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.trevin.json.key.annotation.JsonEntityKey;
import org.trevin.json.model.reference.ClassKeyReference;

public class ReferenceHelper {

    public static final String IS_REFERENCE_FIELD_NAME = "jref";

    public static List<ClassKeyReference> findKeyReferences(Class<?> classObject) {
	List<ClassKeyReference> classKeyReferenceList = new ArrayList<ClassKeyReference>();

	for (Field field : classObject.getDeclaredFields()) {
	    if (field.isAnnotationPresent(JsonEntityKey.class)) {
		classKeyReferenceList.add(new ClassKeyReference(field.getName(), findGetterMethod(classObject, field)));
	    }
	}
	if (!Object.class.equals(classObject.getSuperclass()))
	    classKeyReferenceList.addAll(findKeyReferences(classObject.getSuperclass()));

	return classKeyReferenceList;
    }

    private static Method findGetterMethod(Class<?> classObject, Field field) {
	String methodName = "get" + StringUtils.capitalize(field.getName());
	Method foundMethod = null;
	for (Method method : classObject.getMethods()) {
	    if (method.getName().equals(methodName)) {
		foundMethod = method;
		break;
	    }
	}

	return foundMethod;
    }

    public static void addClassKeyReferences(Class<?> clazz,
	    Map<String, Collection<ClassKeyReference>> classKeyReferenceMap) {
	if (!classKeyReferenceMap.containsKey(clazz.getName())) {
	    classKeyReferenceMap.put(clazz.getName(), ReferenceHelper.findKeyReferences(clazz));
	}
    }

}
