package it.wtfcode.rocknet.gui;

public enum FormsType {
	MAIN,
	ADD,
	EDIT,
	MANAGE,
	EDIT_LIST,
	REMOVE_LIST,
	ERROR;
	public static final String PATH = "path";
	public static final String URL = "url";
	public static FormsType valueOf(int ordinal) {
		for(FormsType value:FormsType.values()) {
			if(value.ordinal() == ordinal)
				return value;
		}
		return null;
	}
	public interface IFormsType {
		public FormsType getType();
	}
}
