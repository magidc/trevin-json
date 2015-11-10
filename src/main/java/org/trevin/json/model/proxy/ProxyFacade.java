package org.trevin.json.model.proxy;

import java.lang.reflect.Modifier;
import java.util.Set;

import org.reflections.Reflections;

import net.sf.cglib.core.NamingPolicy;
import net.sf.cglib.core.Predicate;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;

public class ProxyFacade {

    public ProxyFacade() {
	// initProxies();
    }

    private void initProxies() throws InstantiationException, IllegalAccessException {
	Reflections reflections = new Reflections("");
	Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);

	for (Class<?> clazz : classes) {
	    if (!Modifier.isAbstract(clazz.getModifiers())) {
		try {
		    getProxyFactory(clazz.newInstance());
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	}
    }

    public Factory getProxyFactory(Object entity) {
	Enhancer enhancer = new Enhancer();
	enhancer.setSuperclass(entity.getClass());
	enhancer.setCallback(new ObjectProxy(entity));
	// Making sure the name and UID of the proxies class are always the same
	// every time the application starts
	enhancer.setNamingPolicy(new ModelNamingPolicy());
	enhancer.setSerialVersionUID((long) entity.getClass().getName().hashCode());
	return (Factory) enhancer.create();
    }
}

class ModelNamingPolicy implements NamingPolicy {

    @Override
    public String getClassName(String className, String arg1, Object arg2, Predicate arg3) {
	return className + "_proxy";
    }

}
