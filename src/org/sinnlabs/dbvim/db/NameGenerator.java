/**
 * 
 */
package org.sinnlabs.dbvim.db;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Join sub query name generator
 * @author peter.liverovsky
 *
 */
class NameGenerator {
	private AtomicInteger start;
	
	private String prefix;
	
	public NameGenerator(String prefix) {
		start = new AtomicInteger(1);
		this.prefix = prefix;
	}
	
	public String getNext() {
		return prefix + start.getAndIncrement();
	}
}
