package team.creative.littleframes.common.structure;

import com.creativemd.creativecore.common.utils.mc.InventoryUtils;
import com.creativemd.littletiles.client.gui.handler.LittleStructureGuiHandler;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructurePremade;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LittleFrameBuilder extends LittleStructurePremade {
    
    public InventoryBasic inventory = new InventoryBasic("recipe", false, 1);
    public int lastSizeX = 16;
    public int lastSizeY = 16;
    public int lastThickness = 1;
    public IBlockState lastBlockState;
    
    public LittleFrameBuilder(LittleStructureType type, IStructureTileList mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    public boolean onBlockActivated(World worldIn, LittleTile tile, BlockPos pos, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) throws LittleActionException {
        LittleStructureGuiHandler.openGui("frame_builder", new NBTTagCompound(), playerIn, this);
        return true;
    }
    
    @Override
    protected void loadFromNBTExtra(NBTTagCompound nbt) {
        if (nbt.hasKey("sizeX"))
            lastSizeX = nbt.getInteger("sizeX");
        else
            lastSizeX = 16;
        if (nbt.hasKey("sizeY"))
            lastSizeY = nbt.getInteger("sizeY");
        else
            lastSizeY = 16;
        if (nbt.hasKey("thickness"))
            lastThickness = nbt.getInteger("thickness");
        else
            lastThickness = 1;
        if (nbt.hasKey("block")) {
            String[] parts = nbt.getString("block").split(":");
            Block block = Block.getBlockFromName(parts[0] + ":" + parts[1]);
            int meta;
            if (parts.length == 3)
                meta = Integer.parseInt(parts[2]);
            else
                meta = 0;
            lastBlockState = block.getStateFromMeta(meta);
        } else
            lastBlockState = Blocks.PLANKS.getDefaultState();
        inventory = InventoryUtils.loadInventoryBasic(nbt, 1);
    }
    
    @Override
    protected void writeToNBTExtra(NBTTagCompound nbt) {
        nbt.setInteger("sizeX", lastSizeX);
        nbt.setInteger("sizeY", lastSizeY);
        nbt.setInteger("thickness", lastThickness);
        InventoryUtils.saveInventoryBasic(inventory);
        Block block = lastBlockState.getBlock();
        int meta = block.getMetaFromState(lastBlockState);
        nbt.setString("block", block.getRegistryName().toString() + (meta != 0 ? ":" + meta : ""));
    }
    
}
