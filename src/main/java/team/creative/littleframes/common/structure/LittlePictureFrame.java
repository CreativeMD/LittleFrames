package team.creative.littleframes.common.structure;

import static team.creative.littleframes.LittleFrames.LOGGER;

import java.net.URI;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.watermedia.api.image.ImageAPI;
import org.watermedia.api.image.ImageCache;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.box.BoxFace;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.math.vec.Vec3f;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littleframes.LittleFrames;
import team.creative.littleframes.LittleTilesIntegration;
import team.creative.littleframes.client.display.FrameDisplay;
import team.creative.littleframes.client.display.FramePictureDisplay;
import team.creative.littleframes.client.display.FrameVideoDisplay;
import team.creative.littleframes.common.data.LittleFrameData;
import team.creative.littleframes.common.packet.LittlePictureFramePacket;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.directional.StructureDirectional;
import team.creative.littletiles.common.structure.relative.StructureRelative;

public class LittlePictureFrame extends LittleStructure {
    
    @StructureDirectional(color = ColorUtils.CYAN)
    public StructureRelative frame;
    
    @StructureDirectional
    public Facing facing;
    
    @StructureDirectional
    public Vec3f topRight;
    
    public FitMode fitMode = FitMode.CROP;
    
    public LittleFrameData data;
    
    public boolean released = false;
    
    @OnlyIn(Dist.CLIENT)
    public ImageCache cache;
    
    @OnlyIn(Dist.CLIENT)
    public FrameDisplay display;
    
    public LittlePictureFrame(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
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
                    return display = FrameVideoDisplay.createVideoDisplay(new Vec3d(getStructurePos()), data);
                else
                    return display = new FramePictureDisplay(cache);
            }
            case WAITING -> {
                cleanDisplay();
                cache.load();
                return display;
            }
            case FAILED -> {
                return null;
            }
            case FORGOTTEN -> {
                LOGGER.warn("Cached picture is forgotten, cleaning and reloading");
                cache = null;
                return null;
            }
            case LOADING -> {
                // missing impl
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
    
    public void play() {
        if (!getOutput(0).getState().any())
            getOutput(0).toggle();
    }
    
    public void pause() {
        if (getOutput(0).getState().any())
            getOutput(0).toggle();
    }
    
    public void stop() {
        if (getOutput(0).getState().any())
            getOutput(0).toggle();
        data.stop();
        LittleFrames.NETWORK.sendToClient(new LittlePictureFramePacket(getStructureLocation(), getOutput(0).getState().any(), data.tick), getStructureLevel(), getStructurePos());
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt, HolderLookup.Provider provider) {
        fitMode = FitMode.values()[nbt.getInt("fitMode")];
        if (nbt.contains("data"))
            data = new LittleFrameData(nbt.getCompound("data"));
        else if (nbt.contains("url"))
            data = LittleFrameData.ofOldData(nbt);
        else
            data = new LittleFrameData();
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt, HolderLookup.Provider provider) {
        nbt.putInt("fitMode", fitMode.ordinal());
        nbt.put("data", data.save());
    }
    
    @Override
    public boolean canInteract() {
        return true;
    }
    
    @Override
    public InteractionResult use(Level level, LittleTileContext context, BlockPos pos, Player player, BlockHitResult result) {
        LittleTilesIntegration.LITTLE_FRAME_GUI.open(player, this);
        return InteractionResult.SUCCESS;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderTick(PoseStack pose, MultiBufferSource buffer, BlockPos pos, float partialTickTime) {
        if (isURLEmpty() || data.alpha == 0) {
            if (display != null)
                display.release();
            return;
        }
        
        FrameDisplay display = requestDisplay();
        if (display == null)
            return;
        
        display.prepare(data, getOutput(0).getState().any());
        
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(data.brightness, data.brightness, data.brightness, data.alpha);
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
        
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        Matrix4f mat = pose.last().pose();
        for (BoxCorner corner : face.corners)
            builder.addVertex(mat, box.get(corner.x), box.get(corner.y), box.get(corner.z)).setUv(corner.isFacingPositive(uAxis) != (topRight.get(uAxis) > 0) ? 1 : 0, corner
                    .isFacingPositive(vAxis) != (topRight.get(vAxis) > 0) ? 1 : 0).setColor(255, 255, 255, 255);
        BufferUploader.drawWithShader(builder.buildOrThrow());
        
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public double getMaxRenderDistance() {
        return data.renderDistance;
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
            if (display != null && display.canTick())
                display.tick(data, getOutput(0).getState().any());
            
            if (data.refreshInterval > 0) {
                if (data.refreshCounter <= 0) {
                    data.refreshCounter = data.refreshInterval;
                    if (cache != null)
                        cache.reload();
                } else
                    data.refreshCounter--;
            }
        }
        if (getOutput(0).getState().any())
            data.tick++;
    }
    
    @Override
    public void unload() {
        super.unload();
        if (isClient() && display != null)
            release();
    }
    
    public static enum FitMode {
        CROP,
        STRETCH;
    }
    
}