/**
 * 
 */
package org.sinnlabs.dbvim.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Class represents static resources such as js scripts, images, etc.
 * @author peter.liverovsky
 *
 */
@DatabaseTable
public class StaticResource {

	@DatabaseField(id = true)
	protected String name;
	
	@DatabaseField
	protected String mimeType;
	
	@DatabaseField(width = 1024*1024, dataType = DataType.BYTE_ARRAY)
	protected byte[] data;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContentType() {
		return mimeType;
	}

	public void setContentType(String mimeType) {
		this.mimeType = mimeType;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
