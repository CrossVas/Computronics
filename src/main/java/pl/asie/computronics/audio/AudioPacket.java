package pl.asie.computronics.audio;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import pl.asie.computronics.Computronics;
import pl.asie.computronics.network.Packets;
import pl.asie.lib.network.Packet;

public final class AudioPacket {
	public enum Type {
		DFPWM;
	}

	private static int _idGen;
	private static int getNewId() {
		return _idGen++;
	}

	public final IAudioSource source;
	public final int id;
	public final World world;
	public final Type type;
	public final int frequency;
	public final byte[] data;

	private final Set<IAudioReceiver> receivers = new HashSet<IAudioReceiver>();

	public AudioPacket(IAudioSource source, World world, Type type, int freq, byte[] data) {
		this.id = getNewId();
		this.source = source;
		this.world = world;
		this.type = type;
		this.frequency = freq;
		this.data = data;
	}

	public void addReceiver(IAudioReceiver receiver) {
		if (receiver.getSoundWorld() != null && receiver.getSoundWorld().provider.dimensionId == world.provider.dimensionId
				&& receiver.getSoundVolume() > 0 && receiver.getSoundDistance() > 0) {
			receivers.add(receiver);
		}
	}

	public void send() {
		try {
			for (EntityPlayerMP playerMP : (List<EntityPlayerMP>) MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
				if (playerMP == null || playerMP.worldObj.provider.dimensionId != world.provider.dimensionId) {
					continue;
				}

				Set<IAudioReceiver> receiversLocal = new HashSet<IAudioReceiver>();

				for (IAudioReceiver receiver : receivers) {
					int mdSq = receiver.getSoundDistance() * receiver.getSoundDistance();
					double distSq = (receiver.getSoundX() - playerMP.posX) * (receiver.getSoundX() - playerMP.posX);
					distSq += (receiver.getSoundY() - playerMP.posY) * (receiver.getSoundY() - playerMP.posY);
					distSq += (receiver.getSoundZ() - playerMP.posZ) * (receiver.getSoundZ() - playerMP.posZ);
					if (distSq <= mdSq) {
						receiversLocal.add(receiver);
					}
				}

				if (receiversLocal.size() > 0) {
					Packet pkt = Computronics.packet.create(Packets.PACKET_AUDIO_DATA)
							.writeInt(world.provider.dimensionId)
							.writeInt(id)
							.writeInt(this.frequency);

					pkt.writeShort((short) data.length);
					pkt.writeByteArrayData(data);

					pkt.writeShort((short) receiversLocal.size());

					for (IAudioReceiver receiver : receiversLocal) {
						pkt.writeInt(receiver.getSoundReceiverId())
								.writeInt(receiver.getSoundX()).writeInt(receiver.getSoundY()).writeInt(receiver.getSoundZ())
								.writeShort((short) receiver.getSoundDistance()).writeByte(receiver.getSoundVolume());
					}


					Computronics.packet.sendTo(pkt, playerMP);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
