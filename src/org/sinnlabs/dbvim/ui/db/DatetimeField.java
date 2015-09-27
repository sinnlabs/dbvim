/**
 * 
 */
package org.sinnlabs.dbvim.ui.db;

import java.sql.Timestamp;
import java.util.Date;

import org.sinnlabs.dbvim.db.Value;
import org.sinnlabs.dbvim.db.model.DBField;
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

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.ui.IField#fromString(java.lang.String)
	 */
	@Override
	public Value<Timestamp> fromString(String string) {
		if (string == null) {
			return new Value<Timestamp>(null, dbField);
		}
		return new Value<Timestamp>(Timestamp.valueOf(string), dbField);
	}
}
