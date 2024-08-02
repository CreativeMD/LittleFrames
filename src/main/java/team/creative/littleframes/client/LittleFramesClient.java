package team.creative.littleframes.client;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import team.creative.creativecore.client.CreativeCoreClient;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.CreativeBlockModel;
import team.creative.creativecore.client.render.model.CreativeItemBoxModel;
import team.creative.littleframes.LittleFrames;
import team.creative.littleframes.LittleFramesRegistry;
import team.creative.littleframes.client.display.FrameVideoDisplay;
import team.creative.littleframes.common.block.BECreativePictureFrame;
import team.creative.littleframes.common.block.BlockCreativePictureFrame;

@OnlyIn(Dist.CLIENT)
public class LittleFramesClient {
    
    public static void load(IEventBus bus) {
        bus.addListener(LittleFramesClient::setup);
    }
    
    public static void setup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(LittleFramesClient.class);
        
        CreativeCoreClient.registerClientConfig(LittleFrames.MODID);
        
        CreativeCoreClient.registerItemModel(ResourceLocation.tryBuild(LittleFrames.MODID, "creative_pic_frame"),
            new CreativeItemBoxModel(new ModelResourceLocation(ResourceLocation.withDefaultNamespace("stone"), "inventory")) {
                
                @Override
                public List<? extends RenderBox> getBoxes(ItemStack stack, boolean translucent) {
                    return Collections.singletonList(new RenderBox(0, 0, 0, BlockCreativePictureFrame.frameThickness, 1, 1, Blocks.OAK_PLANKS));
                }
            });
        
        CreativeCoreClient.registerBlockModel(ResourceLocation.tryBuild(LittleFrames.MODID, "creative_pic_frame"), new CreativeBlockModel() {
            
            public final ModelProperty<Boolean> visibility = new ModelProperty<>();
            public final ModelData visible = ModelData.builder().with(visibility, true).build();
            public final ModelData invisible = ModelData.builder().with(visibility, false).build();
            
            @Override
            public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof BECreativePictureFrame frame)
                    return frame.visibleFrame ? visible : invisible;
                return visible;
            }
            
            @Override
            public List<? extends RenderBox> getBoxes(BlockState state, ModelData data, RandomSource source) {
                if (data.has(visibility) && !data.get(visibility))
                    return Collections.EMPTY_LIST;
                RenderBox box = new RenderBox(BlockCreativePictureFrame.box(state.getValue(BlockCreativePictureFrame.FACING)), Blocks.OAK_PLANKS);
                return Collections.singletonList(box);
            }
        });
        
        BlockEntityRenderers.register(LittleFramesRegistry.BE_CREATIVE_FRAME.value(), x -> new CreativePictureFrameRenderer());
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