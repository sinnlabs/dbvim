/**
 * 
 */
package org.dbvim.dbuibuilder.zk;

import org.dbvim.dbuibuilder.config.ConfigLoader;
import org.dbvim.dbuibuilder.model.Form;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.A;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;

/**
 * Composer for startup page (index.zul)
 * @author peter.liverovsky
 *
 */
public class StartPageComposer extends SelectorComposer<Component> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6638008020345812736L;
	
	@Wire
	Grid gridForms;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		
		// Fill grid with forms
		gridForms.getRows().getChildren().clear();
		
		for(Form form : ConfigLoader.getInstance().getForms().queryForAll()) {
			Row row = new Row();
			Cell nameCell = new Cell();
			Cell linkCell = new Cell();
			// Create table name cell
			nameCell.appendChild(new Label(form.getTitle()));
			
			// Create table link cell
			A link = new A();
			link.setTarget("_blank");
			link.setHref("/data/" + form.getName());
			link.setLabel(form.getName());
			linkCell.appendChild(link);
			row.appendChild(nameCell);
			row.appendChild(linkCell);
			gridForms.getRows().appendChild(row);
		}
	}

}
