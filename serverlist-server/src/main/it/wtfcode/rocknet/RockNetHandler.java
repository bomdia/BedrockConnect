package it.wtfcode.rocknet;

import java.io.IOException;
import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nukkitx.math.vector.Vector2f;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.network.util.Preconditions;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.data.AttributeData;
import com.nukkitx.protocol.bedrock.data.GamePublishSetting;
import com.nukkitx.protocol.bedrock.data.GameRuleData;
import com.nukkitx.protocol.bedrock.data.GameType;
import com.nukkitx.protocol.bedrock.data.PlayerPermission;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.AvailableEntityIdentifiersPacket;
import com.nukkitx.protocol.bedrock.packet.BiomeDefinitionListPacket;
import com.nukkitx.protocol.bedrock.packet.ChunkRadiusUpdatedPacket;
import com.nukkitx.protocol.bedrock.packet.CreativeContentPacket;
import com.nukkitx.protocol.bedrock.packet.LevelChunkPacket;
import com.nukkitx.protocol.bedrock.packet.LoginPacket;
import com.nukkitx.protocol.bedrock.packet.ModalFormRequestPacket;
import com.nukkitx.protocol.bedrock.packet.ModalFormResponsePacket;
import com.nukkitx.protocol.bedrock.packet.MovePlayerPacket;
import com.nukkitx.protocol.bedrock.packet.PlayStatusPacket;
import com.nukkitx.protocol.bedrock.packet.RequestChunkRadiusPacket;
import com.nukkitx.protocol.bedrock.packet.ResourcePackClientResponsePacket;
import com.nukkitx.protocol.bedrock.packet.ResourcePackStackPacket;
import com.nukkitx.protocol.bedrock.packet.ResourcePacksInfoPacket;
import com.nukkitx.protocol.bedrock.packet.SetEntityMotionPacket;
import com.nukkitx.protocol.bedrock.packet.SetLocalPlayerAsInitializedPacket;
import com.nukkitx.protocol.bedrock.packet.StartGamePacket;
import com.nukkitx.protocol.bedrock.packet.TransferPacket;
import com.nukkitx.protocol.bedrock.packet.UpdateAttributesPacket;
import com.nukkitx.protocol.bedrock.util.EncryptionUtils;

import cn.nukkit.form.window.FormWindow;
import it.wtfcode.rocknet.gui.AddForm;
import it.wtfcode.rocknet.gui.EditForm;
import it.wtfcode.rocknet.gui.ErrorForm;
import it.wtfcode.rocknet.gui.FormsType;
import it.wtfcode.rocknet.gui.FormsType.IFormsType;
import it.wtfcode.rocknet.gui.MainForm;
import it.wtfcode.rocknet.gui.ManageForm;
import it.wtfcode.rocknet.gui.ManageListForm;
import it.wtfcode.rocknet.pojo.RockNetServer;
import it.wtfcode.rocknet.pojo.RockNetUser;
import it.wtfcode.rocknet.utils.PaletteManager;
import net.minidev.json.JSONObject;

public class RockNetHandler implements BedrockPacketHandler{

	private static final Logger log = LogManager.getLogger(RockNetHandler.class.getName());
	private BedrockServerSession session;
	private RockNetServerInstance rockNetServer;
	private RockNetUser currentUser;
	private FormWindow currentForm;
	private List<RockNetServer> manageUserServersList;
	
    public RockNetHandler(BedrockServerSession session, RockNetServerInstance rockNetServer) {
		this.session = session;
		this.rockNetServer = rockNetServer;
		this.session.addDisconnectHandler((DisconnectReason) -> disconnect());
	}

	public static ModalFormRequestPacket createRequest(FormsType type, FormWindow form) {
		ModalFormRequestPacket mf = new ModalFormRequestPacket();
		mf.setFormId(type.ordinal());
        mf.setFormData(form.getJSONData());
        log.debug("request form data:");
        log.debug(form.getJSONData());
        return mf;
	}
    
