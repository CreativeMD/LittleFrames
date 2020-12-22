package team.creative.littleframes.client.texture;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.madgag.gif.fmsware.GifDecoder;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import team.creative.littleframes.block.TileEntityCreativeFrame;
import team.creative.littleframes.client.display.FrameDisplay;
import team.creative.littleframes.client.display.FramePictureDisplay;
import team.creative.littleframes.client.display.FrameVideoDisplay;

public class TextureCache {
	
	private static HashMap<String, TextureCache> cached = new HashMap<>();
	private static boolean changed = false;
	
	@SubscribeEvent
	public static void render(RenderTickEvent event) {
		if (event.phase == Phase.START) {
			for (Iterator<TextureCache> iterator = cached.values().iterator(); iterator.hasNext();) {
				TextureCache type = iterator.next();
				if (!type.isUsed()) {
					type.remove();
					iterator.remove();
				}
			}
		}
	}
	
	public static void reloadAll() {
		for (TextureCache cache : cached.values())
			cache.reload();
	}
	
	@SubscribeEvent
	public static void unload(WorldEvent.Unload event) {
		if (event.getWorld().isRemote) {
			for (TextureCache cache : cached.values())
				cache.remove();
			cached.clear();
		}
	}
	
	public static TextureCache get(String url) {
		TextureCache cache = cached.get(url);
		if (cache != null) {
			cache.use();
			return cache;
		}
		cache = new TextureCache(url);
		return cache;
	}
	
	public final String url;
	private int[] textures;
	private int width;
	private int height;
	private long[] delay;
	private long duration;
	
	private TextureSeeker seeker;
	private boolean ready = false;
	private String error;
	
	private int usage = 0;
	
	public TextureCache(String url) {
		this.url = url;
		use();
		trySeek();
	}
	
	private void trySeek() {
		if (seeker != null)
			return;
		synchronized (TextureSeeker.LOCK) {
			if (TextureSeeker.activeDownloads < TextureSeeker.MAXIMUM_ACTIVE_DOWNLOADS)
				this.seeker = new TextureSeeker(this);
		}
	}
	
	public int getTexture(long time) {
		if (textures.length == 1)
			return textures[0];
		int last = textures[0];
		for (int i = 1; i < delay.length; i++) {
			if (delay[i] > time)
				break;
			last = textures[i];
		}
		return last;
	}
	
	public FrameDisplay createDisplay(TileEntityCreativeFrame frame) {
		if (textures == null)
			return new FrameVideoDisplay(frame);
		return new FramePictureDisplay(this);
	}
	
	public String getError() {
		return error;
	}
	
	public void processFailed(String error) {
		this.textures = null;
		this.error = error;
		this.ready = true;
	}
	
	public void process(BufferedImage image) {
		width = image.getWidth();
		height = image.getHeight();
		textures = new int[] { uploadFrame(image, width, height) };
		delay = new long[] { 0 };
		duration = 0;
		seeker = null;
		ready = true;
	}
	
	public void process(GifDecoder decoder) {
		Dimension frameSize = decoder.getFrameSize();
		width = (int) frameSize.getWidth();
		height = (int) frameSize.getHeight();
		textures = new int[decoder.getFrameCount()];
		delay = new long[decoder.getFrameCount()];
		
		long time = 0;
		for (int i = 0; i < decoder.getFrameCount(); i++) {
			textures[i] = uploadFrame(decoder.getFrame(i), width, height);
			delay[i] = time;
			time += decoder.getDelay(i);
		}
		duration = time;
		seeker = null;
		ready = true;
	}
	
	public boolean ready() {
		if (ready)
			return true;
		trySeek();
		return false;
	}
	
	public void reload() {
		remove();
		error = null;
		trySeek();
	}
	
	public void use() {
		usage++;
	}
	
	public void unuse() {
		usage--;
		changed = true;
	}
	
	public boolean isUsed() {
		return usage > 0;
	}
	
	public void remove() {
		ready = false;
		if (seeker != null)
			seeker.destroy();
		if (textures != null)
			for (int i = 0; i < textures.length; i++)
				GlStateManager.deleteTexture(textures[i]);
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public long[] getDelay() {
		return delay;
	}
	
	public long getDuration() {
		return duration;
	}
	
	public boolean isAnimated() {
		return textures.length > 1;
	}
	
	public int getFrameCount() {
		return textures.length;
	}
	
	private static int uploadFrame(BufferedImage image, int width, int height) {
		int[] pixels = new int[width * height];
		image.getRGB(0, 0, width, height, pixels, 0, width);
		boolean hasAlpha = false;
		if (image.getColorModel().hasAlpha()) {
			for (int pixel : pixels) {
				if ((pixel >> 24 & 0xFF) < 0xFF) {
					hasAlpha = true;
					break;
				}
			}
		}
		int bytesPerPixel = hasAlpha ? 4 : 3;
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bytesPerPixel);
		for (int pixel : pixels) {
			buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red component
			buffer.put((byte) ((pixel >> 8) & 0xFF)); // Green component
			buffer.put((byte) (pixel & 0xFF)); // Blue component
			if (hasAlpha) {
				buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component. Only for RGBA
			}
		}
		buffer.flip();
		
		int textureID = GL11.glGenTextures(); //Generate texture ID
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID); //Bind texture ID
		
		//Setup wrap mode
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		
		//Setup texture scaling filtering
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		
		if (!hasAlpha)
			GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		
		//Send texel data to OpenGL
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, hasAlpha ? GL11.GL_RGBA8 : GL11.GL_RGB8, width, height, 0, hasAlpha ? GL11.GL_RGBA : GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);
		
		//Return the texture ID so we can bind it later again
		return textureID;
	}
}
