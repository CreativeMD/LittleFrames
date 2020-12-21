package team.creative.littleframes.client;

import com.creativemd.creativecore.client.rendering.model.CreativeBlockRenderHelper;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.littleframes.LittleFrames;
import team.creative.littleframes.block.TileEntityCreativeFrame;
import team.creative.littleframes.client.texture.TextureCache;

@SideOnly(Side.CLIENT)
public class LittleFramesClient {
	
	public static void initClient() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCreativeFrame.class, new CreativeFrameTileRenderer());
		
		CreativeBlockRenderHelper.registerCreativeRenderedBlock(LittleFrames.frame);
		
		MinecraftForge.EVENT_BUS.register(TextureCache.class);
	}
}
