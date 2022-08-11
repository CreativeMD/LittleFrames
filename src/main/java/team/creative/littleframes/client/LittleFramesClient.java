package team.creative.littleframes.client;

import com.creativemd.creativecore.client.rendering.model.CreativeBlockRenderHelper;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import team.creative.littleframes.LittleFrames;
import team.creative.littleframes.client.texture.TextureCache;
import team.creative.littleframes.common.block.BECreativeFrame;

@OnlyIn(Dist.CLIENT)
public class LittleFramesClient {
    
    public static void load(IEventBus bus) {
        
    }
    
    public static void initClient() {
        ClientRegistry.bindTileEntitySpecialRenderer(BECreativeFrame.class, new CreativeFrameTileRenderer());
        
        CreativeBlockRenderHelper.registerCreativeRenderedBlock(LittleFrames.frame);
        
        MinecraftForge.EVENT_BUS.register(TextureCache.class);
    }
}
