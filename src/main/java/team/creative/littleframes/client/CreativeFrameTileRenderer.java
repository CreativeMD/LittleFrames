package team.creative.littleframes.client;

import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.common.utils.math.box.AlignedBox;
import com.creativemd.creativecore.common.utils.math.box.BoxCorner;
import com.creativemd.creativecore.common.utils.math.box.BoxFace;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.CullFace;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.littleframes.client.display.FrameDisplay;
import team.creative.littleframes.common.block.TileEntityCreativeFrame;

@SideOnly(Side.CLIENT)
public class CreativeFrameTileRenderer extends TileEntitySpecialRenderer<TileEntityCreativeFrame> {
	
	@Override
	public void render(TileEntityCreativeFrame frame, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (frame.isURLEmpty() || frame.alpha == 0) {
			if (frame.display != null)
				frame.display.release();
			return;
		}
		
		FrameDisplay display = frame.requestDisplay();
		if (display == null)
			return;
		
		display.prepare(frame.getURL(), frame.volume, frame.playing, frame.loop, frame.tick);
		
		GlStateManager.enableBlend();
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GlStateManager.disableLighting();
		GlStateManager.color(frame.brightness, frame.brightness, frame.brightness, frame.alpha);
		int texture = display.texture();
		GlStateManager.bindTexture(texture);
		
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		
		GlStateManager.pushMatrix();
		
		GlStateManager.translate(x, y, z);
		
		EnumFacing facing = EnumFacing.getFront(frame.getBlockMetadata());
		AlignedBox box = frame.getBox();
		BoxFace face = BoxFace.get(facing);
		
		GlStateManager.translate(0.5, 0.5, 0.5);
		GlStateManager.rotate(facing.getAxisDirection() == AxisDirection.POSITIVE ? -frame.rotation : frame.rotation, facing.getAxis() == Axis.X ? 1 : 0, facing.getAxis() == Axis.Y ? 1 : 0, facing.getAxis() == Axis.Z ? 1 : 0);
		GlStateManager.translate(-0.5, -0.5, -0.5);
		
		GlStateManager.enableRescaleNormal();
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();
		builder.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX);
		for (BoxCorner corner : face.corners)
			builder.pos(box.getValueOfFacing(corner.x), box.getValueOfFacing(corner.y), box.getValueOfFacing(corner.z)).tex(corner.isFacing(face.getTexU()) != frame.flipX ? 1 : 0, corner.isFacing(face.getTexV()) != frame.flipY ? 1 : 0).endVertex();
		tessellator.draw();
		
		if (frame.bothSides) {
			GlStateManager.cullFace(CullFace.FRONT);
			builder.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX);
			for (BoxCorner corner : face.corners)
				builder.pos(box.getValueOfFacing(corner.x), box.getValueOfFacing(corner.y), box.getValueOfFacing(corner.z)).tex(corner.isFacing(face.getTexU()) != frame.flipX ? 1 : 0, corner.isFacing(face.getTexV()) != frame.flipY ? 1 : 0).endVertex();
			tessellator.draw();
		}
		
		GlStateManager.popMatrix();
		
		GlStateManager.cullFace(CullFace.BACK);
		
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
	}
	
	@Override
	public boolean isGlobalRenderer(TileEntityCreativeFrame te) {
		return te.getSizeX() > 16 || te.getSizeY() > 16;
	}
	
}
