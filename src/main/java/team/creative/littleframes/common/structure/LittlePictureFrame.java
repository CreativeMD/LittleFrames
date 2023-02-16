package team.creative.littleframes.common.structure;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.creator.GuiCreator;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.box.BoxFace;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.math.vec.VectorUtils;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littleframes.LittleFrames;
import team.creative.littleframes.client.display.FrameDisplay;
import team.creative.littleframes.client.gui.GuiLittlePictureFrame;
import team.creative.littleframes.client.texture.TextureCache;
import team.creative.littleframes.common.block.BECreativePictureFrame;
import team.creative.littleframes.common.packet.LittlePictureFramePacket;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.gui.handler.LittleStructureGuiCreator;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.directional.StructureDirectional;
import team.creative.littletiles.common.structure.relative.StructureRelative;

public class LittlePictureFrame extends LittleStructure {
    
    public static final LittleStructureGuiCreator GUI = GuiCreator
            .register("little_frame", new LittleStructureGuiCreator((nbt, player, structure) -> new GuiLittlePictureFrame((LittlePictureFrame) structure)));
    
    @StructureDirectional(color = ColorUtils.CYAN)
    public StructureRelative frame;
    
    @StructureDirectional
    public Facing facing;
    
    @StructureDirectional
    public Vector3f topRight;
    
    private String url = "";
    public float brightness = 1;
    public float alpha = 1;
    
    public int renderDistance = 64;
    
    public FitMode fitMode = FitMode.CROP;
    
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
    
    public LittlePictureFrame(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @OnlyIn(Dist.CLIENT)
    public boolean isURLEmpty() {
        return url.isEmpty();
    }
    
    @OnlyIn(Dist.CLIENT)
    public String getURL() {
        return BECreativePictureFrame.replaceVariables(url);
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
        return display = cache.createDisplay(new Vec3d(getPos()), url, volume, minDistance, maxDistance, loop);
    }
    
    public void play() {
        playing = true;
        LittleFrames.NETWORK.sendToClient(new LittlePictureFramePacket(getStructureLocation(), playing, tick), getLevel(), getPos());
    }
    
    public void pause() {
        playing = false;
        LittleFrames.NETWORK.sendToClient(new LittlePictureFramePacket(getStructureLocation(), playing, tick), getLevel(), getPos());
    }
    
    public void stop() {
        playing = false;
        tick = 0;
        LittleFrames.NETWORK.sendToClient(new LittlePictureFramePacket(getStructureLocation(), playing, tick), getLevel(), getPos());
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        url = nbt.getString("url");
        if (nbt.contains("render"))
            renderDistance = nbt.getInt("render");
        else
            renderDistance = 64;
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
        fitMode = FitMode.values()[nbt.getInt("fitMode")];
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        nbt.putString("url", url);
        nbt.putInt("render", renderDistance);
        nbt.putFloat("alpha", alpha);
        nbt.putFloat("brightness", brightness);
        
        nbt.putFloat("volume", volume);
        nbt.putFloat("min", minDistance);
        nbt.putFloat("max", maxDistance);
        
        nbt.putBoolean("playing", playing);
        nbt.putInt("tick", tick);
        nbt.putBoolean("loop", loop);
        nbt.putInt("fitMode", fitMode.ordinal());
    }
    
    @Override
    public boolean canInteract() {
        return true;
    }
    
    @Override
    public InteractionResult use(Level level, LittleTileContext context, BlockPos pos, Player player, BlockHitResult result) {
        GUI.open(player, this);
        return InteractionResult.SUCCESS;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderTick(PoseStack pose, MultiBufferSource buffer, BlockPos pos, float partialTickTime) {
        if (isURLEmpty() || alpha == 0) {
            if (display != null)
                display.release();
            return;
        }
        
        FrameDisplay display = requestDisplay();
        if (display == null)
            return;
        
        display.prepare(getURL(), volume * Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER), minDistance, maxDistance, playing, loop, tick);
        
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(brightness, brightness, brightness, alpha);
        int texture = display.texture();
        RenderSystem.bindTexture(texture);
        RenderSystem.setShaderTexture(0, texture);
        
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        
        AlignedBox box = frame.getBox().getBox(frame.getGrid());
        BoxFace face = BoxFace.get(facing);
        if (facing.positive)
            box.setMax(facing.axis, box.getMin(facing.axis) + 0.01F);
        else
            box.setMin(facing.axis, box.getMax(facing.axis) - 0.01F);
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
        
        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        Matrix4f mat = pose.last().pose();
        Matrix3f mat3f = pose.last().normal();
        Vec3i normal = face.facing.normal;
        for (BoxCorner corner : face.corners)
            builder.vertex(mat, box.get(corner.x), box.get(corner.y), box.get(corner.z))
                    .uv(corner.isFacingPositive(uAxis) != (VectorUtils.get(uAxis, topRight) > 0) ? 1 : 0, corner
                            .isFacingPositive(vAxis) != (VectorUtils.get(vAxis, topRight) > 0) ? 1 : 0)
                    .color(-1).normal(mat3f, normal.getX(), normal.getY(), normal.getZ()).endVertex();
        tesselator.end();
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public double getMaxRenderDistance() {
        return renderDistance;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        return frame.getBox().getBB(frame.getGrid());
    }
    
    @Override
    public void tick() {
        super.tick();
        if (isClient()) {
            FrameDisplay display = requestDisplay();
            if (display != null)
                display.tick(url, volume, minDistance, maxDistance, playing, loop, tick);
        }
        if (playing)
            tick++;
    }
    
    @Override
    public void unload() {
        super.unload();
        if (isClient() && display != null)
            display.release();
    }
    
    public static enum FitMode {
        CROP,
        STRETCH;
    }
    
}
