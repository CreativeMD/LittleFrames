package team.creative.littleframes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.opener.GuiHandler;
import com.creativemd.littletiles.client.gui.handler.LittleStructureGuiHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.creativecore.common.config.holder.CreativeConfigRegistry;
import team.creative.creativecore.common.network.CreativeNetwork;
import team.creative.littleframes.client.LittleFramesClient;
import team.creative.littleframes.client.gui.SubGuiLittleFrame;
import team.creative.littleframes.common.container.SubContainerLittleFrame;
import team.creative.littleframes.common.packet.CreativeFramePacket;
import team.creative.littleframes.common.packet.LittleFramePacket;
import team.creative.littleframes.common.structure.LittleFrame;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.attribute.LittleStructureAttribute;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.type.premade.LittleStructureBuilder;
import team.creative.littletiles.common.structure.type.premade.LittleStructureBuilder.LittleStructureBuilderType;

@Mod(LittleFrames.MODID)
public class LittleFrames {
    
    public static final String MODID = "littleframes";
    
    public static LittleFramesConfig CONFIG;
    public static final Logger LOGGER = LogManager.getLogger(LittleFrames.MODID);
    public static final CreativeNetwork NETWORK = new CreativeNetwork("1.0", LOGGER, new ResourceLocation(LittleFrames.MODID, "main"));
    
    public LittleFrames() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> LittleFramesClient.load(FMLJavaModLoadingContext.get().getModEventBus()));
    }
    
    private void init(final FMLCommonSetupEvent event) {
        CreativeConfigRegistry.ROOT.registerValue(MODID, CONFIG = new LittleFramesConfig());
        
        NETWORK.registerType(CreativeFramePacket.class, CreativeFramePacket::new);
        NETWORK.registerType(LittleFramePacket.class, LittleFramePacket::new);
        
        LittleStructureBuilder.register(new LittleStructureBuilderType(LittleStructureRegistry
                .registerStructureType("little_frame", "decoration", LittleFrame.class, LittleStructureAttribute.TICK_RENDERING | LittleStructureAttribute.TICKING, null), "frame"));
        
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
