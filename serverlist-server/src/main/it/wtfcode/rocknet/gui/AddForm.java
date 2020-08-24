package it.wtfcode.rocknet.gui;

import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.form.window.FormWindowCustom;
import it.wtfcode.rocknet.gui.FormsType.IFormsType;

public class AddForm extends FormWindowCustom implements IFormsType {	
	public enum Inputs {
		SERVER_NAME("Server Name", "Please enter a Name for this Server"),
		SERVER_ADDRESS("Server Address", "Please enter IP or Address"),
		SERVER_PORT("Server Port", "Please enter Port", "19132");
		private String name;
		private String placeHolder;
		private String defaultValue;
		private Inputs(String name,String placeHolder,String defaultValue) {
			this.name = name;
			this.placeHolder = placeHolder;
			this.defaultValue = defaultValue;
		}
		private Inputs(String name,String placeHolder) {
			this(name,placeHolder,null);
		}
		public String getName() {
			return name;
		}
		public String getPlaceHolder() {
			return placeHolder;
		}
		public String getDefaultValue() {
			return defaultValue;
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
		ADD_LIST("Add to server list",true),
		PREFERRED("Set as Preferred",false);
		private String name;
		private boolean defaultValue;
		private Toggles(String name,boolean defaultValue) {
			this.name = name;
			this.defaultValue = defaultValue;
		}
		public String getName() {
			return name;
		}
		public boolean getDefaultValue() {
			return defaultValue;
		}
		public static Toggles valueOf(int ordinal) {
			for(Toggles value:Toggles.values()) {
				if(value.ordinal() == ordinal)
					return value;
			}
			return null;
		}
	}
	
	public AddForm() {
		super("Connect or Add a Server");
		for(Inputs input: Inputs.values()) 
			if(input.getDefaultValue() != null)
				addElement(new ElementInput(input.getName(), input.getPlaceHolder(), input.getDefaultValue()));
			else 
				addElement(new ElementInput(input.getName(), input.getPlaceHolder()));
		for(Toggles toggle: Toggles.values()) 
			addElement(new ElementToggle(toggle.getName(), toggle.getDefaultValue()));
	}
	@Override
	public FormsType getType() {
		return FormsType.ADD;
	}

}
