package it.wtfcode.rocknet.gui;

import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.window.FormWindowCustom;
import it.wtfcode.rocknet.gui.FormsType.IFormsType;

public class ErrorForm extends FormWindowCustom implements IFormsType{
    public ErrorForm(String errorMsg) {
		super("ERROR");
		addElement(new ElementLabel(errorMsg));
	}
	@Override
	public FormsType getType() {
		return FormsType.ERROR;
	}
}
