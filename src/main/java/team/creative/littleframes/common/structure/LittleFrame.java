package team.creative.littleframes.common.structure;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.math.VectorUtils;
import com.creativemd.creativecore.common.utils.math.box.AlignedBox;
import com.creativemd.creativecore.common.utils.math.box.BoxCorner;
import com.creativemd.creativecore.common.utils.math.box.BoxFace;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.client.gui.handler.LittleStructureGuiHandler;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.directional.StructureDirectional;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.relative.StructureRelative;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.CullFace;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.littleframes.client.display.FrameDisplay;
import team.creative.littleframes.client.texture.TextureCache;
import team.creative.littleframes.common.packet.LittleFramePacket;

public class LittleFrame extends LittleStructure {
    
    @StructureDirectional(color = ColorUtils.CYAN)
    public StructureRelative frame;
    
    @StructureDirectional
    public EnumFacing facing;
    
    @StructureDirectional
    public Vector3f topRight;
    
    private String url = "";
    public float brightness = 1;
    public float alpha = 1;
    
    public int renderDistance = 64;
    
    public FitMode fitMode = FitMode.CROP;
    
    public float volume = 1;
    public boolean loop = true;
    public int tick = 0;
    public boolean playing = true;
    
    @SideOnly(Side.CLIENT)
    public boolean isURLEmpty() {
        return url.isEmpty();
    }
    
    public static String getUrl(String url) {
        String result = url.replace("$(name)", Minecraft.getMinecraft().player.getDisplayNameString()).replace("$(uuid)", Minecraft.getMinecraft().player.getCachedUniqueIdString());
        if (result.startsWith("minecraft://"))
            result = result.replace("minecraft://", "file:///" + Minecraft.getMinecraft().mcDataDir.getAbsolutePath().replace("\\", "/") + "/");
        return result;
    }
    
    @SideOnly(Side.CLIENT)
    public String getURL() {
        return getUrl(url);
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
    
    public LittleFrame(LittleStructureType type, IStructureTileList mainBlock) {
        super(type, mainBlock);
    }
    
    public void play() {
        playing = true;
        PacketHandler.sendPacketToTrackingPlayers(new LittleFramePacket(getStructureLocation(), playing, tick), getWorld(), getPos(), null);
    }
    
    public void pause() {
        playing = false;
        PacketHandler.sendPacketToTrackingPlayers(new LittleFramePacket(getStructureLocation(), playing, tick), getWorld(), getPos(), null);
    }
    
    public void stop() {
        playing = false;
        tick = 0;
        PacketHandler.sendPacketToTrackingPlayers(new LittleFramePacket(getStructureLocation(), playing, tick), getWorld(), getPos(), null);
    }
    
    @Override
    protected void loadFromNBTExtra(NBTTagCompound nbt) {
        url = nbt.getString("url");
        if (nbt.hasKey("render"))
            renderDistance = nbt.getInteger("render");
        else
            renderDistance = 64;
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
        fitMode = FitMode.values()[nbt.getInteger("fitMode")];
    }
    
    @Override
    protected void writeToNBTExtra(NBTTagCompound nbt) {
        nbt.setString("url", url);
        nbt.setInteger("render", renderDistance);
        nbt.setFloat("alpha", alpha);
        nbt.setFloat("brightness", brightness);
        
        nbt.setFloat("volume", volume);
        nbt.setBoolean("playing", playing);
        nbt.setInteger("tick", tick);
        nbt.setBoolean("loop", loop);
        nbt.setInteger("fitMode", fitMode.ordinal());
    }
    
    @Override
    public boolean onBlockActivated(World worldIn, LittleTile tile, BlockPos pos, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) throws LittleActionException {
        LittleStructureGuiHandler.openGui("little_frame", new NBTTagCompound(), playerIn, this);
        return true;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void renderTick(BlockPos pos, double x, double y, double z, float partialTickTime) {
        if (isURLEmpty() || alpha == 0) {
            if (display != null)
                display.release();
            return;
        }
        
        FrameDisplay display = requestDisplay();
        if (display == null)
            return;
        
        display.prepare(getURL(), volume * Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MASTER), playing, loop, tick);
        
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GlStateManager.disableLighting();
        GlStateManager.color(brightness, brightness, brightness, alpha);
        int texture = display.texture();
        
        GlStateManager.cullFace(CullFace.BACK);
        GlStateManager.bindTexture(texture);
        
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        
        GlStateManager.pushMatrix();
        
        GlStateManager.translate(x, y, z);
        
        AlignedBox box = frame.getBox().getCube(frame.getContext());
        BoxFace face = BoxFace.get(facing);
        if (facing.getAxisDirection() == AxisDirection.POSITIVE)
            box.setMax(facing.getAxis(), box.getMin(facing.getAxis()) + 0.01F);
        else
            box.setMin(facing.getAxis(), box.getMax(facing.getAxis()) - 0.01F);
        Axis uAxis = face.getTexUAxis();
        Axis vAxis = face.getTexVAxis();
        if (fitMode == FitMode.CROP) {
            float width = box.getSize(uAxis);
            float height = box.getSize(vAxis);
            float videoRatio = display.getWidth() / (float) display.getHeight();
            float ratio = width / height;
            if (ratio > videoRatio)
                box.shrink(uAxis, width - height * videoRatio);
            else if (ratio < videoRatio)
                box.shrink(vAxis, height - width / videoRatio);
        }
        
        GlStateManager.enableRescaleNormal();
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();
        builder.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX);
        for (BoxCorner corner : face.corners)
            builder.pos(box.getValueOfFacing(corner.x), box.getValueOfFacing(corner.y), box.getValueOfFacing(corner.z))
                
                .tex(corner.isFacingPositive(uAxis) != (VectorUtils.get(uAxis, topRight) > 0) ? 1 : 0, corner.isFacingPositive(vAxis) != (VectorUtils.get(vAxis, topRight) > 0) ? 1 : 0)
                .endVertex();
        tessellator.draw();
        
        GlStateManager.popMatrix();
        
        GlStateManager.cullFace(CullFace.BACK);
        
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return Math.pow(renderDistance, 2);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return frame.getBox().getBox(frame.getContext());
    }
    
    @Override
    public void tick() {
        super.tick();
        if (playing)
            tick++;
    }
    
    @Override
    public void unload() {
        super.unload();
        if (getWorld().isRemote && display != null)
            display.release();
    }
    
    public static enum FitMode {
        CROP,
        STRETCH;
    }
    
}
