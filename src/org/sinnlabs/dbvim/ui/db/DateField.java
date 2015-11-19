/**
 * 
 */
package org.sinnlabs.dbvim.ui.db;

import java.sql.Date;

import org.sinnlabs.dbvim.db.Value;
import org.sinnlabs.dbvim.db.model.DBField;
import org.zkoss.zul.Datebox;

/**
 * Class represents date field
 * @author peter.liverovsky
 *
 */
public class DateField extends BaseField<Date, Datebox> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5371885457579006578L;

	public DateField() {
		this(null);
	}

	public DateField(DBField field) {
		super("/components/datetime.zul", field);
		value.setFormat("medium");
	}
	
	@Override
	public Value<Date> getDBValue() {
		if (value.getRawValue() != null) {
			java.util.Date d = value.getValue();
			Date ds = new Date(d.getTime());
			return new Value<Date>(ds, dbField);
		}
		return new Value<Date>(null, dbField);
	}
	
	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.ui.IField#fromString(java.lang.String)
	 */
	@Override
	public Value<Date> fromString(String string) {
		Date t = Date.valueOf(string);
		return new Value<Date>(t, dbField);
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.ui.IField#fromObject(java.lang.Object)
	 */
	@Override
	public Value<Date> fromObject(Object val) {
		return new Value<Date>((Date) val, dbField);
	}
}
