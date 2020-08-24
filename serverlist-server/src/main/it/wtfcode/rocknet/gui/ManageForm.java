package it.wtfcode.rocknet.gui;

import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;
import it.wtfcode.rocknet.gui.FormsType.IFormsType;

public class ManageForm extends FormWindowSimple implements IFormsType{
    public static final String REMOVE = "Remove";
	public static final String EDIT = "Edit";

	public ManageForm() {
		super("Manage Server","Do you want to");
		addButton(new ElementButton(EDIT));
		addButton(new ElementButton(REMOVE));
	}

	@Override
	public FormsType getType() {
		return FormsType.MANAGE;
	}
}
