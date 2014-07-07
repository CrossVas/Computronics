package li.cil.oc.example.item;

import li.cil.oc.api.Network;
import li.cil.oc.api.driver.Slot;
import li.cil.oc.api.network.*;
import li.cil.oc.api.prefab.DriverItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.util.Random;

public class DriverCardFX extends DriverItem {
    
    // We want our item to be a card component, i.e. it can be placed into
    // computers' card slots.

    @Override
    public Slot slot(ItemStack stack) {
        return Slot.Card;
    }

    @Override
    public ManagedEnvironment createEnvironment(ItemStack stack, TileEntity container) {
        return new Environment(container);
    }

    public class Environment extends li.cil.oc.api.prefab.ManagedEnvironment {
        protected final TileEntity container;

        public Environment(TileEntity container) {
            this.container = container;
            node = Network.newNode(this, Visibility.Neighbors).
                    withComponent("particle").
                    create();
        }

        // We allow spawning particle effects. The parameters are the particle
        // name, the position relative to the block the card is in to spawn
        // the particle at, as well as - optionally - the initial velocity.

        @Callback(direct = true, limit = 16)
        public Object[] spawn(Context context, Arguments args) {
            String name = args.checkString(0);

            if (name.length() > Short.MAX_VALUE) {
                return new Object[]{false, "name too long"};
            }

            Random rng = container.getWorldObj().rand;
            double x = container.xCoord + 0.5 + args.checkDouble(1);
            double y = container.yCoord + 0.5 + args.checkDouble(2);
            double z = container.zCoord + 0.5 + args.checkDouble(3);
            double velocity = args.count() > 4 ? args.checkDouble(4) : (container.getWorldObj().rand.nextDouble() * 0.1);
            ModExampleItem.sendParticlePacket(name, container.getWorldObj().provider.dimensionId, x, y, z, velocity * rng.nextGaussian(), velocity * rng.nextGaussian(), velocity * rng.nextGaussian());
            return new Object[]{true};
        }
    }
}