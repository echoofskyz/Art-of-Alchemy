package com.cumulusmc.artofalchemy.block;

import com.cumulusmc.artofalchemy.blockentity.BlockEntityDissolver;
import com.cumulusmc.artofalchemy.item.AoAItems;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BlockDissolver extends BlockWithEntity {

	public static final BooleanProperty FILLED = BooleanProperty.of("filled");
	public static final BooleanProperty LIT = Properties.LIT;
	public static final Settings SETTINGS = Settings
		.of(Material.STONE)
		.strength(5.0f, 6.0f)
		.luminance((state) -> state.get(LIT) ? 15 : 0)
		.nonOpaque();

	public static Identifier getId() {
		return Registry.BLOCK.getId(AoABlocks.DISSOLVER);
	}

	public BlockDissolver() {
		this(SETTINGS);
	}

	protected BlockDissolver(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(FILLED, false).with(LIT, false));
	}

	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		builder.add(FILLED).add(LIT);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return super.getPlacementState(ctx);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
			BlockHitResult hit) {

		ItemStack inHand = player.getStackInHand(hand);

		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof BlockEntityDissolver) {
			BlockEntityDissolver dissolver = (BlockEntityDissolver) blockEntity;
			if (inHand.getItem() == AoAItems.ALKAHEST_BUCKET && dissolver.addAlkahest(1000)) {
				if (!player.abilities.creativeMode) {
					player.setStackInHand(hand, new ItemStack(Items.BUCKET));
				}
				world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY,
						SoundCategory.BLOCKS, 1.0F, 1.0F);
				return ActionResult.SUCCESS;
			} else if (inHand.getItem() == AoAItems.ESSENTIA_VESSEL) {
				ItemUsageContext itemContext = new ItemUsageContext(player, hand, hit);
				ActionResult itemResult = inHand.useOnBlock(itemContext);
				if (itemResult != ActionResult.PASS) {
					return itemResult;
				}
			}
			if (!world.isClient()) {
				player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
			}
			return ActionResult.SUCCESS;
		} else {
			return ActionResult.PASS;
		}

	}

	@Override
	public BlockEntity createBlockEntity(BlockView world) {
		return new BlockEntityDissolver();
	}

	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (state.getBlock() != newState.getBlock()) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof BlockEntityDissolver) {
				ItemScatterer.spawn(world, pos, (Inventory) blockEntity);
			}

			super.onStateReplaced(state, world, pos, newState, moved);
		}
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public boolean hasComparatorOutput(BlockState state) {
		return true;
	}

	@Override
	public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof BlockEntityDissolver) {
			int capacity = ((BlockEntityDissolver) be).getTankSize();
			int filled = ((BlockEntityDissolver) be).getAlkahest();
			double fillLevel = (double) filled / capacity;
			if (fillLevel == 0.0) {
				return 0;
			} else {
				return 1 + (int) (fillLevel * 14);
			}
		} else {
			return 0;
		}
	}

}
