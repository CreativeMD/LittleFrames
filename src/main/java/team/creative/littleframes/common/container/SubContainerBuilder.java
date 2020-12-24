package team.creative.littleframes.common.container;

import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.item.ItemLittleRecipeAdvanced;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import team.creative.littleframes.common.structure.LittleFrame;
import team.creative.littleframes.common.structure.LittleFrameBuilder;

public class SubContainerBuilder extends SubContainer {
	
	public LittleFrameBuilder builder;
	
	public SubContainerBuilder(EntityPlayer player, LittleFrameBuilder builder) {
		super(player);
		this.builder = builder;
	}
	
	@Override
	public void createControls() {
		addSlotToContainer(new Slot(builder.inventory, 0, 152, 61));
		addPlayerSlotsToContainer(player);
	}
	
	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		if ((player.isCreative() && builder.inventory.getStackInSlot(0).isEmpty()) || ItemLittleRecipeAdvanced.isRecipe(builder.inventory.getStackInSlot(0).getItem())) {
			int width = nbt.getInteger("width");
			int height = nbt.getInteger("height");
			int thickness = nbt.getInteger("thickness");
			String[] parts = nbt.getString("block").split(":");
			Block block = Block.getBlockFromName(parts[0] + ":" + parts[1]);
			int meta;
			if (parts.length == 3)
				meta = Integer.parseInt(parts[2]);
			else
				meta = 0;
			IBlockState state = block.getStateFromMeta(meta);
			
			builder.lastBlockState = state;
			builder.lastSizeX = width;
			builder.lastSizeY = height;
			builder.lastThickness = thickness;
			builder.updateStructure();
			
			NBTTagCompound structureNBT = new NBTTagCompound();
			LittleStructureType type = LittleStructureRegistry.getStructureType(LittleFrame.class);
			LittleGridContext context = LittleGridContext.get();
			structureNBT.setString("id", type.id);
			structureNBT.setIntArray("topRight", new int[] { Float.floatToIntBits(0), Float.floatToIntBits(1), Float.floatToIntBits(1) });
			structureNBT.setIntArray("frame", new int[] { thickness, 0, 0, thickness + 1, height, width, context.size });
			LittlePreviews previews = new LittlePreviews(structureNBT, context);
			NBTTagCompound tileData = new NBTTagCompound();
			tileData.setString("block", nbt.getString("block"));
			for (int x = 0; x < thickness; x += context.size)
				for (int y = 0; y < height; y += context.size)
					for (int z = 0; z < width; z += context.size)
						previews.addWithoutCheckingPreview(new LittlePreview(new LittleBox(x, y, z, Math.min(x + 16, thickness), Math.min(y + 16, height), Math.min(z + 16, width)), tileData));
			ItemStack stack = builder.inventory.getStackInSlot(0);
			if (stack.isEmpty()) {
				stack = new ItemStack(LittleTiles.recipeAdvanced);
				builder.inventory.setInventorySlotContents(0, stack);
			}
			LittlePreview.savePreview(previews, stack);
		}
	}
	
}
