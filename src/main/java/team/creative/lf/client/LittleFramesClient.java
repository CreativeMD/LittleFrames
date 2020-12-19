package team.creative.lf.client;

import com.creativemd.creativecore.client.rendering.model.CreativeBlockRenderHelper;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.lf.LittleFrames;
import team.creative.lf.block.TileEntityCreativeFrame;
import team.creative.lf.client.texture.TextureCache;

@SideOnly(Side.CLIENT)
public class LittleFramesClient {
	
	public static void initClient() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCreativeFrame.class, new CreativeFrameTileRenderer());
		
		CreativeBlockRenderHelper.registerCreativeRenderedBlock(LittleFrames.frame);
		
		MinecraftForge.EVENT_BUS.register(TextureCache.class);
	}
}
