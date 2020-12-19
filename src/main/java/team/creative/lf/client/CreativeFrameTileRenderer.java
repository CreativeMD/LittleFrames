package team.creative.lf.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.lf.block.TileEntityCreativeFrame;
import team.creative.lf.client.display.FrameDisplay;

@SideOnly(Side.CLIENT)
public class CreativeFrameTileRenderer extends TileEntitySpecialRenderer<TileEntityCreativeFrame> {
	
	@Override
	public void render(TileEntityCreativeFrame frame, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (frame.url.isEmpty() || frame.alpha == 0)
			return;
		
		FrameDisplay display = frame.requestDisplay();
		if (display == null)
			return;
		
		display.prepare(frame);
		
		GlStateManager.enableBlend();
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GlStateManager.disableLighting();
		GlStateManager.color(frame.brightness, frame.brightness, frame.brightness, frame.alpha);
		int texture = display.texture();
		GlStateManager.bindTexture(texture);
		
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		
		GlStateManager.pushMatrix();
		
		GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
		
		EnumFacing direction = EnumFacing.getFront(frame.getBlockMetadata());
		/*applyDirection(direction);
		if (direction == EnumFacing.UP || direction == EnumFacing.DOWN)
			GL11.glRotatef(90, 0, 1, 0);
		
		double posX = -0.5 + sizeX / 2D;
		if (frame.posX == 1)
			posX = 0;
		else if (frame.posX == 2)
			posX = -posX;
		double posY = -0.5 + sizeY / 2D;
		if (frame.posY == 1)
			posY = 0;
		else if (frame.posY == 2)
			posY = -posY;
		
		if ((frame.rotation == 1 || frame.rotation == 3) && (frame.posX == 2 ^ frame.posY == 2))
			GL11.glRotated(180, 1, 0, 0);
		
		GL11.glRotated(frame.rotation * 90, 1, 0, 0);
		
		GL11.glRotated(frame.rotationX, 0, 1, 0);
		GL11.glRotated(frame.rotationY, 0, 0, 1);
		
		GL11.glTranslated(-0.945, posY, posX);*/
		
		GlStateManager.enableRescaleNormal();
		GL11.glScaled(1, 1, 1);
		
		GL11.glBegin(GL11.GL_POLYGON);
		GL11.glNormal3f(1.0f, 0.0F, 0.0f);
		
		GL11.glTexCoord3f(frame.flipY ? 0 : 1, frame.flipX ? 0 : 1, 0);
		GL11.glVertex3f(0.5F, -0.5f, -0.5f);
		GL11.glTexCoord3f(frame.flipY ? 0 : 1, frame.flipX ? 1 : 0, 0);
		GL11.glVertex3f(0.5f, 0.5f, -0.5f);
		GL11.glTexCoord3f(frame.flipY ? 1 : 0, frame.flipX ? 1 : 0, 0);
		GL11.glVertex3f(0.5f, 0.5f, 0.5f);
		GL11.glTexCoord3f(frame.flipY ? 1 : 0, frame.flipX ? 0 : 1, 0);
		GL11.glVertex3f(0.5f, -0.5f, 0.5f);
		GL11.glEnd();
		
		GlStateManager.popMatrix();
		
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
	}
	
	@Override
	public boolean isGlobalRenderer(TileEntityCreativeFrame te) {
		return te.getSizeX() > 16 || te.getSizeY() > 16;
	}
	
}
