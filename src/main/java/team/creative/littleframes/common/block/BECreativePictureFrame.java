package team.creative.littleframes.common.block;

import static team.creative.littleframes.LittleFrames.LOGGER;

import java.net.URI;

import org.watermedia.api.image.ImageAPI;
import org.watermedia.api.image.ImageCache;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.be.BlockEntityCreative;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.vec.Vec2f;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littleframes.LittleFrames;
import team.creative.littleframes.LittleFramesRegistry;
import team.creative.littleframes.client.display.FrameDisplay;
import team.creative.littleframes.client.display.FramePictureDisplay;
import team.creative.littleframes.client.display.FrameVideoDisplay;
import team.creative.littleframes.common.data.LittleFrameData;
import team.creative.littleframes.common.packet.CreativePictureFramePacket;

public class BECreativePictureFrame extends BlockEntityCreative {
    
    public Vec2f min = new Vec2f(0, 0);
    public Vec2f max = new Vec2f(1, 1);
    
    public float rotation = 0;
    public boolean flipX = false;
    public boolean flipY = false;
    
    public boolean bothSides = false;
    
    public boolean playing = true;
    
    public LittleFrameData data = new LittleFrameData();
    
    private boolean released = false;
    
    private boolean shouldConvert = false;
    
    @OnlyIn(Dist.CLIENT)
    public ImageCache cache;
    
    @OnlyIn(Dist.CLIENT)
    public FrameDisplay display;
    
    public BECreativePictureFrame(BlockPos pos, BlockState state) {
        super(LittleFramesRegistry.BE_CREATIVE_FRAME.get(), pos, state);
    }
    
