package team.creative.littleframes.client.display;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.locks.ReentrantLock;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import team.creative.creativecore.client.CreativeCoreClient;
import team.creative.littleframes.client.texture.TextureCache;
import team.creative.littleframes.client.vlc.VLCDiscovery;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;

public class FrameVideoDisplay extends FrameDisplay {
    
    private static final String VLC_DOWNLOAD_32 = "https://i.imgur.com/qDIb9iV.png";
    private static final String VLC_DOWNLOAD_64 = "https://i.imgur.com/3EKo7Jx.png";
    private static final int ACCEPTABLE_SYNC_TIME = 1000;
    
    public static FrameDisplay createVideoDisplay(String url, float volume, boolean loop) {
        if (VLCDiscovery.load())
            return new FrameVideoDisplay(url, volume, loop);
        String failURL = System.getProperty("sun.arch.data.model").equals("32") ? VLC_DOWNLOAD_32 : VLC_DOWNLOAD_64;
        TextureCache cache = TextureCache.get(failURL);
        if (cache.ready())
            return cache.createDisplay(failURL, volume, loop, true);
        return null;
    }
    
    public int width = 1;
    public int height = 1;
    public CallbackMediaPlayerComponent player;
    
    private IntBuffer buffer;
    public int texture;
    private boolean stream = false;
    private float lastSetVolume;
    private boolean needsUpdate = false;
    private ReentrantLock lock = new ReentrantLock();
    private boolean first = true;
    private long lastCorrectedTime = Long.MIN_VALUE;
    
    public FrameVideoDisplay(String url, float volume, boolean loop) {
        super();
        texture = GlStateManager._genTexture();
        
        player = new CallbackMediaPlayerComponent(VLCDiscovery.factory, null, null, false, new RenderCallback() {
            
            @Override
            public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
                lock.lock();
                buffer.rewind();
                buffer.put(nativeBuffers[0].asIntBuffer());
                buffer.rewind();
                needsUpdate = true;
                lock.unlock();
            }
        }, new BufferFormatCallback() {
            
            @Override
            public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
                synchronized (FrameVideoDisplay.this) {
                    FrameVideoDisplay.this.width = sourceWidth;
                    FrameVideoDisplay.this.height = sourceHeight;
                    FrameVideoDisplay.this.first = true;
                }
                lock.lock();
                buffer = MemoryTracker.create(sourceWidth * sourceHeight * 4).asIntBuffer();
                needsUpdate = true;
                lock.unlock();
                return new BufferFormat("RGBA", sourceWidth, sourceHeight, new int[] { sourceWidth * 4 }, new int[] { sourceHeight });
            }
            
            @Override
            public void allocatedBuffers(ByteBuffer[] buffers) {}
            
        }, null);
        //player.mediaPlayer().submit(() -> {
        player.mediaPlayer().audio().setVolume((int) (volume * 100F));
        lastSetVolume = volume;
        player.mediaPlayer().controls().setRepeat(loop);
        player.mediaPlayer().media().start(url);
        //});
    }
    
    @Override
    public void prepare(String url, float volume, boolean playing, boolean loop, int tick) {
        if (player == null)
            return;
        lock.lock();
        if (needsUpdate) {
            RenderSystem.bindTexture(texture);
            synchronized (this) {
                if (buffer != null && first) {
                    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
                    first = false;
                } else
                    GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
            }
            needsUpdate = false;
        }
        lock.unlock();
        if (player.mediaPlayer().media().isValid()) {
            boolean realPlaying = playing && !Minecraft.getInstance().isPaused();
            
            if (volume != lastSetVolume) {
                player.mediaPlayer().audio().setVolume((int) (volume * 100F)); //player.mediaPlayer().submit(() -> player.mediaPlayer().audio().setVolume((int) (volume * 100F)));
                lastSetVolume = volume;
            }
            if (player.mediaPlayer().controls().getRepeat() != loop)
                player.mediaPlayer().controls().setRepeat(loop); // player.mediaPlayer().submit(() -> player.mediaPlayer().controls().setRepeat(loop));
            long tickTime = 50;
            long newDuration = player.mediaPlayer().status().length();
            if (!stream && newDuration != -1 && newDuration != 0 && player.mediaPlayer().media().info().duration() == 0)
                stream = true;
            if (stream) {
                if (player.mediaPlayer().status().isPlaying() != realPlaying)
                    player.mediaPlayer().controls().setPause(!realPlaying); // player.mediaPlayer().submit(() -> player.mediaPlayer().controls().setPause(!realPlaying));
            } else {
                if (player.mediaPlayer().status().length() > 0) {
                    if (player.mediaPlayer().status().isPlaying() != realPlaying)
                        player.mediaPlayer().controls().setPause(!realPlaying);
                    
                    long time = tick * tickTime + (realPlaying ? (long) (CreativeCoreClient.getFrameTime() * tickTime) : 0);
                    if (player.mediaPlayer().status().isSeekable() && time > player.mediaPlayer().status().time())
                        if (loop)
                            time %= player.mediaPlayer().status().length();
                    if (Math.abs(time - player.mediaPlayer().status().time()) > ACCEPTABLE_SYNC_TIME && Math.abs(time - lastCorrectedTime) > ACCEPTABLE_SYNC_TIME) {
                        long newTime = tick * tickTime + (realPlaying ? (long) (CreativeCoreClient.getFrameTime() * tickTime) : 0);
                        if (player.mediaPlayer().status().isSeekable() && newTime > player.mediaPlayer().status().length())
                            if (loop)
                                newTime %= player.mediaPlayer().status().length();
                        lastCorrectedTime = newTime;
                        player.mediaPlayer().controls().setTime(newTime);
                        
                    }
                }
            }
        }
    }
    
    @Override
    public void release() {
        player.mediaPlayer().release();
        player = null;
    }
    
    @Override
    public int texture() {
        return texture;
    }
    
    @Override
    public void pause(String url, float volume, boolean playing, boolean loop, int tick) {
        //player.mediaPlayer().submit(() -> {
        player.mediaPlayer().controls().setTime(tick * 50);
        player.mediaPlayer().controls().pause();
        //});
    }
    
    @Override
    public void resume(String url, float volume, boolean playing, boolean loop, int tick) {
        //player.mediaPlayer().submit(() -> {
        player.mediaPlayer().controls().setTime(tick * 50);
        player.mediaPlayer().controls().play();
        //});
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
