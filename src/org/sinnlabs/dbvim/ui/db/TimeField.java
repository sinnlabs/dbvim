/**
 * 
 */
package org.sinnlabs.dbvim.ui.db;

import java.sql.Time;
import java.util.Date;

import org.sinnlabs.dbvim.db.Value;
import org.sinnlabs.dbvim.db.model.DBField;
import org.zkoss.zul.Timebox;

/**
 * Class represents time field
 * @author peter.liverovsky
 *
 */
public class TimeField extends BaseField<Time, Timebox> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2103045559070955451L;

	public TimeField(DBField dbfield) {
		super("/components/timefield.zul", dbfield);
		
	}
	
	public TimeField() {
		this(null);
	}
	
	@Override
	public Time getValue() {
		Date t = (Date) value.getRawValue();
		if (t!= null)
			return new Time(t.getTime());
		return null;
	}
	
	@Override
	public Value<Time> getDBValue() {
		Date t = (Date) value.getRawValue();
		if (t!= null)
			return new Value<Time>(new Time(t.getTime()), dbField);
		return new Value<Time>(null, dbField);
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.ui.IField#fromString(java.lang.String)
	 */
	@Override
	public Value<Time> fromString(String string) {
		Time t = Time.valueOf(string);
		return new Value<Time>(t, dbField);
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.ui.IField#fromObject(java.lang.Object)
	 */
	@Override
	public Value<Time> fromObject(Object val) {
		return new Value<Time>((Time) val, dbField);
	}
}
