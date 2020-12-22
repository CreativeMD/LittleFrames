package team.creative.littleframes.client.display;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.common.utils.mc.TickUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import team.creative.littleframes.block.TileEntityCreativeFrame;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.MediaType;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;

public class FrameVideoDisplay extends FrameDisplay {
	
	private static final int ACCEPTABLE_SYNC_TIME = 1000;
	public int width = 1;
	public int height = 1;
	public CallbackMediaPlayerComponent player;
	public ByteBuffer buffer;
	public int texture;
	private boolean stream = false;
	private long durationBefore;
	private float lastSetVolume;
	private AtomicBoolean needsUpdate = new AtomicBoolean(false);
	private boolean first = true;
	
	public FrameVideoDisplay(TileEntityCreativeFrame frame) {
		super();
		texture = GlStateManager.generateTexture();
		
		player = new CallbackMediaPlayerComponent(new MediaPlayerFactory("--quiet"), null, null, false, new RenderCallback() {
			
			@Override
			public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
				synchronized (this) {
					buffer = nativeBuffers[0];
					needsUpdate.set(true);
				}
			}
		}, new BufferFormatCallback() {
			
			@Override
			public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
				synchronized (this) {
					FrameVideoDisplay.this.width = sourceWidth;
					FrameVideoDisplay.this.height = sourceHeight;
					FrameVideoDisplay.this.first = true;
				}
				return new BufferFormat("RGBA", sourceWidth, sourceHeight, new int[] { sourceWidth * 4 }, new int[] { sourceHeight });
			}
			
			@Override
			public void allocatedBuffers(ByteBuffer[] buffers) {
				
			}
			
		}, null);
		player.mediaPlayer().submit(() -> {
			player.mediaPlayer().audio().setVolume((int) (frame.volume * 100F));
			lastSetVolume = frame.volume;
			player.mediaPlayer().controls().setRepeat(frame.loop);
			player.mediaPlayer().media().start(frame.url);
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
				GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
				GlStateManager.popMatrix();
			}
		}
		if (player.mediaPlayer().status().isPlayable() && player.mediaPlayer().media().info().type() != MediaType.STREAM) {
			if (frame.volume != lastSetVolume) {
				player.mediaPlayer().submit(() -> player.mediaPlayer().audio().setVolume((int) (frame.volume * 100F)));
				lastSetVolume = frame.volume;
			}
			if (player.mediaPlayer().controls().getRepeat() != frame.loop)
				player.mediaPlayer().submit(() -> player.mediaPlayer().controls().setRepeat(frame.loop));
			long tickTime = 50;
			if (!stream && durationBefore != 0 && player.mediaPlayer().status().length() != durationBefore) {
				System.out.println("recognized stream");
				stream = true;
			}
			durationBefore = player.mediaPlayer().status().length();
			if (stream) {
				boolean playing = frame.playing && !Minecraft.getMinecraft().isGamePaused();
				if (player.mediaPlayer().status().isPlaying() != playing)
					player.mediaPlayer().submit(() -> player.mediaPlayer().controls().setPause(!playing));
			} else {
				if (player.mediaPlayer().status().length() > 0) {
					long time = frame.tick * tickTime + (frame.playing ? (long) (TickUtils.getPartialTickTime() * tickTime) : 0);
					if (player.mediaPlayer().status().isSeekable() && time > player.mediaPlayer().status().time())
						if (frame.loop)
							time %= player.mediaPlayer().status().length();
					if (Math.abs(time - player.mediaPlayer().status().time()) > ACCEPTABLE_SYNC_TIME)
						player.mediaPlayer().submit(() -> {
							long newTime = frame.tick * tickTime + (frame.playing ? (long) (TickUtils.getPartialTickTime() * tickTime) : 0);
							if (player.mediaPlayer().status().isSeekable() && newTime > player.mediaPlayer().status().length())
								if (frame.loop)
									newTime %= player.mediaPlayer().status().length();
								
							player.mediaPlayer().controls().setTime(newTime);
							boolean playing = frame.playing && !Minecraft.getMinecraft().isGamePaused();
							if (player.mediaPlayer().status().isPlaying() != playing)
								player.mediaPlayer().controls().setPause(!playing);
						});
				}
			}
		}
	}
	
	@Override
	public void release() {
		player.mediaPlayer().submit(() -> player.release());
	}
	
	@Override
	public int texture() {
		return texture;
	}
	
	@Override
	public void pause(TileEntityCreativeFrame frame) {
		player.mediaPlayer().submit(() -> {
			player.mediaPlayer().controls().setTime(frame.tick * 50);
			player.mediaPlayer().controls().pause();
		});
	}
	
	@Override
	public void resume(TileEntityCreativeFrame frame) {
		player.mediaPlayer().submit(() -> {
			player.mediaPlayer().controls().setTime(frame.tick * 50);
			player.mediaPlayer().controls().play();
		});
	}
	
	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public int getHeight() {
		return height;
	}
	
}