    @Override
    public boolean handle(SetLocalPlayerAsInitializedPacket packet) {
    	goToMainForm();
        return false;
    }
    
    @Override
    public boolean handle(ModalFormResponsePacket packet) {
    	if(validFormPacket(packet)) {
	    	currentForm.setResponse(packet.getFormData());
	    	
	        final FormsType formType = ((IFormsType)currentForm).getType();
	        
			switch (formType) {
	        case MAIN:
	        	if (currentForm.wasClosed()) //if you close main form you want to exit
	        		session.disconnect("Bye Bye RockNet User! <3");
	        	else {
	        		MainForm form = (MainForm) currentForm;
	        		int chosen = form.getResponse().getClickedButtonId();
	        		if(chosen >= form.getContextButton()) { //server List
	        			RockNetServer server = currentUser.getServerList().get(chosen-form.getContextButton());
	        			try {
		        			transfer(server.getServerAddress(),server.getServerPort());
	        			} catch (Exception e) {
	        				goToErrorForm("Error connecting to server. Invalid address.");
	        			}
	        		}else { //context menu
	        			if(StringUtils.equals(form.getResponse().getClickedButton().getText(),MainForm.ADD_A_SERVER)) {
	        				goToAddForm();
	        			}else if(StringUtils.equals(form.getResponse().getClickedButton().getText(),MainForm.MANAGE_A_SERVER)) {
	        				goToManageForm();
	        			}
	        		}
	        	}
	        	break;
			case ADD:
				if (currentForm.wasClosed()) {
					goToMainForm();
				}else {
					AddForm form = (AddForm) currentForm;
					String serverName = form.getResponse().getInputResponse(AddForm.Inputs.SERVER_NAME.ordinal());
					String serverAddress = form.getResponse().getInputResponse(AddForm.Inputs.SERVER_ADDRESS.ordinal());
					String serverPort = form.getResponse().getInputResponse(AddForm.Inputs.SERVER_PORT.ordinal());
					boolean addToList = form.getResponse().getToggleResponse(AddForm.Toggles.ADD_LIST.ordinal());
					boolean isPreferred = form.getResponse().getToggleResponse(AddForm.Toggles.PREFERRED.ordinal());
					
					if(isValidServer(serverName,serverAddress,serverPort,addToList)) {
						Integer sPort = Integer.valueOf(serverPort);
						if(addToList) {
							rockNetServer.getIRockNetDB().createServer(serverAddress, sPort, "", isPreferred);
							rockNetServer.getIRockNetDB().attachUserToServer(currentUser.getXuid(), serverAddress, sPort, serverName, isPreferred);
							goToMainForm();
						} else {
							transfer(serverAddress,sPort);
						}
					}
				}
				break;
			case EDIT:
				if (currentForm.wasClosed()) {
					goToMainForm();
				}else {
					EditForm form = (EditForm) currentForm;
					String serverName = form.getResponse().getInputResponse(EditForm.Inputs.SERVER_NAME.ordinal());
					String serverAddress = form.getResponse().getInputResponse(EditForm.Inputs.SERVER_ADDRESS.ordinal());
					String serverPort = form.getResponse().getInputResponse(EditForm.Inputs.SERVER_PORT.ordinal());
					boolean isPreferred = form.getResponse().getToggleResponse(EditForm.Toggles.PREFERRED.ordinal());
					
					if(isValidServer(serverName,serverAddress,serverPort,true)) {
						Integer sPort = Integer.valueOf(serverPort);
						RockNetServer to = new RockNetServer(serverAddress,sPort,serverName,"",isPreferred);
						rockNetServer.getIRockNetDB().updateServer(currentUser.getXuid(),form.getServerToEdit(),to);
						goToManageList(false);
					}
				}
				break;
			case MANAGE:
				if (currentForm.wasClosed()) {
					goToMainForm();
				}else {
					ManageForm form = (ManageForm) currentForm;
					if(StringUtils.equals(form.getResponse().getClickedButton().getText(),ManageForm.EDIT)){
						goToManageList(false);
					}else if(StringUtils.equals(form.getResponse().getClickedButton().getText(), ManageForm.REMOVE)) {
						goToManageList(true);
					}
				}
				break;
			case EDIT_LIST:
				if (currentForm.wasClosed()) {
					goToManageForm();
				}else {
					ManageListForm form = (ManageListForm) currentForm;
					RockNetServer choosenServer = manageUserServersList.get(form.getResponse().getDropdownResponse(0).getElementID());
					goToEditForm(choosenServer);
					
				}
				break;
			case REMOVE_LIST:
				if (currentForm.wasClosed()) {
					goToManageForm();
				}else {
					ManageListForm form = (ManageListForm) currentForm;
					RockNetServer choosenServer = manageUserServersList.get(form.getResponse().getDropdownResponse(0).getElementID());
					rockNetServer.getIRockNetDB().removeServer(choosenServer);
					goToManageList(true);
				}
				break;
			case ERROR:
				goToMainForm();
				break;
			default:
				break;
	        }
    	}
        return false;
    }

