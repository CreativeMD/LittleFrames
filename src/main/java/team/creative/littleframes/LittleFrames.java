package team.creative.littleframes;

import com.creativemd.creativecore.common.config.holder.CreativeConfigRegistry;
import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.opener.GuiHandler;
import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.client.gui.handler.LittleStructureGuiHandler;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructurePremade;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.littleframes.client.LittleFramesClient;
import team.creative.littleframes.client.gui.SubGuiBuilder;
import team.creative.littleframes.client.gui.SubGuiLittleFrame;
import team.creative.littleframes.common.block.BlockCreativeFrame;
import team.creative.littleframes.common.block.TileEntityCreativeFrame;
import team.creative.littleframes.common.container.SubContainerBuilder;
import team.creative.littleframes.common.container.SubContainerLittleFrame;
import team.creative.littleframes.common.packet.CreativeFramePacket;
import team.creative.littleframes.common.packet.LittleFramePacket;
import team.creative.littleframes.common.structure.LittleFrame;
import team.creative.littleframes.common.structure.LittleFrameBuilder;

@Mod(modid = LittleFrames.modid, version = LittleFrames.version, name = "LittleFrames", acceptedMinecraftVersions = "", dependencies = "required-after:creativecore",
    guiFactory = "team.creative.littleframes.LittleFramesSettings")
@Mod.EventBusSubscriber
public class LittleFrames {
    
    public static final String modid = "littleframes";
    public static final String version = "1.0.0";
    
    public static Block frame = new BlockCreativeFrame().setUnlocalizedName("creative_frame").setRegistryName("creative_frame");
    
    public static LittleFramesConfig CONFIG;
    
    @SideOnly(Side.CLIENT)
    public static void initClient() {
        LittleFramesClient.initClient();
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        MinecraftForge.EVENT_BUS.register(LittleFrames.class);
    }
    
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(frame);
    }
    
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(new ItemBlock(frame).setRegistryName(frame.getRegistryName()));
        
        if (FMLCommonHandler.instance().getSide().isClient())
            initClient();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent evt) {
        CreativeConfigRegistry.ROOT.registerValue(modid, CONFIG = new LittleFramesConfig());
        
        CreativeCorePacket.registerPacket(CreativeFramePacket.class);
        CreativeCorePacket.registerPacket(LittleFramePacket.class);
        
        GameRegistry.registerTileEntity(TileEntityCreativeFrame.class, new ResourceLocation(modid, "CreativeFrame"));
        LittleStructurePremade.registerPremadeStructureType("frame_builder", modid, LittleFrameBuilder.class);
        LittleStructureRegistry.registerStructureType("little_frame", "decoration", LittleFrame.class, LittleStructureAttribute.TICK_RENDERING | LittleStructureAttribute.TICKING, null);
        
        GuiHandler.registerGuiHandler("frame_builder", new LittleStructureGuiHandler() {
            
            @Override
            @SideOnly(Side.CLIENT)
            public SubGui getGui(EntityPlayer player, NBTTagCompound nbt, LittleStructure structure) {
                if (structure instanceof LittleFrameBuilder)
                    return new SubGuiBuilder((LittleFrameBuilder) structure);
                return null;
            }
            
            @Override
            public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt, LittleStructure structure) {
                if (structure instanceof LittleFrameBuilder)
                    return new SubContainerBuilder(player, (LittleFrameBuilder) structure);
                return null;
            }
        });
        GuiHandler.registerGuiHandler("little_frame", new LittleStructureGuiHandler() {
            
            @Override
            @SideOnly(Side.CLIENT)
            public SubGui getGui(EntityPlayer player, NBTTagCompound nbt, LittleStructure structure) {
                if (structure instanceof LittleFrame)
                    return new SubGuiLittleFrame((LittleFrame) structure);
                return null;
            }
            
            @Override
            public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt, LittleStructure structure) {
                if (structure instanceof LittleFrame)
                    return new SubContainerLittleFrame(player, (LittleFrame) structure);
                return null;
            }
        });
    }
}
