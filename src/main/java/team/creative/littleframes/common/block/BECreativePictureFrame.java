package team.creative.littleframes.common.block;

import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageCache;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLPaths;
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
import team.creative.littleframes.common.packet.CreativePictureFramePacket;

import static team.creative.littleframes.LittleFrames.LOGGER;

public class BECreativePictureFrame extends BlockEntityCreative {
    
    @OnlyIn(Dist.CLIENT)
    public static String replaceVariables(String url) {
        String result = url.replace("$(name)", Minecraft.getInstance().player.getDisplayName().getString()).replace("$(uuid)", Minecraft.getInstance().player.getStringUUID());
        if (result.startsWith("minecraft://"))
            result = result.replace("minecraft://", "file:///" + FMLPaths.GAMEDIR.get().toAbsolutePath().toString().replace("\\", "/") + "/");
        return result;
    }
    
    private String url = "";
    public Vec2f min = new Vec2f(0, 0);
    public Vec2f max = new Vec2f(1, 1);
    
    public float rotation = 0;
    public boolean flipX = false;
    public boolean flipY = false;
    
    public boolean visibleFrame = true;
    public boolean bothSides = false;
    
    public float brightness = 1;
    public float alpha = 1;
    
    public int renderDistance = 128;
    
    public float volume = 1;
    public float minDistance = 5;
    public float maxDistance = 20;
    
    public boolean loop = true;
    public int tick = 0;
    public boolean playing = true;

    private boolean released = false;
    
    @OnlyIn(Dist.CLIENT)
    public ImageCache cache;
    
    @OnlyIn(Dist.CLIENT)
    public FrameDisplay display;
    
    public BECreativePictureFrame(BlockPos pos, BlockState state) {
        super(LittleFramesRegistry.BE_CREATIVE_FRAME.get(), pos, state);
    }
    
    @OnlyIn(Dist.CLIENT)
    public boolean isURLEmpty() {
        return url.isEmpty();
    }
    
    @OnlyIn(Dist.CLIENT)
    public String getURL() {
        return replaceVariables(url);
    }
    
    public String getRealURL() {
        return url;
    }
    
    public void setURL(String url) {
        this.url = url;
    }

    @OnlyIn(Dist.CLIENT)
    public FrameDisplay requestDisplay() {
        String url = getURL();
        if (released) {
            cache = null;
            return null;
        }
        if (cache == null || !cache.url.equals(url)) {
            cache = ImageAPI.getCache(url, Minecraft.getInstance());
            cleanDisplay();
        }

        switch (cache.getStatus()) {
            case READY -> {
                if (display != null)
                    return display;
                if (cache.isVideo())
                    return display = FrameVideoDisplay.createVideoDisplay(new Vec3d(worldPosition), url, volume, minDistance, maxDistance, loop);
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
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        return getBox().getBB(getBlockPos());
    }
    
    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        savePicture(nbt);
    }
    
    public void play() {
        playing = true;
        LittleFrames.NETWORK.sendToClient(new CreativePictureFramePacket(worldPosition, playing, tick), level, worldPosition);
    }
    
    public void pause() {
        playing = false;
        LittleFrames.NETWORK.sendToClient(new CreativePictureFramePacket(worldPosition, playing, tick), level, worldPosition);
    }
    
    public void stop() {
        playing = false;
        tick = 0;
        LittleFrames.NETWORK.sendToClient(new CreativePictureFramePacket(worldPosition, playing, tick), level, worldPosition);
    }
    
    protected void savePicture(CompoundTag nbt) {
        nbt.putString("url", url);
        nbt.putFloat("minx", min.x);
        nbt.putFloat("miny", min.y);
        nbt.putFloat("maxx", max.x);
        nbt.putFloat("maxy", max.y);
        nbt.putFloat("rotation", rotation);
        nbt.putInt("render", renderDistance);
        nbt.putBoolean("visibleFrame", visibleFrame);
        nbt.putBoolean("bothSides", bothSides);
        nbt.putBoolean("flipX", flipX);
        nbt.putBoolean("flipY", flipY);
        nbt.putFloat("alpha", alpha);
        nbt.putFloat("brightness", brightness);
        
        nbt.putFloat("volume", volume);
        nbt.putFloat("min", minDistance);
        nbt.putFloat("max", maxDistance);
        
        nbt.putBoolean("playing", playing);
        nbt.putInt("tick", tick);
        nbt.putBoolean("loop", loop);
    }
    
    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        loadPicture(nbt);
    }
    
    protected void loadPicture(CompoundTag nbt) {
        url = nbt.getString("url");
        min.x = nbt.getFloat("minx");
        min.y = nbt.getFloat("miny");
        max.x = nbt.getFloat("maxx");
        max.y = nbt.getFloat("maxy");
        rotation = nbt.getFloat("rotation");
        renderDistance = nbt.getInt("render");
        visibleFrame = nbt.getBoolean("visibleFrame");
        bothSides = nbt.getBoolean("bothSides");
        flipX = nbt.getBoolean("flipX");
        flipY = nbt.getBoolean("flipY");
        if (nbt.contains("alpha"))
            alpha = nbt.getFloat("alpha");
        else
            alpha = 1;
        if (nbt.contains("brightness"))
            brightness = nbt.getFloat("brightness");
        else
            brightness = 1;
        
        volume = nbt.getFloat("volume");
        if (nbt.contains("min"))
            minDistance = nbt.getFloat("min");
        else
            minDistance = 5;
        if (nbt.contains("max"))
            maxDistance = nbt.getFloat("max");
        else
            maxDistance = 20;
        
        playing = nbt.getBoolean("playing");
        tick = nbt.getInt("tick");
        loop = nbt.getBoolean("loop");
    }
    
    @Override
    public void handleUpdate(CompoundTag nbt, boolean chunkUpdate) {
        loadPicture(nbt);
        markDirty();
    }
    
    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (blockEntity instanceof BECreativePictureFrame be) {
            if (level.isClientSide) {
                FrameDisplay display = be.requestDisplay();
                if (display != null && display.canTick())
                    display.tick(be.url, be.volume, be.minDistance, be.maxDistance, be.playing, be.loop, be.tick);
            }
            if (be.playing)
                be.tick++;
        }
    }
    
    @Override
    public void setRemoved() {
        if (isClient() && display != null)
            display.release();
    }
    
    @Override
    public void onChunkUnloaded() {
        if (isClient() && display != null)
            display.release();
    }
}
