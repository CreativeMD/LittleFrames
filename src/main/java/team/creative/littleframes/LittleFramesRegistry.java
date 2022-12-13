package team.creative.littleframes;

import java.util.function.Supplier;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import team.creative.littleframes.common.block.BECreativePictureFrame;
import team.creative.littleframes.common.block.BlockCreativePictureFrame;

public class LittleFramesRegistry {
    
    // ITEMS
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LittleFrames.MODID);
    
    // BLOCKS
    
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LittleFrames.MODID);
    
    public static final RegistryObject<Block> CREATIVE_PICTURE_FRAME = register("creative_pic_frame", () -> new BlockCreativePictureFrame());
    
    private static <T extends Block> RegistryObject<T> register(String name, Supplier<? extends T> sup) {
        RegistryObject<T> ret = BLOCKS.register(name, sup);
        ITEMS.register(name, () -> new BlockItem(ret.get(), new Item.Properties()));
        return ret;
    }
    
    // BLOCK_ENTITY
    
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, LittleFrames.MODID);
    
    public static final RegistryObject<BlockEntityType<BECreativePictureFrame>> BE_CREATIVE_FRAME = BLOCK_ENTITIES
            .register("creative_pic_frame", () -> BlockEntityType.Builder.of(BECreativePictureFrame::new, CREATIVE_PICTURE_FRAME.get()).build(null));
    
}
