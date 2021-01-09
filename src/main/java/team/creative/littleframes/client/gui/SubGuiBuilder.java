package team.creative.littleframes.client.gui;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCounter;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.custom.GuiStackSelectorAll;
import com.creativemd.creativecore.common.utils.mc.BlockUtils;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.client.gui.LittleSubGuiUtils;
import com.creativemd.littletiles.common.item.ItemLittleRecipeAdvanced;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import team.creative.littleframes.common.structure.LittleFrameBuilder;

public class SubGuiBuilder extends SubGui {
    
    public LittleFrameBuilder builder;
    
    public SubGuiBuilder(LittleFrameBuilder builder) {
        this.builder = builder;
    }
    
    @Override
    public void createControls() {
        controls.add(new GuiLabel(translate("gui.frame_builder.width"), 0, 1));
        controls.add(new GuiLabel(translate("gui.frame_builder.height"), 90, 1));
        controls.add(new GuiLabel(translate("gui.frame_builder.thickness"), 0, 21));
        controls.add(new GuiCounter("width", 53, 0, 30, builder.lastSizeX, 1, Integer.MAX_VALUE));
        controls.add(new GuiCounter("height", 140, 0, 30, builder.lastSizeY, 1, Integer.MAX_VALUE));
        controls.add(new GuiCounter("thickness", 53, 20, 30, builder.lastThickness, 1, Integer.MAX_VALUE));
        
        GuiStackSelectorAll selector = new GuiStackSelectorAll("preview", 0, 38, 112, getPlayer(), LittleSubGuiUtils.getCollector(getPlayer()), true);
        selector.setSelectedForce(new ItemStack(builder.lastBlockState.getBlock(), 1, builder.lastBlockState.getBlock().getMetaFromState(builder.lastBlockState)));
        controls.add(selector);
        controls.add(new GuiLabel("failed", translate("gui.frame_builder.failed"), 0, 61, ColorUtils.RED).setVisible(false));
        controls.add(new GuiButton(translate("gui.frame_builder.craft"), 110, 60) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                if ((getPlayer().isCreative() && builder.inventory.getStackInSlot(0).isEmpty()) || ItemLittleRecipeAdvanced.isRecipe(builder.inventory.getStackInSlot(0).getItem())) {
                    get("failed").visible = false;
                    NBTTagCompound nbt = new NBTTagCompound();
                    GuiCounter width = (GuiCounter) get("width");
                    nbt.setInteger("width", width.getValue());
                    GuiCounter height = (GuiCounter) get("height");
                    nbt.setInteger("height", height.getValue());
                    GuiCounter thickness = (GuiCounter) get("thickness");
                    nbt.setInteger("thickness", thickness.getValue());
                    ItemStack stack = selector.getSelected();
                    IBlockState state = BlockUtils.getState(stack);
                    Block block = state.getBlock();
                    int meta = block.getMetaFromState(state);
                    nbt.setString("block", block.getRegistryName().toString() + (meta != 0 ? ":" + meta : ""));
                    sendPacketToServer(nbt);
                } else
                    get("failed").visible = true;
            }
        });
    }
    
}
