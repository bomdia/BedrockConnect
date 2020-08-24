package it.wtfcode.rocknet.gui;

import java.util.ArrayList;
import java.util.List;

import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.window.FormWindowCustom;
import it.wtfcode.rocknet.gui.FormsType.IFormsType;
import it.wtfcode.rocknet.pojo.RockNetServer;

public class ManageListForm extends FormWindowCustom implements IFormsType{
		private boolean remove;
		
		public ManageListForm(List<RockNetServer> serverList,boolean remove) {
			super("Manage Server");
			this.remove = remove;
			ArrayList<String> serverNameList = new ArrayList<>();
			serverList.forEach(server -> {
				serverNameList.add(server.getServerName());
			});
			addElement(new ElementDropdown("Servers", serverNameList, 0));
		}

		@Override
		public FormsType getType() {
			if(remove)
				return FormsType.REMOVE_LIST;
			else
				return FormsType.EDIT_LIST;
		}
}
