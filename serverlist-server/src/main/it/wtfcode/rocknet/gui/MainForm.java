package it.wtfcode.rocknet.gui;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.window.FormWindowSimple;
import it.wtfcode.rocknet.Main;
import it.wtfcode.rocknet.gui.FormsType.IFormsType;
import it.wtfcode.rocknet.pojo.RockNetServer;

public class MainForm extends FormWindowSimple implements IFormsType{
	public static final String MANAGE_A_SERVER = "Manage a Server";
	public static final String ADD_A_SERVER = "Add a Server";
	private int contextButton = 0;
	public MainForm(List<RockNetServer> serversList) {
		super("RockNet Server List", "Choose your Server!");
		addContextButton(ADD_A_SERVER);
		addContextButton(MANAGE_A_SERVER);
        for(RockNetServer server:serversList) {
        	if(Main.getConfig().isShowServersIcon() && StringUtils.isNotBlank(server.getIconPath()))
        		addButton(new ElementButton(server.getServerName(), new ElementButtonImageData(FormsType.URL, server.getIconPath())));
        	else
        		addButton(new ElementButton(server.getServerName()));
        }
	}
	@Override
	public FormsType getType() {
		return FormsType.MAIN;
	}
	public void addContextButton(String name) {
		addButton(new ElementButton(name));
		contextButton = getContextButton() + 1;
	}
	public int getContextButton() {
		return contextButton;
	}
}
