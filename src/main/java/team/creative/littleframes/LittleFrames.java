package team.creative.littleframes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import team.creative.creativecore.common.config.holder.CreativeConfigRegistry;
import team.creative.creativecore.common.network.CreativeNetwork;
import team.creative.littleframes.client.LittleFramesClient;
import team.creative.littleframes.common.packet.CreativePictureFramePacket;
import team.creative.littleframes.common.packet.LittlePictureFramePacket;
import team.creative.littleframes.common.structure.LittlePictureFrame;
import team.creative.littletiles.common.structure.attribute.LittleAttributeBuilder;
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
        
        LittleFramesRegistry.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        LittleFramesRegistry.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        LittleFramesRegistry.BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerTabs);
    }
    
    private void init(final FMLCommonSetupEvent event) {
        CreativeConfigRegistry.ROOT.registerValue(MODID, CONFIG = new LittleFramesConfig());
        
        NETWORK.registerType(CreativePictureFramePacket.class, CreativePictureFramePacket::new);
        
        if (ModList.get().isLoaded("littletiles")) {
            NETWORK.registerType(LittlePictureFramePacket.class, LittlePictureFramePacket::new);
            LittleStructureBuilder.register(new LittleStructureBuilderType(LittleStructureRegistry.register("little_picture_frame", LittlePictureFrame.class,
                LittlePictureFrame::new, new LittleAttributeBuilder().tickRendering().ticking()), "frame"));
            LittleTilesIntegration.init();
        }
    }
    
    private void registerTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.BUILDING_BLOCKS))
            event.accept(LittleFramesRegistry.CREATIVE_PICTURE_FRAME.get());
    }
}
