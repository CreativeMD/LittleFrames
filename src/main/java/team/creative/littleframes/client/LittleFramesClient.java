package team.creative.littleframes.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import team.creative.creativecore.client.CreativeCoreClient;
import team.creative.littleframes.LittleFrames;
import team.creative.littleframes.LittleFramesRegistry;
import team.creative.littleframes.client.display.FrameVideoDisplay;

@OnlyIn(Dist.CLIENT)
public class LittleFramesClient {
    
    public static void load(IEventBus bus) {
        bus.addListener(LittleFramesClient::setup);
    }
    
    public static void setup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(LittleFramesClient.class);
        
        CreativeCoreClient.registerClientConfig(LittleFrames.MODID);
        BlockEntityRenderers.register(LittleFramesRegistry.BE_CREATIVE_FRAME.value(), x -> new CreativePictureFrameRenderer());
        BlockEntityRenderers.register(LittleFramesRegistry.BE_CREATIVE_FRAME_INVISIBLE.value(), x -> new CreativePictureFrameRenderer());
    }
    
    @SubscribeEvent
    public static void render(ClientTickEvent.Pre event) {
        FrameVideoDisplay.tick();
    }
    
    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            FrameVideoDisplay.unload();
        }
    }
    
}