package team.creative.littleframes.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.littleframes.LittleFramesRegistry;

public class BECreativePictureFrameInvisible extends BECreativePictureFrame {
    
    public BECreativePictureFrameInvisible(BlockPos pos, BlockState state) {
        super(LittleFramesRegistry.BE_CREATIVE_FRAME_INVISIBLE.get(), pos, state);
    }
    
    @Override
    public boolean isVisible() {
        return false;
    }
    
}