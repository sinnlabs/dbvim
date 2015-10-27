/**
 * 
 */
package org.sinnlabs.dbvim.ui;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.sinnlabs.dbvim.config.ConfigLoader;
import org.sinnlabs.dbvim.model.Form;
import org.sinnlabs.dbvim.zk.model.IFormComposer;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.GenericRichlet;
import org.zkoss.zk.ui.Page;
import org.zkoss.zul.Idspace;

import com.j256.ormlite.stmt.Where;

/**
 * Class that build user interface for forms URL is /formname action - search or
 * submit
 * 
 * @author peter.liverovsy
 *
 */
public class DataRichlet extends GenericRichlet {

	protected Form form;

	protected IFormComposer composer;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.zkoss.zk.ui.Richlet#service(org.zkoss.zk.ui.Page)
	 */
	@Override
	public void service(Page page) throws Exception {

		String[] arr = page.getRequestPath().split("/");
		arr = ArrayUtils.removeElements(arr, "");
		if (arr.length > 1) {
			setError(404);
			return;
		}
		String formName = arr[0]; // formName

		page.setTitle(formName);

		Idspace root = new Idspace();
		root.setId("");
		root.setVflex("1");
		root.setHflex("1");
		root.setPage(page);

		form = getForm(formName);
		if (form == null) {
			System.err.println("ERROR: form does not exist on the server: "
					+ formName);
			HttpServletResponse r = (HttpServletResponse) Executions
					.getCurrent().getNativeResponse();
			r.setStatus(404);
			return;
		}

		page.setTitle(form.getTitle());

		buildSearch(root, formName);
	}

	private void buildSearch(Component root, String formName) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("form", form);
		Executions.createComponents("/components/search_page.zul", root, args);
	}

	private void setError(int err) {
		HttpServletResponse r = (HttpServletResponse) Executions.getCurrent()
				.getNativeResponse();
		r.setStatus(err);
	}

	private Form getForm(String formName) {
		// search current form
		try {
			Where<Form, String> w = ConfigLoader.getInstance().getForms()
					.queryBuilder().where().eq(Form.NAME_FIELD_NAME, formName);
			List<Form> l = ConfigLoader.getInstance().getForms()
					.query(w.prepare());
			if (l.isEmpty()) {
				System.err.println("Form does not exist: " + formName);
				return null;
			}

			return l.get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}