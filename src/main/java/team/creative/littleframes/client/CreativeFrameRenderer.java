package team.creative.littleframes.client;

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
import net.minecraft.client.renderer.GlStateManager.CullFace;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.box.BoxFace;
import team.creative.littleframes.client.display.FrameDisplay;
import team.creative.littleframes.common.block.BECreativeFrame;
import team.creative.littleframes.common.block.BlockCreativeFrame;

@OnlyIn(Dist.CLIENT)
public class CreativeFrameRenderer implements BlockEntityRenderer<BECreativeFrame> {
    
    @Override
    public boolean shouldRenderOffScreen(BECreativeFrame frame) {
        return frame.getSizeX() > 16 || frame.getSizeY() > 16;
    }
    
    @Override
    public boolean shouldRender(BECreativeFrame frame, Vec3 vec) {
        return Vec3.atCenterOf(frame.getBlockPos()).closerThan(vec, frame.renderDistance);
    }
    
    @Override
    public void render(BECreativeFrame frame, float partialTicks, PoseStack pose, MultiBufferSource buffer, int p_112311_, int p_112312_) {
        if (frame.isURLEmpty() || frame.alpha == 0) {
            if (frame.display != null)
                frame.display.release();
            return;
        }
        
        FrameDisplay display = frame.requestDisplay();
        if (display == null)
            return;
        
        display.prepare(frame.getURL(), frame.volume * Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER), frame.playing, frame.loop, frame.tick);
        
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        //RenderSystem.disableLighting();
        RenderSystem.setShaderColor(frame.brightness, frame.brightness, frame.brightness, frame.alpha);
        int texture = display.texture();
        RenderSystem.bindTexture(texture);
        //GlStateManager.cullFace(CullFace.BACK);
        
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        
        pose.pushPose();
        
        Facing facing = Facing.get(frame.getBlockState().getValue(BlockCreativeFrame.FACING));
        AlignedBox box = frame.getBox();
        BoxFace face = BoxFace.get(facing);
        
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        for (BoxCorner corner : face.corners)
            builder.pos(box.getValueOfFacing(corner.x), box.getValueOfFacing(corner.y), box.getValueOfFacing(corner.z))
                    .tex(corner.isFacing(face.getTexU()) != frame.flipX ? 1 : 0, corner.isFacing(face.getTexV()) != frame.flipY ? 1 : 0).endVertex();
        tesselator.end();
        
        if (frame.bothSides) {
            GlStateManager.cullFace(CullFace.FRONT);
            builder.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX);
            for (BoxCorner corner : face.corners)
                builder.pos(box.getValueOfFacing(corner.x), box.getValueOfFacing(corner.y), box.getValueOfFacing(corner.z))
                        .tex(corner.isFacing(face.getTexU()) != frame.flipX ? 1 : 0, corner.isFacing(face.getTexV()) != frame.flipY ? 1 : 0).endVertex();
            tessellator.draw();
        }
        
        pose.popPose();
    }
    
}
