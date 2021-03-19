package electrodynamics.common.tile;

import electrodynamics.DeferredRegisters;
import electrodynamics.api.electricity.CapabilityElectrodynamic;
import electrodynamics.api.tile.GenericTileTicking;
import electrodynamics.api.tile.components.ComponentType;
import electrodynamics.api.tile.components.type.ComponentContainerProvider;
import electrodynamics.api.tile.components.type.ComponentDirection;
import electrodynamics.api.tile.components.type.ComponentElectrodynamic;
import electrodynamics.api.tile.components.type.ComponentInventory;
import electrodynamics.api.tile.components.type.ComponentPacketHandler;
import electrodynamics.api.tile.components.type.ComponentProcessor;
import electrodynamics.api.tile.components.type.ComponentProcessorType;
import electrodynamics.api.tile.components.type.ComponentTickable;
import electrodynamics.client.particle.GrindedParticle;
import electrodynamics.common.inventory.container.ContainerO2OProcessor;
import electrodynamics.common.item.ItemProcessorUpgrade;
import electrodynamics.common.recipe.MachineRecipes;
import electrodynamics.common.settings.Constants;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;

public class TileMineralCrusher extends GenericTileTicking {
    public long clientRunningTicks = 0;

    public TileMineralCrusher() {
	super(DeferredRegisters.TILE_MINERALCRUSHER.get());
	addComponent(new ComponentDirection());
	addComponent(new ComponentPacketHandler());
	addComponent(new ComponentTickable().addTickClient(this::tickClient));
	addComponent(new ComponentElectrodynamic(this).addRelativeInputDirection(Direction.NORTH)
		.setVoltage(CapabilityElectrodynamic.DEFAULT_VOLTAGE * 2));
	addComponent(new ComponentInventory().setInventorySize(5).addSlotsOnFace(Direction.UP, 0).addSlotsOnFace(Direction.DOWN, 1)
		.addRelativeSlotsOnFace(Direction.EAST, 0).addRelativeSlotsOnFace(Direction.WEST, 1)
		.setItemValidPredicate((slot, stack) -> slot == 0 || slot != 1 && stack.getItem() instanceof ItemProcessorUpgrade)
		.shouldSendInfo(this));
	addComponent(new ComponentContainerProvider("container.mineralcrusher").setCreateMenuFunction(
		(id, player) -> new ContainerO2OProcessor(id, player, getComponent(ComponentType.Inventory), getCoordsArray())));
	addComponent(new ComponentProcessor(this).addUpgradeSlots(2, 3, 4).setCanProcess(component -> MachineRecipes.canProcess(this))
		.setProcess(component -> MachineRecipes.process(this)).setRequiredTicks(Constants.MINERALCRUSHER_REQUIRED_TICKS)
		.setJoulesPerTick(Constants.MINERALCRUSHER_USAGE_PER_TICK).setType(ComponentProcessorType.ObjectToObject));
    }

    protected void tickClient(ComponentTickable tickable) {
	ComponentProcessor processor = getComponent(ComponentType.Processor);
	if (processor.operatingTicks > 0) {
	    Direction direction = this.<ComponentDirection>getComponent(ComponentType.Direction).getDirection();
	    if (world.rand.nextDouble() < 0.15) {
		double d4 = world.rand.nextDouble();
		double d5 = direction.getAxis() == Direction.Axis.X ? direction.getXOffset() * (direction.getXOffset() == -1 ? 0 : 1) : d4;
		double d6 = world.rand.nextDouble();
		double d7 = direction.getAxis() == Direction.Axis.Z ? direction.getZOffset() * (direction.getZOffset() == -1 ? 0 : 1) : d4;
		world.addParticle(ParticleTypes.SMOKE, pos.getX() + d5, pos.getY() + d6, pos.getZ() + d7, 0.0D, 0.0D, 0.0D);
	    }
	    double progress = Math.sin(0.05 * Math.PI * (clientRunningTicks % 20));
	    if (progress == 1) {
		Minecraft.getInstance().getSoundHandler()
			.play(new SimpleSound(DeferredRegisters.SOUND_MINERALCRUSHER.get(), SoundCategory.BLOCKS, 5, .75f, pos));
	    } else if (progress < 0.3) {
		for (int i = 0; i < 5; i++) {
		    double d4 = world.rand.nextDouble() * 4.0 / 16.0 + 0.5 - 2.0 / 16.0;
		    double d6 = world.rand.nextDouble() * 4.0 / 16.0 + 0.5 - 2.0 / 16.0;
		    world.addParticle(ParticleTypes.SMOKE, pos.getX() + d4 + direction.getXOffset() * 0.2, pos.getY() + 0.4,
			    pos.getZ() + d6 + direction.getZOffset() * 0.2, 0.0D, 0.0D, 0.0D);
		}
		ItemStack stack = processor.getInput();
		if (stack.getItem() instanceof BlockItem) {
		    BlockItem it = (BlockItem) stack.getItem();
		    Block block = it.getBlock();
		    for (int i = 0; i < 5; i++) {
			double d4 = world.rand.nextDouble() * 4.0 / 16.0 + 0.5 - 2.0 / 16.0;
			double d6 = world.rand.nextDouble() * 4.0 / 16.0 + 0.5 - 2.0 / 16.0;
			Minecraft.getInstance().particles
				.addEffect(new GrindedParticle((ClientWorld) world, pos.getX() + d4 + direction.getXOffset() * 0.2, pos.getY() + 0.4,
					pos.getZ() + d6 + direction.getZOffset() * 0.2, 0.0D, 0.0D, 0.0D, block.getDefaultState()).setBlockPos(pos));
		    }
		}
	    }
	    clientRunningTicks++;
	}
    }
}
