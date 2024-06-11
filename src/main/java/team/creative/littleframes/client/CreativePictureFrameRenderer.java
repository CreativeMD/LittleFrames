package team.creative.littleframes.client;

import com.mojang.math.Matrix4f;
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
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.box.BoxFace;
import team.creative.littleframes.client.display.FrameDisplay;
import team.creative.littleframes.common.block.BECreativePictureFrame;
import team.creative.littleframes.common.block.BlockCreativePictureFrame;

@OnlyIn(Dist.CLIENT)
public class CreativePictureFrameRenderer implements BlockEntityRenderer<BECreativePictureFrame> {

    @Override
    public boolean shouldRenderOffScreen(BECreativePictureFrame frame) {
        return frame.getSizeX() > 16 || frame.getSizeY() > 16;
    }

    @Override
    public boolean shouldRender(BECreativePictureFrame frame, Vec3 vec) {
        return Vec3.atCenterOf(frame.getBlockPos()).closerThan(vec, frame.renderDistance);
    }

    @Override
    public void render(BECreativePictureFrame frame, float partialTicks, PoseStack pose, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (frame.isURLEmpty() || frame.alpha == 0) {
            if (frame.display != null)
                frame.display.release();
            return;
        }

        FrameDisplay display = frame.requestDisplay();
        if (display == null)
            return;

        int texture = display.prepare(frame.getURL(), frame.volume * Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER), frame.minDistance, frame.maxDistance,
                frame.playing, frame.loop, frame.tick);

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(frame.brightness, frame.brightness, frame.brightness, frame.alpha);

        if (texture == -1)
            return;
        RenderSystem.bindTexture(texture);
        RenderSystem.setShaderTexture(0, texture);

        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        Facing facing = Facing.get(frame.getBlockState().getValue(BlockCreativePictureFrame.FACING));
        AlignedBox box = frame.getBox();
        box.grow(facing.axis, 0.01F);
        BoxFace face = BoxFace.get(facing);

        pose.pushPose();

        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(facing.rotation().rotation((float) Math.toRadians(-frame.rotation)));
        pose.translate(-0.5, -0.5, -0.5);

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        Matrix4f mat = pose.last().pose();
        for (BoxCorner corner : face.corners)
            builder.vertex(mat, box.get(corner.x), box.get(corner.y), box.get(corner.z)).uv(corner.isFacing(face.getTexU()) != frame.flipX ? 1 : 0, corner.isFacing(face
                    .getTexV()) != frame.flipY ? 1 : 0).color(255, 255, 255, 255).endVertex();
        tesselator.end();

        if (frame.bothSides) {
            builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

            for (int i = face.corners.length - 1; i >= 0; i--) {
                BoxCorner corner = face.corners[i];
                builder.vertex(mat, box.get(corner.x), box.get(corner.y), box.get(corner.z)).uv(corner.isFacing(face.getTexU()) != frame.flipX ? 1 : 0, corner.isFacing(face
                        .getTexV()) != frame.flipY ? 1 : 0).color(255, 255, 255, 255).endVertex();
            }
            tesselator.end();
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        pose.popPose();
    }

}
