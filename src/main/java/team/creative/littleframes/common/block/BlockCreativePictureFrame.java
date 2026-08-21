package team.creative.littleframes.common.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.creator.BlockGuiCreator;
import team.creative.creativecore.common.gui.creator.GuiCreator;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.littleframes.LittleFrames;
import team.creative.littleframes.client.gui.GuiCreativePictureFrame;

public class BlockCreativePictureFrame extends BaseEntityBlock implements BlockGuiCreator {
    
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final float frameThickness = 0.031F;
    
    public static AlignedBox box(Direction direction) {
        Facing facing = Facing.get(direction);
        AlignedBox box = new AlignedBox();
        if (facing.positive)
            box.setMax(facing.axis, frameThickness);
        else
            box.setMin(facing.axis, 1 - frameThickness);
        return box;
    }
    
    public final boolean invisible;
    
    public BlockCreativePictureFrame(boolean invisible) {
        super(BlockBehaviour.Properties.of().explosionResistance(2.5F).destroyTime(2.0F).noOcclusion());
        this.invisible = invisible;
    }
    
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }
    
    @Override
    public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }
    
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }
    
    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return box(state.getValue(FACING)).voxelShape();
    }
    
    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return box(state.getValue(FACING)).voxelShape();
    }
    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && LittleFrames.CONFIG.canInteract(player, level))
            GuiCreator.BLOCK_OPENER.open(player, pos);
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return BECreativePictureFrame::tick;
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (invisible)
            return new BECreativePictureFrameInvisible(pos, state);
        return new BECreativePictureFrame(pos, state);
    }
    
    @Override
    public GuiLayer create(CompoundTag nbt, Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BECreativePictureFrame frame)
            return new GuiCreativePictureFrame(frame);
        return null;
    }
    
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block p_55669_, BlockPos otherPos, boolean p_55671_) {
        if (level.isClientSide)
            return;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BECreativePictureFrame frame && frame.playing == level.hasNeighborSignal(pos)) {
            if (frame.playing)
                frame.pause();
            else
                frame.play();
        }
    }
    
}