	@Override
    public boolean handle(RequestChunkRadiusPacket packet) {
        ChunkRadiusUpdatedPacket chunkRadiusUpdatePacket = new ChunkRadiusUpdatedPacket();
        chunkRadiusUpdatePacket.setRadius(packet.getRadius());
        session.sendPacketImmediately(chunkRadiusUpdatePacket);
        PlayStatusPacket playStatus = new PlayStatusPacket();
        playStatus.setStatus(PlayStatusPacket.Status.PLAYER_SPAWN);
        session.sendPacket(playStatus);
        return false;
    }
	
	@Override
	public boolean handle(ResourcePackClientResponsePacket packet) {
		switch (packet.getStatus()) {
		case COMPLETED:
			joinGame();
			break;
		case HAVE_ALL_PACKS:
			ResourcePackStackPacket rs = new ResourcePackStackPacket();
		    rs.setForcedToAccept(false);
		    rs.setGameVersion(rockNetServer.getCodec().getMinecraftVersion());
		    session.sendPacket(rs);
		    break;
		default:
		    session.disconnect("disconnectionScreen.resourcePack");
	        break;
	    }
	    return true;
	}

    @Override
    public boolean handle(LoginPacket packet) {
        int protocolVersion = packet.getProtocolVersion();
        if (protocolVersion != rockNetServer.getProtocol()) {
            PlayStatusPacket status = new PlayStatusPacket();
            if (protocolVersion > rockNetServer.getProtocol()) {
                status.setStatus(PlayStatusPacket.Status.LOGIN_FAILED_SERVER_OLD);
            } else {
                status.setStatus(PlayStatusPacket.Status.LOGIN_FAILED_CLIENT_OLD);
            }
            session.sendPacket(status);
        }
        session.setPacketCodec(rockNetServer.getCodec());

        JsonNode certData;
        try {
            certData = Main.mapper.readTree(packet.getChainData().toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Certificate JSON can not be read.");
        }

        JsonNode certChainData = certData.get("chain");
        if (certChainData.getNodeType() != JsonNodeType.ARRAY) {
            throw new RuntimeException("Certificate data is not valid");
        }

        try {
            if(!validateChainData(certChainData)) throw new RuntimeException("Invalid Chain Data!");

            JWSObject jwt = JWSObject.parse(certChainData.get(certChainData.size() - 1).asText());
            JsonNode payload = Main.mapper.readTree(jwt.getPayload().toBytes());

            if (payload.get("extraData").getNodeType() != JsonNodeType.OBJECT) {
                throw new RuntimeException("AuthData was not found!");
            }

            JSONObject extraData = (JSONObject) jwt.getPayload().toJSONObject().get("extraData");

            if (payload.get("identityPublicKey").getNodeType() != JsonNodeType.STRING) {
                throw new RuntimeException("Identity Public Key was not found!");
            }
            
            ECPublicKey identityPublicKey = EncryptionUtils.generateKey(payload.get("identityPublicKey").textValue());

            JWSObject clientJwt = JWSObject.parse(packet.getSkinData().toString());
            verifyJwt(clientJwt, identityPublicKey);

            String xuid = extraData.getAsString("XUID");
            
            String name = extraData.getAsString("displayName");

            setCurrentUser(xuid,name);
            
            PlayStatusPacket status = new PlayStatusPacket();
            status.setStatus(PlayStatusPacket.Status.LOGIN_SUCCESS);
            session.sendPacket(status);

            SetEntityMotionPacket motion = new SetEntityMotionPacket();
            motion.setRuntimeEntityId(1);
            motion.setMotion(Vector3f.ZERO);
            session.sendPacket(motion);

            ResourcePacksInfoPacket resourcePacksInfo = new ResourcePacksInfoPacket();
            resourcePacksInfo.setForcedToAccept(false);
            resourcePacksInfo.setScriptingEnabled(false);
            session.sendPacket(resourcePacksInfo);
        } catch (Exception e) {
            session.disconnect("disconnectionScreen.internalError.cantConnect");
            throw new RuntimeException("Unable to complete login", e);
        }
        return true;
    }

    public void disconnect() {
        rockNetServer.getConnectedUsers().remove(currentUser);
    }
    
	private void goToManageList(boolean remove) {
		manageUserServersList = rockNetServer.getIRockNetDB().getUserServers(currentUser.getXuid());
		currentForm = new ManageListForm(manageUserServersList,remove);
		session.sendPacketImmediately(createRequest(remove ? FormsType.REMOVE_LIST : FormsType.EDIT_LIST,currentForm));
	}

	private void goToManageForm() {
		currentForm = new ManageForm();
		session.sendPacketImmediately(createRequest(FormsType.MANAGE,currentForm));
	}

	private void goToErrorForm(String errorMsg) {
		currentForm = new ErrorForm(errorMsg);
		session.sendPacketImmediately(createRequest(FormsType.ERROR,currentForm));
	}

	private void goToEditForm(RockNetServer choosenServer) {
		currentForm = new EditForm(choosenServer);
		session.sendPacketImmediately(createRequest(FormsType.EDIT,currentForm));
	}

	private void goToAddForm() {
		currentForm = new AddForm();
		session.sendPacketImmediately(createRequest(FormsType.ADD,currentForm));
	}

	private void goToMainForm() {
		refreshCurrentUserServerList();
		currentForm = new MainForm(currentUser.getServerList());
		session.sendPacketImmediately(createRequest(FormsType.MAIN,currentForm));
	}

	private void refreshCurrentUserServerList(){
		List<RockNetServer> uList = rockNetServer.getIRockNetDB().getUserServers(currentUser.getXuid());
		uList.addAll(rockNetServer.getIRockNetDB().getAllGlobalServers());
		currentUser.setServerList(uList);
	}
	
	private boolean isValidServer(String serverName, String serverAddress, String serverPort, boolean addToList) {
		if(((addToList && StringUtils.isNotBlank(serverName))||!addToList) && StringUtils.isNotBlank(serverAddress) && StringUtils.isNotBlank(serverPort)) {
			if(addToList && serverName.length() >= 253) {
				goToErrorForm("Name is too large. (Must be less than 253)");
				return false;
			}
			if(serverAddress.length() >= 253) {
				goToErrorForm("Address is too large. (Must be less than 253)");
				return false;
			}
			if(serverPort.length() >= 10) {
				goToErrorForm("Port is too large. (Must be less than 10)");
				return false;
			}
			if (!serverAddress.matches("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")
				&& !serverAddress.matches("^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$")) {        	  
				goToErrorForm("Enter a valid address. (E.g. play.example.net, 172.16.254.1)");
				return false;
			}
			int sPort = -1;
			try {
				sPort = Integer.valueOf(serverPort);
			}catch (NumberFormatException e) {
				log.error(e);
				goToErrorForm("Enter a valid port with only numbers");
				return false;
			}
			if(sPort<0 || sPort > 65535) {
				goToErrorForm("Enter a valid port the valid range is from 0 to 65535");
				return false;
			}
			return true;
		}else {
			if(addToList) 
				goToErrorForm("All input need to be filled in");
			else
				goToErrorForm("For direct connect you need to fill the address and the port");
			return false;
		}
	}

	private boolean validFormPacket(ModalFormResponsePacket packet) {
		return currentForm != null && currentForm instanceof IFormsType && packet.getFormId() == ((IFormsType)currentForm).getType().ordinal();
	}

	private static boolean validateChainData(JsonNode data) throws Exception {
        ECPublicKey lastKey = null;
        boolean validChain = false;
        for (JsonNode node : data) {
            JWSObject jwt = JWSObject.parse(node.asText());

            if (!validChain) {
                validChain = verifyJwt(jwt, EncryptionUtils.getMojangPublicKey());
            }

            if (lastKey != null) {
                verifyJwt(jwt, lastKey);
            }

            JsonNode payloadNode = Main.mapper.readTree(jwt.getPayload().toString());
            JsonNode ipkNode = payloadNode.get("identityPublicKey");
            Preconditions.checkState(ipkNode != null && ipkNode.getNodeType() == JsonNodeType.STRING, "identityPublicKey node is missing in chain");
            lastKey = EncryptionUtils.generateKey(ipkNode.asText());
        }
        return validChain;
    }


    private static boolean verifyJwt(JWSObject jwt, ECPublicKey key) throws JOSEException {
        return jwt.verify(new DefaultJWSVerifierFactory().createJWSVerifier(jwt.getHeader(), key));
    }
	
    public void transfer(String address, int port) {
        TransferPacket tp = new TransferPacket();
        tp.setAddress(address);
        tp.setPort(port);
        session.sendPacketImmediately(tp);
    }
	
    private void setCurrentUser(String xuid, String name) {
    	RockNetUser tempUser = rockNetServer.getIRockNetDB().getUser(xuid);
    	
    	if(tempUser != null) {
    		if(!StringUtils.equals(tempUser.getUserName(),name)) {
    			tempUser.setUserName(name);
    			rockNetServer.getIRockNetDB().updateUserName(tempUser);
    		}
    	}else {
    		tempUser = rockNetServer.getIRockNetDB().createUser(xuid, name);
    	}
    	
    	currentUser = tempUser;
    	
    }
    
	public void joinGame() {
        MovePlayerPacket mp = new MovePlayerPacket();
        mp.setRuntimeEntityId(1);
        mp.setOnGround(false);
        mp.setMode(MovePlayerPacket.Mode.NORMAL);
        mp.setRotation(Vector3f.from(0,0,0));
        mp.setPosition(Vector3f.from(0,0,0));
        session.sendPacket(mp);

        StartGamePacket startGamePacket = new StartGamePacket();
        startGamePacket.setUniqueEntityId(1);
        startGamePacket.setRuntimeEntityId(1);
        startGamePacket.setPlayerGameType(GameType.SURVIVAL);
        startGamePacket.setPlayerPosition(Vector3f.from(0, 0, 0));
        startGamePacket.setRotation(Vector2f.from(1, 1));

        startGamePacket.setSeed(-1);
        startGamePacket.setDimensionId(0);
        startGamePacket.setGeneratorId(1);
        startGamePacket.setLevelGameType(GameType.SURVIVAL);
        startGamePacket.setDifficulty(1);
        startGamePacket.setDefaultSpawn(Vector3i.ZERO);
        startGamePacket.setAchievementsDisabled(false);
        startGamePacket.setCurrentTick(-1);
        startGamePacket.setEduEditionOffers(0);
        startGamePacket.setEduFeaturesEnabled(false);
        startGamePacket.setRainLevel(0);
        startGamePacket.setLightningLevel(0);
        startGamePacket.setMultiplayerGame(true);
        startGamePacket.setBroadcastingToLan(true);
        startGamePacket.getGamerules().add((new GameRuleData<>("showcoordinates", true)));
        startGamePacket.setPlatformBroadcastMode(GamePublishSetting.PUBLIC);
        startGamePacket.setXblBroadcastMode(GamePublishSetting.PUBLIC);
        startGamePacket.setCommandsEnabled(true);
        startGamePacket.setTexturePacksRequired(false);
        startGamePacket.setBonusChestEnabled(false);
        startGamePacket.setStartingWithMap(false);
        startGamePacket.setTrustingPlayers(true);
        startGamePacket.setDefaultPlayerPermission(PlayerPermission.MEMBER);
        startGamePacket.setServerChunkTickRange(4);
        startGamePacket.setBehaviorPackLocked(false);
        startGamePacket.setResourcePackLocked(false);
        startGamePacket.setFromLockedWorldTemplate(false);
        startGamePacket.setUsingMsaGamertagsOnly(false);
        startGamePacket.setFromWorldTemplate(false);
        startGamePacket.setWorldTemplateOptionLocked(false);
        startGamePacket.setVanillaVersion("*");
        startGamePacket.setLevelId("world");
        startGamePacket.setLevelName("world");
        startGamePacket.setPremiumWorldTemplateId("00000000-0000-0000-0000-000000000000");
        startGamePacket.setCurrentTick(0);
        startGamePacket.setEnchantmentSeed(0);
        startGamePacket.setMultiplayerCorrelationId("");
        startGamePacket.setBlockPalette(Main.getPaletteManager().getCachedPalette());

        session.sendPacket(startGamePacket);

        spawn();
    }

    public void spawn() {

        Vector3f pos = Vector3f.ZERO;
        int chunkX = pos.getFloorX() >> 4;
        int chunkZ = pos.getFloorX() >> 4;

        for (int x = -3; x < 3; x++) {
            for (int z = -3; z < 3; z++) {
                LevelChunkPacket data2 = new LevelChunkPacket();
                data2.setChunkX(chunkX + x);
                data2.setChunkZ(chunkZ + z);
                data2.setSubChunksLength(0);
                data2.setData(rockNetServer.getEmptyLevelChunkData());
                session.sendPacket(data2);
            }
        }

        BiomeDefinitionListPacket biomePacket = new BiomeDefinitionListPacket();
        biomePacket.setDefinitions(PaletteManager.BIOMES);
        session.sendPacket(biomePacket);

        AvailableEntityIdentifiersPacket entityPacket = new AvailableEntityIdentifiersPacket();
        entityPacket.setIdentifiers(PaletteManager.ENTITY_IDENTIFIERS);
        session.sendPacket(entityPacket);

        CreativeContentPacket creativePacket = new CreativeContentPacket();
        creativePacket.setContents(PaletteManager.CREATIVE_ITEMS);
        session.sendPacket(creativePacket);

        PlayStatusPacket playStatus = new PlayStatusPacket();
        playStatus.setStatus(PlayStatusPacket.Status.PLAYER_SPAWN);
        session.sendPacket(playStatus);

        UpdateAttributesPacket attributesPacket = new UpdateAttributesPacket();
        attributesPacket.setRuntimeEntityId(0);
        List<AttributeData> attributes = new ArrayList<>();
        // Default move speed
        // Bedrock clients move very fast by default until they get an attribute packet correcting the speed
        attributes.add(new AttributeData("minecraft:movement", 0.0f, 1024f, 0.1f, 0.1f));
        attributesPacket.setAttributes(attributes);
        session.sendPacket(attributesPacket);
        
        rockNetServer.getConnectedUsers().add(currentUser);
    }
}
