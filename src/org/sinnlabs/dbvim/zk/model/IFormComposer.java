/**
 * 
 */
package org.sinnlabs.dbvim.zk.model;

import java.util.List;

import org.sinnlabs.dbvim.ui.IField;

/**
 * @author peter.liverovsky
 *
 */
public interface IFormComposer {
	/**
	 * Returns all fields from the form
	 * @return
	 */
	public List<IField<?>> getFields();
}
