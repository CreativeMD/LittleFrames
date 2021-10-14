package team.creative.littleframes.common.block;

import javax.vecmath.Vector2f;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.tileentity.TileEntityCreative;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.math.box.AlignedBox;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.littleframes.client.display.FrameDisplay;
import team.creative.littleframes.client.texture.TextureCache;
import team.creative.littleframes.common.packet.CreativeFramePacket;
import team.creative.littleframes.common.structure.LittleFrame;

public class TileEntityCreativeFrame extends TileEntityCreative implements ITickable {
    
    private String url = "";
    public Vector2f min = new Vector2f(0, 0);
    public Vector2f max = new Vector2f(1, 1);
    
    public float rotation = 0;
    public boolean flipX = false;
    public boolean flipY = false;
    
    public boolean visibleFrame = true;
    public boolean bothSides = false;
    
    public float brightness = 1;
    public float alpha = 1;
    
    public int renderDistance = 128;
    
    public float volume = 1;
    public boolean loop = true;
    public int tick = 0;
    public boolean playing = true;
    
    @SideOnly(Side.CLIENT)
    public boolean isURLEmpty() {
        return url.isEmpty();
    }
    
    @SideOnly(Side.CLIENT)
    public String getURL() {
        return LittleFrame.getUrl(url);
    }
    
    public String getRealURL() {
        return url;
    }
    
    public void setURL(String url) {
        this.url = url;
    }
    
    @SideOnly(Side.CLIENT)
    public TextureCache cache;
    
    @SideOnly(Side.CLIENT)
    public FrameDisplay display;
    
    @SideOnly(Side.CLIENT)
    public FrameDisplay requestDisplay() {
        String url = getURL();
        if (cache == null || !cache.url.equals(url)) {
            cache = TextureCache.get(url);
            if (display != null)
                display.release();
            display = null;
        }
        if (display != null)
            return display;
        if (cache.ready())
            return display = cache.createDisplay(url, volume, loop);
        return null;
    }
    
    public AlignedBox getBox() {
        AlignedBox box = new AlignedBox();
        EnumFacing facing = EnumFacing.getFront(getBlockMetadata());
        if (facing.getAxisDirection() == AxisDirection.POSITIVE) {
            box.setMin(facing.getAxis(), 0F);
            box.setMax(facing.getAxis(), 0.031F);
        } else {
            box.setMin(facing.getAxis(), 0.969F);
            box.setMax(facing.getAxis(), 1F);
        }
        Axis one;
        Axis two;
        
        if (facing.getAxis() != Axis.Z) {
            one = RotationUtils.getTwo(facing.getAxis());
            two = RotationUtils.getOne(facing.getAxis());
        } else {
            one = RotationUtils.getOne(facing.getAxis());
            two = RotationUtils.getTwo(facing.getAxis());
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
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return Math.pow(renderDistance, 2);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return getBox().getBB(pos);
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);
        writePictureNBT(nbt);
        return nbt;
    }
    
    public void play() {
        playing = true;
        PacketHandler.sendPacketToTrackingPlayers(new CreativeFramePacket(pos, playing, tick), world, pos, null);
    }
    
    public void pause() {
        playing = false;
        PacketHandler.sendPacketToTrackingPlayers(new CreativeFramePacket(pos, playing, tick), world, pos, null);
    }
    
    public void stop() {
        playing = false;
        tick = 0;
        PacketHandler.sendPacketToTrackingPlayers(new CreativeFramePacket(pos, playing, tick), world, pos, null);
    }
    
    protected void writePictureNBT(NBTTagCompound nbt) {
        nbt.setString("url", url);
        nbt.setFloat("minx", min.x);
        nbt.setFloat("miny", min.y);
        nbt.setFloat("maxx", max.x);
        nbt.setFloat("maxy", max.y);
        nbt.setFloat("rotation", rotation);
        nbt.setInteger("render", renderDistance);
        nbt.setBoolean("visibleFrame", visibleFrame);
        nbt.setBoolean("bothSides", bothSides);
        nbt.setBoolean("flipX", flipX);
        nbt.setBoolean("flipX", flipX);
        nbt.setFloat("alpha", alpha);
        nbt.setFloat("brightness", brightness);
        
        nbt.setFloat("volume", volume);
        nbt.setBoolean("playing", playing);
        nbt.setInteger("tick", tick);
        nbt.setBoolean("loop", loop);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        readPictureNBT(nbt);
    }
    
    protected void readPictureNBT(NBTTagCompound nbt) {
        url = nbt.getString("url");
        min.x = nbt.getFloat("minx");
        min.y = nbt.getFloat("miny");
        max.x = nbt.getFloat("maxx");
        max.y = nbt.getFloat("maxy");
        rotation = nbt.getFloat("rotation");
        renderDistance = nbt.getInteger("render");
        visibleFrame = nbt.getBoolean("visibleFrame");
        bothSides = nbt.getBoolean("bothSides");
        flipX = nbt.getBoolean("flipX");
        flipX = nbt.getBoolean("flipX");
        if (nbt.hasKey("alpha"))
            alpha = nbt.getFloat("alpha");
        else
            alpha = 1;
        if (nbt.hasKey("brightness"))
            brightness = nbt.getFloat("brightness");
        else
            brightness = 1;
        
        volume = nbt.getFloat("volume");
        playing = nbt.getBoolean("playing");
        tick = nbt.getInteger("tick");
        loop = nbt.getBoolean("loop");
    }
    
    @Override
    public void getDescriptionNBT(NBTTagCompound nbt) {
        super.getDescriptionNBT(nbt);
        writePictureNBT(nbt);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void receiveUpdatePacket(NBTTagCompound nbt) {
        super.receiveUpdatePacket(nbt);
        readPictureNBT(nbt);
    }
    
    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }
    
    @Override
    public void update() {
        if (playing)
            tick++;
    }
    
    @Override
    public void invalidate() {
        super.invalidate();
        if (isClientSide() && display != null)
            display.release();
    }
    
    @Override
    public void onChunkUnload() {
        if (isClientSide() && display != null)
            display.release();
    }
}
