package pl.asie.computronics.api.audio;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface IAudioReceiver extends IAudioConnection {

	@Nullable
	World getSoundWorld();

	BlockPos getSoundPos();

	int getSoundDistance();

	void receivePacket(AudioPacket packet, @Nullable EnumFacing side);
}
