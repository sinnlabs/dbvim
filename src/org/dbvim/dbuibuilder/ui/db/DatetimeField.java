/**
 * 
 */
package org.dbvim.dbuibuilder.ui.db;

import java.sql.Timestamp;
import java.util.Date;

import org.dbvim.dbuibuilder.db.Value;
import org.dbvim.dbuibuilder.db.model.DBField;
import org.zkoss.zul.Datebox;

/**
 * @author peter.liverovsky
 *
 */
public class DatetimeField extends BaseField<Timestamp, Datebox> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5742329342967041266L;
	
	public DatetimeField() {
		this(null);
	}

	public DatetimeField(DBField field) {
		super("/components/datetime.zul", field);
		this.value.setFormat("medium+medium");
	}
	
	@Override
	public Value<Timestamp> getDBValue() {
		if (value.getRawValue() != null) {
			Date d = value.getValue();
			Timestamp ts = new Timestamp(d.getTime());
			return new Value<Timestamp>(ts, dbField);
		}
		return new Value<Timestamp>(null, dbField);
	}
}
