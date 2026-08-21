package team.creative.littleframes;

import java.util.function.Supplier;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import team.creative.littleframes.common.block.BECreativePictureFrame;
import team.creative.littleframes.common.block.BECreativePictureFrameInvisible;
import team.creative.littleframes.common.block.BlockCreativePictureFrame;

public class LittleFramesRegistry {
    
    // ITEMS
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, LittleFrames.MODID);
    
    // BLOCKS
    
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, LittleFrames.MODID);
    
    public static final Holder<Block> CREATIVE_PICTURE_FRAME = register("creative_pic_frame", () -> new BlockCreativePictureFrame(false));
    public static final Holder<Block> CREATIVE_PICTURE_FRAME_INVISIBLE = register("creative_pic_frame_invisible", () -> new BlockCreativePictureFrame(true));
    
    private static <T extends Block> DeferredHolder<Block, ? extends T> register(String name, Supplier<? extends T> sup) {
        var ret = BLOCKS.register(name, sup);
        ITEMS.register(name, () -> new BlockItem(ret.value(), new Item.Properties()));
        return ret;
    }
    
    // BLOCK_ENTITY
    
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LittleFrames.MODID);
    
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BECreativePictureFrame>> BE_CREATIVE_FRAME = registerBlockEntity("creative_pic_frame",
        () -> BlockEntityType.Builder.<BECreativePictureFrame>of(BECreativePictureFrame::new, CREATIVE_PICTURE_FRAME.value()));
    
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BECreativePictureFrameInvisible>> BE_CREATIVE_FRAME_INVISIBLE = registerBlockEntity(
        "creative_pic_frame_invisible", () -> BlockEntityType.Builder.<BECreativePictureFrameInvisible>of(BECreativePictureFrameInvisible::new, CREATIVE_PICTURE_FRAME_INVISIBLE
                .value()));
    
    public static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> registerBlockEntity(String name, Supplier<BlockEntityType.Builder<T>> sup) {
        return BLOCK_ENTITIES.register(name, () -> sup.get().build(Util.fetchChoiceType(References.BLOCK_ENTITY, name)));
    }
    
}
