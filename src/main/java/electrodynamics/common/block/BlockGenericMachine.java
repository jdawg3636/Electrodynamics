package electrodynamics.common.block;

import java.util.Arrays;
import java.util.List;

import electrodynamics.api.IWrenchItem;
import electrodynamics.api.capability.CapabilityUtils;
import electrodynamics.api.electricity.CapabilityElectrodynamic;
import electrodynamics.prefab.tile.GenericTile;
import electrodynamics.prefab.tile.IWrenchable;
import electrodynamics.prefab.tile.components.ComponentType;
import electrodynamics.prefab.tile.components.type.ComponentFluidHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext.Builder;
import net.minecraft.loot.LootParameters;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class BlockGenericMachine extends HorizontalBlock implements IWrenchable {
    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;

    public BlockGenericMachine() {
	super(Properties.create(Material.IRON).hardnessAndResistance(3.5F).sound(SoundType.METAL).harvestTool(ToolType.PICKAXE).notSolid());
	setDefaultState(stateContainer.getBaseState().with(FACING, Direction.NORTH));
    }

    @Deprecated
    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
	TileEntity tile = worldIn.getTileEntity(pos);
	if (!(state.getBlock() == newState.getBlock() && state.get(FACING) != newState.get(FACING)) && tile instanceof GenericTile) {
	    GenericTile generic = (GenericTile) tile;
	    if (generic.hasComponent(ComponentType.Inventory)) {
		InventoryHelper.dropInventoryItems(worldIn, pos, generic.getComponent(ComponentType.Inventory));
	    }
	}
	super.onReplaced(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
	return true;
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
	return 0;
    }

    @Override
    @Deprecated
    public float getAmbientOcclusionLightValue(BlockState state, IBlockReader worldIn, BlockPos pos) {
	return 1;
    }

    @Override
    @Deprecated
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
	    BlockRayTraceResult hit) {

	if (worldIn.isRemote) {
	    return ActionResultType.SUCCESS;
	}
	TileEntity tile = worldIn.getTileEntity(pos);
	ItemStack stack = player.getHeldItem(handIn);
	// allows you to right-click a canister on a machine and drain/fill it
	if (CapabilityUtils.hasFluidItemCap(stack)) {
	    if (tile instanceof GenericTile) {
		GenericTile generic = (GenericTile) tile;
		if (generic.hasComponent(ComponentType.FluidHandler)) {
		    ComponentFluidHandler tank = generic.getComponent(ComponentType.FluidHandler);
		    boolean isBucket = stack.getItem() instanceof BucketItem;
		    int tankroom = 0;

		    FluidStack containedFluid = CapabilityUtils.simDrain(stack, Integer.MAX_VALUE);
		    FluidTank inputFluidTank = tank.getTankFromFluid(containedFluid.getFluid(), true);
		    if (inputFluidTank != null) {
			tankroom = inputFluidTank.getCapacity() - inputFluidTank.getFluidAmount();
		    }
		    FluidStack amtTaken = CapabilityUtils.simDrain(stack, new FluidStack(containedFluid.getFluid(), tankroom));
		    if (!amtTaken.getFluid().isEquivalentTo(Fluids.EMPTY) && amtTaken.getAmount() > 0 && !isBucket) {
			CapabilityUtils.drain(stack, amtTaken);
			tank.addFluidToTank(amtTaken, true);
			return ActionResultType.FAIL;
		    } else if (!amtTaken.getFluid().isEquivalentTo(Fluids.EMPTY) && amtTaken.getAmount() >= 1000 && isBucket) {
			CapabilityUtils.drain(stack, new FluidStack(amtTaken.getFluid(), 1000));
			tank.addFluidToTank(new FluidStack(amtTaken.getFluid(), 1000), true);
			player.setHeldItem(handIn, new ItemStack(Items.BUCKET, 1));
			return ActionResultType.FAIL;
		    } else if (!isBucket) {
			if (!containedFluid.getFluid().isEquivalentTo(Fluids.EMPTY)) {
			    int amtAccepted = 0;
			    FluidTank outputFluidTank = tank.getTankFromFluid(containedFluid.getFluid(), false);
			    if (outputFluidTank != null) {
				amtAccepted = CapabilityUtils.simFill(stack, outputFluidTank.getFluid());
			    }
			    if (amtAccepted > 0) {
				CapabilityUtils.fill(stack, new FluidStack(containedFluid.getFluid(), amtAccepted));
				tank.drainFluidFromTank(new FluidStack(containedFluid.getFluid(), amtAccepted), false);
				return ActionResultType.FAIL;
			    }
			} else {
			    for (Fluid fluid : tank.getValidOutputFluids()) {
				FluidTank outputFluidTank = tank.getTankFromFluid(fluid, false);
				int amtAccepted = CapabilityUtils.simFill(stack, outputFluidTank.getFluid());
				if (amtAccepted > 0) {
				    CapabilityUtils.fill(stack, new FluidStack(fluid, amtAccepted));
				    tank.drainFluidFromTank(new FluidStack(fluid, amtAccepted), false);
				    return ActionResultType.FAIL;
				}
			    }
			}
		    }
		    if (generic.hasComponent(ComponentType.ContainerProvider)) {
			player.openContainer(generic.getComponent(ComponentType.ContainerProvider));
		    }
		}
	    }
	    player.addStat(Stats.INTERACT_WITH_FURNACE);
	    return ActionResultType.CONSUME;
	} else if (!(stack.getItem() instanceof IWrenchItem)) {
	    if (tile instanceof GenericTile) {
		GenericTile generic = (GenericTile) tile;
		if (generic.hasComponent(ComponentType.ContainerProvider)) {
		    player.openContainer(generic.getComponent(ComponentType.ContainerProvider));
		}
	    }
	    player.addStat(Stats.INTERACT_WITH_FURNACE);
	    return ActionResultType.CONSUME;
	}
	return ActionResultType.FAIL;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
	return getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
	builder.add(FACING);
    }

    @Override
    @Deprecated
    public List<ItemStack> getDrops(BlockState state, Builder builder) {
	ItemStack stack = new ItemStack(this);
	TileEntity tile = builder.get(LootParameters.BLOCK_ENTITY);
	tile.getCapability(CapabilityElectrodynamic.ELECTRODYNAMIC).ifPresent(el -> {
	    double joules = el.getJoulesStored();
	    if (joules > 0) {
		stack.getOrCreateTag().putDouble("joules", joules);
	    }
	});
	return Arrays.asList(stack);
    }

    @Override
    @Deprecated
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
	super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	TileEntity tile = worldIn.getTileEntity(pos);
	if (stack.hasTag()) {
	    tile.getCapability(CapabilityElectrodynamic.ELECTRODYNAMIC)
		    .ifPresent(el -> el.setJoulesStored(stack.getOrCreateTag().getDouble("joules")));
	}
    }

    @Deprecated
    @Override
    public void onRotate(ItemStack stack, BlockPos pos, PlayerEntity player) {
	player.world.setBlockState(pos, rotate(player.world.getBlockState(pos), Rotation.CLOCKWISE_90));
    }

    @Override
    public void onPickup(ItemStack stack, BlockPos pos, PlayerEntity player) {
	World world = player.world;
	TileEntity tile = world.getTileEntity(pos);
	if (tile instanceof GenericTile) {
	    GenericTile generic = (GenericTile) tile;
	    if (generic.hasComponent(ComponentType.Inventory)) {
		InventoryHelper.dropInventoryItems(world, pos, generic.getComponent(ComponentType.Inventory));
	    }
	}
	world.destroyBlock(pos, true, player);
    }
}
