package team.creative.littleframes;

import com.creativemd.creativecore.common.config.holder.CreativeConfigRegistry;
import com.creativemd.creativecore.common.packet.CreativeCorePacket;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
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
import team.creative.littleframes.block.BlockCreativeFrame;
import team.creative.littleframes.block.TileEntityCreativeFrame;
import team.creative.littleframes.client.LittleFramesClient;
import team.creative.littleframes.packet.CreativeFramePacket;

@Mod(modid = LittleFrames.modid, version = LittleFrames.version, name = "LittleFrames", acceptedMinecraftVersions = "", dependencies = "required-after:creativecore", guiFactory = "team.creative.littleframes.LittleFramesSettings")
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
		
		GameRegistry.registerTileEntity(TileEntityCreativeFrame.class, new ResourceLocation(modid, "CreativeFrame"));
	}
}
