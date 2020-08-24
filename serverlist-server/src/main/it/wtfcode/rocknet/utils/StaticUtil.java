package it.wtfcode.rocknet.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StaticUtil {

	private static final Logger log = LogManager.getLogger(StaticUtil.class.getName());
	
	@SuppressWarnings("unchecked")
	public static <V> V getReflected(Object instance,String method, Class<V> returnType) {
		try {
			Method toReflect = instance.getClass().getMethod(method);
			try {
				Object invoked = toReflect.invoke(instance);
				if(toReflect.getReturnType() == returnType) {					
					return (V) invoked;
				}else if(returnType == String.class) {
					return (V) String.valueOf(invoked);
				}else return (V) invoked;
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				log.error(e);
				return null;
			}
		} catch (NoSuchMethodException | SecurityException e) {
			log.error(e);
			return null;
		}
	}
	
	public static String toCamelCase(String string) {
		StringBuilder sb = new StringBuilder(string);

		for (int i = 0; i < sb.length(); i++) {
		    if (sb.charAt(i) == '_') {
		        sb.deleteCharAt(i);
		        sb.replace(i, i+1, String.valueOf(Character.toUpperCase(sb.charAt(i))));
		    }
		}
		return sb.toString();
	}
}
