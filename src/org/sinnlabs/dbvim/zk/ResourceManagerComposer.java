/**
 * 
 */
package org.sinnlabs.dbvim.zk;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.sinnlabs.dbvim.config.ConfigLoader;
import org.sinnlabs.dbvim.model.Form;
import org.sinnlabs.dbvim.model.StaticResource;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

/**
 * Class represents composer for resource manager UI
 * @author peter.liverovsky
 *
 */
public class ResourceManagerComposer extends SelectorComposer<Window> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1801049028838856988L;

	@Wire
	private Listbox lstResult;
	
	@Wire
	private Listbox lstFilterType;
	
	@Wire
	private Textbox txtQuery;
	
	@Wire
	private Textbox txtName;
	
	@Wire
	private Textbox txtContentType;
	
	@Wire
	private Textbox txtFileName;
		
	@Wire
	private Button btnOpen;
	
	@Wire
	private Button btnSave;
	
	@Wire
	private Button btnUpload;
	
	private byte[] data = null;
	
	private StaticResource current = null;
	
	private boolean isNew = false;
	
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		loadResources();
	}
	
	@Listen("onSelect = #lstResult")
	public void lstResult_onSelect() {
		if (lstResult.getSelectedItem() != null) {
			current = lstResult.getSelectedItem().getValue();
			isNew = false;
			loadCurrentData();
		}
	}
	
	@Listen("onClick = #btnSave")
	public void btnSave_onClick() throws SQLException {
		if (current!=null) {
			current.setName(txtName.getText());
			current.setContentType(txtContentType.getText());
			current.setData(data);
			ConfigLoader.getInstance().getStaticResources().createOrUpdate(current);
			loadResources();
		}
	}
	
	@Listen("onClick = #btnNewResource")
	public void btnNewResource_onClick() {
		current = new StaticResource();
		isNew = true;
		loadCurrentData();
	}
	
	@Listen("onClick = #btnDeleteResource")
	public void btnDeleteResource_onClick() throws SQLException {
		if (current != null && !isNew) {
			ConfigLoader.getInstance().getStaticResources().delete(current);
			loadResources();
		}
	}
	
	@Listen("onUpload = #btnUpload")
	public void btnUpload_onUpload(UploadEvent evnt) throws IOException {
		Media media = evnt.getMedia();
		if (media != null) {
			txtContentType.setText(media.getContentType());
			txtFileName.setText(media.getName());
			txtName.setText(media.getName());
			if (media.isBinary())
				data = IOUtils.toByteArray(media.getStreamData());
			else
				data = media.getStringData().getBytes(Charset.forName("UTF-8"));
		}
	}
	
	private void loadResources() throws SQLException {
		String query = txtQuery.getText();
		String filter = "contains"; 
		if (lstFilterType.getSelectedItem() != null) {
			filter = lstFilterType.getSelectedItem().getValue();
		}
		
		List<StaticResource> resources = null;
		// query all
		if (StringUtils.isBlank(query)) {
			resources = ConfigLoader.getInstance().getStaticResources().queryForAll();
		} else if (StringUtils.isNoneBlank(query) && filter.equals("contains")) {
			QueryBuilder<StaticResource, String> qb = 
					ConfigLoader.getInstance().getStaticResources().queryBuilder();
			Where<StaticResource, String> w = qb.where();
			w.like(Form.NAME_FIELD_NAME, "%"+query+"%");
			resources = ConfigLoader.getInstance().getStaticResources().query(qb.prepare());
		} else if (StringUtils.isNoneBlank(query) && filter.equals("equals")) {
			resources = ConfigLoader.getInstance().getStaticResources().queryForEq(
					StaticResource.NAME_FIELD, query);
		}
		clearSelectedItem();
		lstResult.getItems().clear();
		if (resources != null) {
			for(StaticResource r : resources) {
				Listitem item = new Listitem();
				item.appendChild(new Listcell(r.getName()));
				item.appendChild(new Listcell(r.getContentType()));
				item.setValue(r);
				lstResult.getItems().add(item);
			}
		}
	}
	
	private void clearSelectedItem() {
		current = null;
		txtName.setText("");
		txtFileName.setText("");
		txtContentType.setText("");
		btnOpen.setHref("");
		data = null;
	}
	
	private void loadCurrentData() {
		if (current != null) {
			data = null;
			txtName.setText(current.getName());
			txtContentType.setText(current.getContentType());
			if (current.getData() != null) {
				txtFileName.setText("data");
				data = current.getData();
			}
			if (isNew)
				txtName.setReadonly(false);
			else
				txtName.setReadonly(true);
		}
	}
}
