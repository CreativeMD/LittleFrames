package team.creative.littleframes.common.block;

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
import team.creative.creativecore.common.be.BlockEntityCreative;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.vec.Vec2f;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littleframes.LittleFrames;
import team.creative.littleframes.LittleFramesRegistry;
import team.creative.littleframes.client.display.FrameDisplay;
import team.creative.littleframes.client.texture.TextureCache;
import team.creative.littleframes.common.packet.CreativePictureFramePacket;

public class BECreativePictureFrame extends BlockEntityCreative {
    
    @OnlyIn(Dist.CLIENT)
    public static String replaceVariables(String url) {
        return url.replace("$(name)", Minecraft.getInstance().player.getDisplayName().getString()).replace("$(uuid)", Minecraft.getInstance().player.getStringUUID());
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
    
    @OnlyIn(Dist.CLIENT)
    public TextureCache cache;
    
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
    
    public FrameDisplay requestDisplay() {
        String url = getURL();
        if (cache == null || !cache.url.equals(url)) {
            cache = TextureCache.get(url);
            if (display != null)
                display.release();
            display = null;
        }
        if (!cache.isVideo() && (!cache.ready() || cache.getError() != null))
            return null;
        if (display != null)
            return display;
        return display = cache.createDisplay(new Vec3d(worldPosition), url, volume, minDistance, maxDistance, loop);
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
                if (display != null)
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
