package org.trevin.json.model.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class ObjectProxy implements MethodInterceptor, Serializable {
    private Object coreObject;
    private static final long serialVersionUID = -8260071125728047431L;

    public ObjectProxy(Object coreObject) {
	super();
	this.coreObject = coreObject;
    }

    public Object getCoreObject() {
	return coreObject;
    }

    public void setCoreObject(Object coreObject) {
	this.coreObject = coreObject;
    }

    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy proxy) throws Throwable {
	return method.invoke(coreObject, args);
    }
}
