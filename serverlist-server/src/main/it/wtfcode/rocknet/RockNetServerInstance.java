package it.wtfcode.rocknet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.nukkitx.nbt.NBTOutputStream;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtUtils;
import com.nukkitx.protocol.bedrock.BedrockPacketCodec;
import com.nukkitx.protocol.bedrock.BedrockPong;
import com.nukkitx.protocol.bedrock.BedrockServer;
import com.nukkitx.protocol.bedrock.BedrockServerEventHandler;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.v408.Bedrock_v408;

import it.wtfcode.rocknet.db.IRockNetDB;
import it.wtfcode.rocknet.pojo.RockNetUser;

public class RockNetServerInstance {
	
	private byte[] emptyLevelChunkData;
	private IRockNetDB iRockNetDB;
	private BedrockServer server;
	private BedrockPong pong;
	private BedrockPacketCodec codec;
	private List<RockNetUser> connectedUsers = new ArrayList<>();
    
	public RockNetServerInstance(IRockNetDB iRockNetDB) {
        populateEmptyChunk();
        this.iRockNetDB = iRockNetDB;
       
        InetSocketAddress bindAddress = new InetSocketAddress(
        		Main.getConfig().getAddress(), 
        		Main.getConfig().getPort()
        );
        
        codec = Bedrock_v408.V408_CODEC;
        server = new BedrockServer(bindAddress);
        pong = new BedrockPong();
        pong.setEdition("MCPE");
        pong.setMotd(Main.getConfig().getTitle());
        pong.setSubMotd(Main.getConfig().getSubTitle());
        pong.setMaximumPlayerCount(Main.getConfig().getMaxConnectedPlayers());
        pong.setGameType("Survival");
        pong.setIpv4Port(Main.getConfig().getPort());
        pong.setProtocolVersion(getProtocol());
        pong.setVersion(codec.getMinecraftVersion());
        server.setHandler(new BedrockServerEventHandler() {
            @Override
            public boolean onConnectionRequest(InetSocketAddress address) {
                return true; // Connection will be accepted
            }
            @Nonnull
            public BedrockPong onQuery(InetSocketAddress address) {
            	pong.setPlayerCount(connectedUsers.size());
                return pong;
            }
            @Override
            public void onSessionCreation(BedrockServerSession session) {
            	session.setPacketHandler(new RockNetHandler(session,RockNetServerInstance.this));
            }
        });
        // Start server up
        server.bind().join();
        System.out.println("RockNet Handler Started on "+Main.getConfig().getAddress()+":"+Main.getConfig().getPort());
	}



	public void stop() {
		server.close();
		iRockNetDB.deinitialize();
	}

	
    public int getProtocol() {
        return codec.getProtocolVersion();
    }

    public BedrockPacketCodec getCodec() {
        return codec;
    }
	
	public byte[] getEmptyLevelChunkData() {
		return emptyLevelChunkData;
	}


	private void populateEmptyChunk() throws AssertionError {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(new byte[258]); // Biomes + Border Size + Extra Data Size

            try (NBTOutputStream stream = NbtUtils.createNetworkWriter(outputStream)) {
                stream.writeTag(NbtMap.EMPTY);
            }

            emptyLevelChunkData = outputStream.toByteArray();
        }catch (IOException e) {
            throw new AssertionError("Unable to generate empty level chunk data");
        }
	}



	public IRockNetDB getIRockNetDB() {
		return iRockNetDB;
	}



	public List<RockNetUser> getConnectedUsers() {
		return connectedUsers;
	}
}
