/**
 * 
 */
zk.$package('org.sinnlabs.wgt');

org.sinnlabs.wgt.DBField = zk.$extends(zul.wgt.Idspace, {
	getValue: function () {
		return this.$f('value').getValue();
	},

	setValue: function (val) {
		this.$f('value').setValue(val);
	}
});