    protected BECreativePictureFrame(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    
    @OnlyIn(Dist.CLIENT)
    public boolean isURLEmpty() {
        return !data.hasURI();
    }
    
    public void setURL(URI url) {
        this.data.setURI(url);
    }
    
    @OnlyIn(Dist.CLIENT)
    public FrameDisplay requestDisplay() {
        if (!data.hasURI() && display != null) {
            cleanDisplay();
            return null;
        }
        
        if (released) {
            cache = null;
            return null;
        }
        
        if (cache == null && !data.hasURI()) {
            this.cleanDisplay();
            return null;
        }
        
        if (cache == null || (data.hasURI() && !cache.uri.equals(data.getURI()))) {
            cache = ImageAPI.getCache(data.getURI(), Minecraft.getInstance());
            cleanDisplay();
        }
        
        switch (cache.getStatus()) {
            case READY -> {
                if (display != null)
                    return display;
                if (cache.isVideo())
                    return display = FrameVideoDisplay.createVideoDisplay(new Vec3d(worldPosition), data);
                else
                    return display = new FramePictureDisplay(cache);
            }
            case WAITING -> {
                cleanDisplay();
                cache.load();
                return display;
            }
            case LOADING, FAILED -> {
                return null;
            }
            case FORGOTTEN -> {
                LOGGER.warn("Cached picture is forgotten, cleaning and reloading");
                cache = null;
                return null;
            }
            default -> {
                LOGGER.warn("WATERMeDIA Behavior is modified, this shouldn't be executed");
                return null;
            }
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    private void cleanDisplay() {
        if (display != null) {
            display.release();
            display = null;
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    private void release() {
        cleanDisplay();
        released = true;
    }
    
    public AlignedBox getBox() {
        Direction direction = getBlockState().getValue(BlockCreativePictureFrame.FACING);
        Facing facing = Facing.get(direction);
        AlignedBox box = BlockCreativePictureFrame.box(direction);
        
        Axis one = facing.one();
        Axis two = facing.two();
        
        if (facing.axis != Axis.Z) {
            one = facing.two();
            two = facing.one();
        }
        
        box.setMin(one, min.x);
        box.setMax(one, max.x);
        
        box.setMin(two, min.y);
        box.setMax(two, max.y);
        return box;
    }
    
    public float getSizeX() {
        return max.x - min.x;
    }
    
    public float getSizeY() {
        return max.y - min.y;
    }
    
    @Override
    protected void saveAdditional(CompoundTag nbt, Provider provider) {
        super.saveAdditional(nbt, provider);
        savePicture(nbt);
    }
    
    public void play() {
        playing = true;
        LittleFrames.NETWORK.sendToClient(new CreativePictureFramePacket(worldPosition, playing, data.tick), level, worldPosition);
    }
    
    public void pause() {
        playing = false;
        LittleFrames.NETWORK.sendToClient(new CreativePictureFramePacket(worldPosition, playing, data.tick), level, worldPosition);
    }
    
    public void stop() {
        playing = false;
        data.stop();
        LittleFrames.NETWORK.sendToClient(new CreativePictureFramePacket(worldPosition, playing, data.tick), level, worldPosition);
    }
    
    public boolean setVisible(boolean visible) {
        if (visible != isVisible()) {
            CompoundTag nbt = new CompoundTag();
            saveAdditional(nbt, level.registryAccess());
            Direction facing = getBlockState().getValue(BlockCreativePictureFrame.FACING);
            BlockState newState = visible ? LittleFramesRegistry.CREATIVE_PICTURE_FRAME.value().defaultBlockState() : LittleFramesRegistry.CREATIVE_PICTURE_FRAME_INVISIBLE.value()
                    .defaultBlockState();
            level.setBlockAndUpdate(worldPosition, newState.setValue(BlockCreativePictureFrame.FACING, facing));
            if (level.getBlockEntity(worldPosition) instanceof BECreativePictureFrame f)
                f.loadAdditional(nbt, level.registryAccess());
            return true;
        }
        return false;
    }
    
    protected void savePicture(CompoundTag nbt) {
        nbt.putFloat("minx", min.x);
        nbt.putFloat("miny", min.y);
        nbt.putFloat("maxx", max.x);
        nbt.putFloat("maxy", max.y);
        nbt.putFloat("rotation", rotation);
        nbt.putBoolean("bothSides", bothSides);
        nbt.putBoolean("flipX", flipX);
        nbt.putBoolean("flipY", flipY);
        nbt.putBoolean("playing", playing);
        nbt.put("data", data.save());
    }
    
    @Override
    protected void loadAdditional(CompoundTag nbt, Provider provider) {
        super.loadAdditional(nbt, provider);
        loadPicture(nbt);
    }
    
    protected void loadPicture(CompoundTag nbt) {
        min.x = nbt.getFloat("minx");
        min.y = nbt.getFloat("miny");
        max.x = nbt.getFloat("maxx");
        max.y = nbt.getFloat("maxy");
        rotation = nbt.getFloat("rotation");
        bothSides = nbt.getBoolean("bothSides");
        flipX = nbt.getBoolean("flipX");
        flipY = nbt.getBoolean("flipY");
        playing = nbt.getBoolean("playing");
        
        if (nbt.contains("data"))
            data = new LittleFrameData(nbt.getCompound("data"));
        else if (nbt.contains("url"))
            data = LittleFrameData.ofOldData(nbt);
        else
            data = new LittleFrameData();
        
        if (nbt.contains("visibleFrame"))
            shouldConvert = !nbt.getBoolean("visibleFrame");
    }
    
    @Override
    public void handleUpdate(CompoundTag nbt, boolean chunkUpdate) {
        loadPicture(nbt);
        markDirty();
    }
    
    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (blockEntity instanceof BECreativePictureFrame be) {
            if (be.shouldConvert) {
                be.setVisible(false);
                return;
            }
            if (level.isClientSide) {
                FrameDisplay display = be.requestDisplay();
                if (display != null && display.canTick())
                    display.tick(be.data, be.playing);
                
                if (be.data.refreshInterval > 0) {
                    if (be.data.refreshCounter <= 0) {
                        be.data.refreshCounter = be.data.refreshInterval;
                        if (be.cache != null)
                            be.cache.reload();
                    } else
                        be.data.refreshCounter--;
                }
            }
            if (be.playing)
                be.data.tick++;
        }
    }
    
    @Override
    public void setRemoved() {
        if (isClient() && display != null)
            release();
        super.setRemoved();
    }
    
    @Override
    public void onChunkUnloaded() {
        if (isClient() && display != null)
            release();
        super.onChunkUnloaded();
    }
    
    public boolean isVisible() {
        return true;
    }
}