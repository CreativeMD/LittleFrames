package team.creative.lf.client.display;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import team.creative.lf.block.TileEntityCreativeFrame;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;

public class FrameVideoDisplay extends FrameDisplay {
	
	public int width;
	public int height;
	public CallbackMediaPlayerComponent player;
	public ByteBuffer buffer;
	public int texture;
	private AtomicBoolean needsUpdate = new AtomicBoolean(false);
	private boolean first = true;
	
	public FrameVideoDisplay(String url) {
		super();
		texture = GlStateManager.generateTexture();
		
		player = new CallbackMediaPlayerComponent(new MediaPlayerFactory("--quiet"), null, null, false, new RenderCallback() {
			
			@Override
			public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
				buffer = nativeBuffers[0];
				needsUpdate.set(true);
			}
		}, new BufferFormatCallback() {
			
			@Override
			public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
				FrameVideoDisplay.this.width = sourceWidth;
				FrameVideoDisplay.this.height = sourceHeight;
				FrameVideoDisplay.this.first = true;
				return new BufferFormat("RGBA", sourceWidth, sourceHeight, new int[] { sourceWidth * 4 }, new int[] { sourceHeight });
			}
			
			@Override
			public void allocatedBuffers(ByteBuffer[] buffers) {
				
			}
			
		}, null);
		player.mediaPlayer().submit(() -> {
			player.mediaPlayer().media().start(url);
		});
	}
	
	@Override
	public void prepare(TileEntityCreativeFrame frame) {
		synchronized (this) {
			if (buffer != null && first) {
				GlStateManager.pushMatrix();
				GlStateManager.bindTexture(texture);
				GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
				GlStateManager.popMatrix();
				first = false;
			}
			if (needsUpdate.getAndSet(false)) {
				GlStateManager.pushMatrix();
				GlStateManager.bindTexture(texture);
				
				//GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
				GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
				
				GlStateManager.popMatrix();
			}
		}
	}
	
	@Override
	public void release() {
		player.release();
	}
	
	@Override
	public int texture() {
		return texture;
	}
	
	@Override
	public void pause(TileEntityCreativeFrame frame) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void resume(TileEntityCreativeFrame frame) {
		// TODO Auto-generated method stub
		
	}
	
}
