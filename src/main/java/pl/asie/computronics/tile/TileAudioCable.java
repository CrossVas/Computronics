package pl.asie.computronics.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.computronics.api.audio.AudioPacket;
import pl.asie.computronics.api.audio.IAudioConnection;
import pl.asie.computronics.api.audio.IAudioReceiver;
import pl.asie.computronics.integration.charset.audio.IntegrationCharsetAudio;
import pl.asie.computronics.reference.Mods;
import pl.asie.lib.tile.TileEntityBase;
import pl.asie.lib.util.ColorUtils;
import pl.asie.lib.util.internal.IColorable;

import javax.annotation.Nullable;
import java.util.HashSet;

public class TileAudioCable extends TileEntityBase implements IAudioReceiver, IColorable, ITickable {

	private final HashSet<Object> packetIds = new HashSet<Object>();

	private int ImmibisMicroblocks_TransformableTileEntityMarker;

	private byte connectionMap = 0;
	private boolean initialConnect = false;

	private void updateConnections() {
		final byte oldConnections = connectionMap;
		connectionMap = 0;
		for(EnumFacing dir : EnumFacing.VALUES) {
			if(!connectsInternal(dir)) {
				continue;
			}

			if(worldObj.isBlockLoaded(getPos().offset(dir))) {
				TileEntity tile = worldObj.getTileEntity(getPos().offset(dir));
				if(tile instanceof TileAudioCable) {
					if(!((TileAudioCable) tile).connectsInternal(dir.getOpposite())) {
						continue;
					}
				} else if(tile instanceof IAudioConnection) {
					if(!((IAudioConnection) tile).connectsAudio(dir.getOpposite())) {
						continue;
					}
				} else if(Mods.API.hasAPI(Mods.API.CharsetAudio)) {
					if(!IntegrationCharsetAudio.connects(tile, dir.getOpposite())) {
						continue;
					}
				} else {
					continue;
				}

				if(tile instanceof IColorable && ((IColorable) tile).canBeColored()
					&& !ColorUtils.isSameOrDefault(this, (IColorable) tile)) {
					continue;
				}

				connectionMap |= 1 << dir.ordinal();
			}
		}
		if(connectionMap != oldConnections) {
			worldObj.markBlockRangeForRenderUpdate(getPos(), getPos());
		}
	}

	protected boolean connectsInternal(EnumFacing dir) {
		return ImmibisMicroblocks_isSideOpen(dir.ordinal());
	}

	@Override
	public boolean connectsAudio(EnumFacing dir) {
		if(!initialConnect) {
			updateConnections();
			initialConnect = true;
		}
		return ((connectionMap >> dir.ordinal()) & 1) == 1;
	}

	@Override
	public void update() {
		packetIds.clear();
		if(worldObj != null) {
			updateConnections();
		}
	}

	public boolean receivePacketID(Object o) {
		if(packetIds.contains(o)) {
			return false;
		}

		packetIds.add(o);
		return true;
	}

	@Override
	public void receivePacket(AudioPacket packet, @Nullable EnumFacing side) {
		if(!receivePacketID(packet.id)) {
			return;
		}

		for(EnumFacing dir : EnumFacing.VALUES) {
			if(dir == side || !connectsAudio(dir)) {
				continue;
			}

			BlockPos pos = getPos().offset(dir);
			if(!worldObj.isBlockLoaded(pos)) {
				continue;
			}

			TileEntity tile = worldObj.getTileEntity(pos);
			if(tile instanceof IAudioReceiver) {
				((IAudioReceiver) tile).receivePacket(packet, dir.getOpposite());
			}
		}
	}

	@Override
	public World getSoundWorld() {
		return null;
	}

	@Override
	public BlockPos getSoundPos() {
		return BlockPos.ORIGIN;
	}

	@Override
	public int getSoundDistance() {
		return 0;
	}

	protected int overlayColor = getDefaultColor();

	@Override
	public boolean canBeColored() {
		return true;
	}

	@Override
	public int getColor() {
		return overlayColor;
	}

	@Override
	public int getDefaultColor() {
		return ColorUtils.Color.LightGray.color;
	}

	@Override
	public void setColor(int color) {
		this.overlayColor = color;
		this.markDirty();
	}

	@Override
	public void readFromRemoteNBT(NBTTagCompound nbt) {
		super.readFromRemoteNBT(nbt);
		int oldColor = this.overlayColor;
		byte oldConnections = this.connectionMap;
		if(nbt.hasKey("col")) {
			overlayColor = nbt.getInteger("col");
		}
		if(this.overlayColor < 0) {
			this.overlayColor = getDefaultColor();
		}
		if(nbt.hasKey("con")) {
			this.connectionMap = nbt.getByte("con");
		}
		if(oldColor != this.overlayColor || oldConnections != this.connectionMap) {
			this.worldObj.markBlockRangeForRenderUpdate(getPos(), getPos());
		}
	}

	@Override
	public NBTTagCompound writeToRemoteNBT(NBTTagCompound nbt) {
		super.writeToRemoteNBT(nbt);
		if(overlayColor != getDefaultColor()) {
			nbt.setInteger("col", overlayColor);
		}
		nbt.setByte("con", connectionMap);
		return nbt;
	}

	@Override
	public void readFromNBT(final NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if(nbt.hasKey("col")) {
			overlayColor = nbt.getInteger("col");
		}
		if(this.overlayColor < 0) {
			this.overlayColor = getDefaultColor();
		}
		if(nbt.hasKey("con")) {
			this.connectionMap = nbt.getByte("con");
		}
	}

	@Override
	public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if(overlayColor != getDefaultColor()) {
			nbt.setInteger("col", overlayColor);
		}
		nbt.setByte("con", connectionMap);
		return nbt;
	}

	public boolean ImmibisMicroblocks_isSideOpen(int side) {
		return true;
	}

	public void ImmibisMicroblocks_onMicroblocksChanged() {

	}
}
