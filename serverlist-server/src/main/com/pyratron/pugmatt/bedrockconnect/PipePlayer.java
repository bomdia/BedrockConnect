package com.pyratron.pugmatt.bedrockconnect;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.nukkitx.math.vector.Vector2f;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.nbt.NBTOutputStream;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtUtils;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.data.AttributeData;
import com.nukkitx.protocol.bedrock.data.GamePublishSetting;
import com.nukkitx.protocol.bedrock.data.GameRuleData;
import com.nukkitx.protocol.bedrock.data.GameType;
import com.nukkitx.protocol.bedrock.data.PlayerPermission;
import com.nukkitx.protocol.bedrock.packet.AvailableEntityIdentifiersPacket;
import com.nukkitx.protocol.bedrock.packet.BiomeDefinitionListPacket;
import com.nukkitx.protocol.bedrock.packet.CreativeContentPacket;
import com.nukkitx.protocol.bedrock.packet.LevelChunkPacket;
import com.nukkitx.protocol.bedrock.packet.MovePlayerPacket;
import com.nukkitx.protocol.bedrock.packet.PlayStatusPacket;
import com.nukkitx.protocol.bedrock.packet.StartGamePacket;
import com.nukkitx.protocol.bedrock.packet.UpdateAttributesPacket;
import com.pyratron.pugmatt.bedrockconnect.gui.UIComponents;
import com.pyratron.pugmatt.bedrockconnect.sql.Data;

import it.wtfcode.rocknet.Main;
import it.wtfcode.rocknet.utils.PaletteManager;

public class PipePlayer {

    private BedrockServerSession session;
    private List<String> serverList = new ArrayList<>();

    private int serverLimit;

    private Data data;

    private String uuid;

    private static final NbtMap EMPTY_TAG = NbtMap.EMPTY;
    private static final byte[] EMPTY_LEVEL_CHUNK_DATA;

    static {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(new byte[258]); // Biomes + Border Size + Extra Data Size

            try (NBTOutputStream stream = NbtUtils.createNetworkWriter(outputStream)) {
                stream.writeTag(EMPTY_TAG);
            }

            EMPTY_LEVEL_CHUNK_DATA = outputStream.toByteArray();
        }catch (IOException e) {
            throw new AssertionError("Unable to generate empty level chunk data");
        }
    }


    public PipePlayer(String uuid, Data data, BedrockServerSession session, List<String> serverList, int serverLimit) {
        this.uuid = uuid;
        this.data = data;
        this.session = session;
        this.serverList = serverList;
        this.serverLimit = serverLimit;

        joinGame();
    }

    public BedrockServerSession getSession() {
        return session;
    }

    public List<String> getServerList() {
        return serverList;
    }

    public void setServerList(List<String> serverList) {
        data.setValueString("servers", UIComponents.serversToFormData(serverList), serverList, uuid);
        this.serverList = serverList;
    }

    public String getUuid() {
        return uuid;
    }

    public int getServerLimit() {
        return serverLimit;
    }

    public void setServerLimit(int serverLimit) {
        data.setValueInt("serverLimit", serverLimit, uuid);
        this.serverLimit = serverLimit;
    }

    public void joinGame() {

        /** for(BedrockPacket packet : BedrockConnect.server.packets) {
            session.sendPacket(packet);
        } **/

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
        //startGamePacket.setUnknownInt0(-1);
        //startGamePacket.setUnknownInt1(-1);
        startGamePacket.setEduEditionOffers(0);
        startGamePacket.setEduFeaturesEnabled(false);
        startGamePacket.setRainLevel(0);
        startGamePacket.setLightningLevel(0);
        //startGamePacket.setPlatformLockedContentConfirmed(false);
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

        //startGamePacket.setOnlySpawningV1Villagers(false);
        //startGamePacket.setMovementServerAuthoritative(false);
        //startGamePacket.setTrial(false);
        startGamePacket.setVanillaVersion("*");

        startGamePacket.setLevelId("world");
        startGamePacket.setLevelName("world");
        //startGamePacket.setWorldName("world");
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
                data2.setData(EMPTY_LEVEL_CHUNK_DATA);
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
    }
}
