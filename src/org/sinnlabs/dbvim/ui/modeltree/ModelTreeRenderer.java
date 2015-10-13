/**
 * 
 */
package org.sinnlabs.dbvim.ui.modeltree;

import org.sinnlabs.dbvim.model.DBConnection;
import org.sinnlabs.dbvim.ui.events.DBConnectionOnDoubleClickEventListener;
import org.sinnlabs.dbvim.ui.events.FormNodeOnDoubleClickEventListener;
import org.sinnlabs.dbvim.zk.model.IDeveloperStudio;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;

/**
 * @author peter.liverovsky
 *
 */
public class ModelTreeRenderer implements TreeitemRenderer<Object> {

	protected IDeveloperStudio developer;
	
	public ModelTreeRenderer(IDeveloperStudio developer) {
		this.developer = developer;
	}
	
	/* (non-Javadoc)
	 * @see org.zkoss.zul.TreeitemRenderer#render(org.zkoss.zul.Treeitem, java.lang.Object, int)
	 */
	@Override
	public void render(Treeitem item, Object node, int index) throws Exception {
		Treerow dataRow = new Treerow();
        dataRow.setParent(item);
        item.setValue(node);
        if (node instanceof DBConnection) {
        	DBConnection conn = (DBConnection)node;
        	//Hlayout layout = new Hlayout();
        	//layout.appendChild(new Image("/images/connector3.png"));
            //layout.appendChild(new Label(conn.getName()));
            //layout.setSclass("h-inline-block");
            Treecell img = new Treecell();
            img.appendChild(new Image("/images/connector3.png"));
            Treecell treeCell = new Treecell();
            treeCell.appendChild(new Image("/images/connector3.png"));
            treeCell.appendChild(new Label(conn.getName()));
            //dataRow.appendChild(img);
            dataRow.appendChild(treeCell);
            item.addEventListener(Events.ON_DOUBLE_CLICK, 
            		new DBConnectionOnDoubleClickEventListener(developer));
        }
        if (node instanceof TablesTreeNode) {
        	//Hlayout layout = new Hlayout();
        	//layout.appendChild(new Image("/images/file98.png"));
            //layout.appendChild(new Label("Tables"));
            //layout.setSclass("h-inline-block");
        	Treecell img = new Treecell();
        	img.appendChild(new Image("/images/file98.png"));
            Treecell treeCell = new Treecell();
            treeCell.appendChild(new Image("/images/file98.png"));
            treeCell.appendChild(new Label("Tables"));
            
            //dataRow.appendChild(img);
            dataRow.appendChild(treeCell);
        }
        if (node instanceof FormsTreeNode) {
        	Hlayout layout = new Hlayout();
        	layout.appendChild(new Image("/images/file186.png"));
            layout.appendChild(new Label("Forms"));
            //layout.setSclass("h-inline-block");
            Treecell treeCell = new Treecell();
            Treecell img = new Treecell();
            img.appendChild(new Image("/images/file186.png"));
            treeCell.appendChild(new Image("/images/file186.png"));
            treeCell.appendChild(new Label("Forms"));
            //dataRow.appendChild(img);
            dataRow.appendChild(treeCell);
        }
        if (node instanceof TableTreeNode) {
        	TableTreeNode tableNode = (TableTreeNode) node;
        	Treecell cell = new Treecell();
        	cell.appendChild(new Image("/images/tables1.png"));
        	cell.appendChild(new Label(tableNode.getTable().getName() + 
        			" (" + tableNode.getTable().getCatalog() + ")"));
        	Treecell img = new Treecell();
        	img.appendChild(new Image("/images/tables1.png"));
        	//dataRow.appendChild(img);
        	dataRow.appendChild(cell);
        }
        if (node instanceof FormTreeNode) {
        	FormTreeNode formNode = (FormTreeNode) node;
        	Treecell cell = new Treecell();
        	cell.appendChild(new Image("/images/dossier.png"));
        	cell.appendChild(new Label(formNode.getForm().getName()));
        	Treecell img = new Treecell();
        	img.appendChild(new Image("/images/dossier.png"));
        	//dataRow.appendChild(img);
        	dataRow.appendChild(cell);
        	item.addEventListener(Events.ON_DOUBLE_CLICK, 
        			new FormNodeOnDoubleClickEventListener(developer));
        }
	}

}
