/**
 * 
 */
package org.sinnlabs.dbvim.form;

import java.util.concurrent.ConcurrentHashMap;

import org.sinnlabs.dbvim.model.Form;

/**
 * @author peter.liverovsky
 *
 */
public class FormFieldResolverFactory {
	
	private final static ConcurrentHashMap<String, FormFieldResolver> cache;

	static {
		cache = new ConcurrentHashMap<String, FormFieldResolver>();
	}
	
	/**
	 * Returns the resolver instance
	 * @param f Form
	 * @return FormFieldResolver for the Form
	 * @throws Exception
	 */
	public static FormFieldResolver getResolver(Form f) throws Exception {
		if (!cache.containsKey(f.getName())) {
			synchronized(cache) {
				// second check with lock
				if (!cache.containsKey(f.getName())) {
					FormFieldResolver r = new FormFieldResolver(f);
					cache.put(f.getName(), r);
					return r;
				}
				return cache.get(f.getName());
			}
		} else {
			return cache.get(f.getName());
		}
	}
	
	/**
	 * Returns cached forms count
	 * @return
	 */
	public static int getCacheSize() {
		return cache.size();
	}
	
	/**
	 * Flush forms cache
	 */
	public static void flushCache() {
		synchronized(cache) {
			cache.clear();
		}
	}
	
	/**
	 * Refresh cached form field resolver
	 * @param f form to be refreshed
	 * @return true if entry exists in cache, otherwise false
	 * @throws Exception
	 */
	public static boolean refreshItem(Form f) throws Exception {
		if (cache.containsKey(f.getName())) {
			synchronized (cache) {
				if (cache.containsKey(f.getName())) {
					FormFieldResolver r = new FormFieldResolver(f);
					cache.replace(f.getName(), r);
					return true;
				}
			}
		}
		return false;
	}
}
