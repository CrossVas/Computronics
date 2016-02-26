package pl.asie.computronics.integration.railcraft.signalling;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.railcraft.api.core.WorldCoordinate;
import mods.railcraft.api.signals.SignalAspect;
import mods.railcraft.api.signals.SignalController;
import mods.railcraft.api.signals.SignalReceiver;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import pl.asie.computronics.util.collect.SimpleInvertibleDualMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Vexatos
 */
public class MassiveSignalReceiver extends SignalReceiver {

	private final Map<WorldCoordinate, SignalAspect> aspects = new HashMap<WorldCoordinate, SignalAspect>();
	private final SimpleInvertibleDualMap<String, WorldCoordinate> signalNames = SimpleInvertibleDualMap.create();

	public MassiveSignalReceiver(String locTag, TileEntity tile) {
		super(locTag, tile, 32);
	}

	public SignalAspect getAspectFor(WorldCoordinate coord) {
		return this.aspects.get(coord);
	}

	public void onControllerAspectChange(SignalController con, SignalAspect aspect) {
		WorldCoordinate coords = con.getCoords();
		SignalAspect oldAspect = this.aspects.get(coords);
		if(oldAspect != aspect) {
			this.aspects.put(coords, aspect);
			super.onControllerAspectChange(con, aspect);
		}
		if(!signalNames.containsEntry(con.getName(), coords)) {
			signalNames.put(con.getName(), coords);
		}
	}

	@Override
	public void cleanPairings() {
		super.cleanPairings();
		Collection<WorldCoordinate> pairs = getPairs();
		this.aspects.keySet().retainAll(pairs);
		this.signalNames.retainAllValues(pairs);
	}

	@Override
	public void clearPairings() {
		super.clearPairings();
		this.aspects.clear();
		this.signalNames.clear();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void removePair(int x, int y, int z) {
		super.removePair(x, y, z);
		Collection<WorldCoordinate> pairs = getPairs();
		this.aspects.keySet().retainAll(pairs);
		this.signalNames.retainAllValues(pairs);
	}

	protected void saveNBT(NBTTagCompound data) {
		super.saveNBT(data);
		NBTTagList list = new NBTTagList();

		for(Map.Entry<WorldCoordinate, SignalAspect> entry : this.aspects.entrySet()) {
			NBTTagCompound tag = new NBTTagCompound();
			WorldCoordinate key = entry.getKey();
			tag.setIntArray("coords", new int[] { key.dimension, key.x, key.y, key.z });
			tag.setByte("aspect", (byte) entry.getValue().ordinal());
			String s = signalNames.inverse().get(key);
			if(s != null) {
				tag.setString("name", s);
			}
			list.appendTag(tag);
		}
		data.setTag("aspects", list);
	}

	protected void loadNBT(NBTTagCompound data) {
		super.loadNBT(data);
		NBTTagList list = data.getTagList("aspects", Constants.NBT.TAG_COMPOUND);

		for(byte entry = 0; entry < list.tagCount(); ++entry) {
			NBTTagCompound tag = list.getCompoundTagAt(entry);
			int[] c = tag.getIntArray("coords");
			WorldCoordinate coord = new WorldCoordinate(c[0], c[1], c[2], c[3]);
			this.aspects.put(coord, SignalAspect.fromOrdinal(data.getByte("aspect")));
			if(tag.hasKey("name")) {
				signalNames.put(tag.getString("name"), coord);
			}
		}
	}
}