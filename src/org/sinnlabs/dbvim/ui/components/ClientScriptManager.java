/**
 * 
 */
package org.sinnlabs.dbvim.ui.components;

import org.sinnlabs.dbvim.db.Entry;
import org.sinnlabs.dbvim.ui.annotations.EventType;
import org.sinnlabs.dbvim.ui.annotations.WireEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.impl.XulElement;

/**
 * Class represents script manager component
 * @author peter.liverovsky
 *
 */
public class ClientScriptManager extends XulElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8508514667730530699L;
	
	private String sFormLoadedScript;
	
	private String sEntryLoadedScript;
	
	private String sFormModeChangeScript;
	
	public ClientScriptManager() {
		sFormLoadedScript = "";
		sEntryLoadedScript = "";
		sFormModeChangeScript = "";
	}
	
	public void setFormLoadedScript(String script) {
		sFormLoadedScript = script;
	}
	
	public String getFormLoadedScript() {
		return sFormLoadedScript;
	}
	
	public void setEntryLoadedScript(String script) {
		sEntryLoadedScript = script;
	}
	
	public String getEntryLoadedScript() {
		return sEntryLoadedScript;
	}
	
	public void setFormChangeModeScript(String script) {
		sFormModeChangeScript = script;
	}
	
	public String getFormChangeModeScript() {
		return sFormModeChangeScript;
	}
	
	@WireEvent(EventType.FORM_LOADED)
	public void onFormLoaded() {
		Clients.evalJavaScript(sFormLoadedScript);
	}
	
	@WireEvent(EventType.ENTRY_LOADED)
	public void onEntryLoaded(Entry e) {
		Clients.evalJavaScript(sEntryLoadedScript);
	}
	
	@WireEvent(EventType.CHANGE_FORM_MODE)
	public void onChangeFormMode(int mode) {
		Clients.evalJavaScript(sFormModeChangeScript);
	}

	@Override
	protected boolean isChildable() {
		return false;
	}
}
