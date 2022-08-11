package team.creative.littleframes;

import java.util.function.Supplier;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import team.creative.littleframes.common.block.BECreativeFrame;
import team.creative.littleframes.common.block.BlockCreativeFrame;
import team.creative.littletiles.LittleTiles;

public class LittleFramesRegistry {
    
    // ITEMS
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LittleFrames.MODID);
    
    // BLOCKS
    
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LittleFrames.MODID);
    
    public static final RegistryObject<Block> CREATIVE_FRAME = register("creative_frame", () -> new BlockCreativeFrame());
    
    private static <T extends Block> RegistryObject<T> register(String name, Supplier<? extends T> sup) {
        RegistryObject<T> ret = BLOCKS.register(name, sup);
        ITEMS.register(name, () -> new BlockItem(ret.get(), new Item.Properties().tab(LittleTiles.LITTLE_TAB)));
        return ret;
    }
    
    // BLOCK_ENTITY
    
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, LittleFrames.MODID);
    
    public static final RegistryObject<BlockEntityType<BECreativeFrame>> BE_CREATIVE_FRAME = BLOCK_ENTITIES
            .register("creative_frame", () -> BlockEntityType.Builder.of(BECreativeFrame::new, CREATIVE_FRAME.get()).build(null));
    
}
