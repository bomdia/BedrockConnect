package it.wtfcode.rocknet.gui;

import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.form.window.FormWindowCustom;
import it.wtfcode.rocknet.gui.FormsType.IFormsType;
import it.wtfcode.rocknet.pojo.RockNetServer;
import it.wtfcode.rocknet.utils.StaticUtil;

public class EditForm extends FormWindowCustom implements IFormsType {
	public enum Inputs {
		SERVER_NAME("Server Name", "Please enter a Name for this Server"),
		SERVER_ADDRESS("Server Address", "Please enter IP or Address"),
		SERVER_PORT("Server Port", "Please enter Port");
		private String name;
		private String placeHolder;
		private Inputs(String name,String placeHolder) {
			this.name = name;
			this.placeHolder = placeHolder;
		}
		public String getName() {
			return name;
		}
		public String getPlaceHolder() {
			return placeHolder;
		}
		public static Inputs valueOf(int ordinal) {
			for(Inputs value:Inputs.values()) {
				if(value.ordinal() == ordinal)
					return value;
			}
			return null;
		}
	}
	public enum Toggles {
		PREFERRED("Set as Preferred");
		private String name;
		private Toggles(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public static Toggles valueOf(int ordinal) {
			for(Toggles value:Toggles.values()) {
				if(value.ordinal() == ordinal)
					return value;
			}
			return null;
		}
	}
	
	private RockNetServer serverToEdit;

	public EditForm(RockNetServer serverToEdit) {
		super("Edit a Server");
		this.serverToEdit = serverToEdit;
		for(Inputs input: Inputs.values()) {
			String methodCamelCased = StaticUtil.toCamelCase("get_"+input.name().toLowerCase());
			String defaultValue = StaticUtil.getReflected(serverToEdit, methodCamelCased,String.class);
			addElement(new ElementInput(input.getName(), input.getPlaceHolder(), defaultValue));
		}
		for(Toggles toggle: Toggles.values()) { 
			String methodCamelCased = StaticUtil.toCamelCase("is_"+toggle.name().toLowerCase());
			boolean defaultValue = StaticUtil.getReflected(serverToEdit, methodCamelCased,boolean.class);
			addElement(new ElementToggle(toggle.getName(), defaultValue));
		}		
	}
	@Override
	public FormsType getType() {
		return FormsType.EDIT;
	}
	public RockNetServer getServerToEdit() {
		return serverToEdit;
	}
	
}
