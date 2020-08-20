package it.wtfcode.rocknet.gui;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nukkitx.protocol.bedrock.packet.ModalFormRequestPacket;

import it.wtfcode.rocknet.Main;
import it.wtfcode.rocknet.pojo.RockNetServer;
import it.wtfcode.rocknet.utils.UIComponents;

public class MainForm extends ModalFormRequestPacket {

	public MainForm(List<RockNetServer> serversList) {
		setFormId(FormsType.MAIN.ordinal());
		
		JsonObject out = UIComponents.createForm("form", "Server List");
        out.addProperty("content", "");

        JsonArray buttons = new JsonArray();

        buttons.add(UIComponents.createButton("Connect to a Server"));
        buttons.add(UIComponents.createButton("Remove a Server"));
        for(RockNetServer server:serversList) {
        	if(Main.getConfig().isShowServersIcon())
        		buttons.add(UIComponents.createButton(server.getServerName(), server.getIconPath(), FormsType.PATH));
        	else
        		buttons.add(UIComponents.createButton(server.getServerName()));
        }
        buttons.add(UIComponents.createButton("The Hive", "https://forum.playhive.com/uploads/default/original/1X/0d05e3240037f7592a0f16b11b57c08eba76f19c.png", "url"));
        buttons.add(UIComponents.createButton("Mineplex", "https://www.mineplex.com/assets/www-mp/img/footer/footer_smalllogo.png", "url"));
        buttons.add(UIComponents.createButton("CubeCraft Games", "https://i.imgur.com/aFH1NUr.png", "url"));
        buttons.add(UIComponents.createButton("Lifeboat Network", "https://lbsg.net/wp-content/uploads/2017/06/lifeboat-square.png", "url"));
        buttons.add(UIComponents.createButton("Mineville City", "https://pbs.twimg.com/profile_images/1095835578451537920/0-x9qcw8.png", "url"));
        buttons.add(UIComponents.createButton("Galaxite", "https://i.imgur.com/VxXO8Of.png", "url"));
        out.add("buttons", buttons);

        setFormData(out.toString());
	}
}
