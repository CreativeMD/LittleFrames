package team.creative.littleframes.client.display;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.common.utils.mc.TickUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import team.creative.littleframes.client.texture.TextureCache;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;

public class FrameVideoDisplay extends FrameDisplay {
    
    private static HashSet<Runnable> toBeRun = new HashSet<>();
    private static final String VLC_DOWNLOAD_32 = "https://i.imgur.com/qDIb9iV.png";
    private static final String VLC_DOWNLOAD_64 = "https://i.imgur.com/3EKo7Jx.png";
    private static final int ACCEPTABLE_SYNC_TIME = 1000;
    private static boolean isVLCInstalled = true;
    
    public static FrameDisplay createVideoDisplay(String url, float volume, boolean loop) {
        try {
            if (isVLCInstalled)
                return new FrameVideoDisplay(url, volume, loop);
        } catch (Exception | UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
        isVLCInstalled = false;
        String failURL = System.getProperty("sun.arch.data.model").equals("32") ? VLC_DOWNLOAD_32 : VLC_DOWNLOAD_64;
        TextureCache cache = TextureCache.get(failURL);
        if (cache.ready())
            return cache.createDisplay(failURL, volume, loop);
        return null;
    }
    
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
    
    public FrameVideoDisplay(String url, float volume, boolean loop) {
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
            player.mediaPlayer().audio().setVolume((int) (volume * 100F));
            lastSetVolume = volume;
            player.mediaPlayer().controls().setRepeat(loop);
            player.mediaPlayer().media().start(url);
        });
    }
    
    @Override
    public void prepare(String url, float volume, boolean playing, boolean loop, int tick) {
        synchronized (this) {
            if (needsUpdate.getAndSet(false)) {
                if (buffer != null && first) {
                    GlStateManager.pushMatrix();
                    GlStateManager.bindTexture(texture);
                    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
                    GlStateManager.popMatrix();
                    first = false;
                } else {
                    GlStateManager.pushMatrix();
                    GlStateManager.bindTexture(texture);
                    GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
                    GlStateManager.popMatrix();
                }
            }
        }
        if (player.mediaPlayer().media().isValid()) {
            boolean realPlaying = playing && !Minecraft.getMinecraft().isGamePaused();
            
            if (volume != lastSetVolume) {
                player.mediaPlayer().submit(() -> player.mediaPlayer().audio().setVolume((int) (volume * 100F)));
                lastSetVolume = volume;
            }
            if (player.mediaPlayer().controls().getRepeat() != loop)
                player.mediaPlayer().submit(() -> player.mediaPlayer().controls().setRepeat(loop));
            long tickTime = 50;
            long newDuration = player.mediaPlayer().status().length();
            if (!stream && durationBefore != 0 && durationBefore != -1 && newDuration != 0 && newDuration != -1 && player.mediaPlayer().status().length() != durationBefore) // if duration changes it's a stream and should not be synced
                stream = true;
            durationBefore = player.mediaPlayer().status().length();
            if (stream) {
                if (player.mediaPlayer().status().isPlaying() != realPlaying)
                    player.mediaPlayer().submit(() -> player.mediaPlayer().controls().setPause(!realPlaying));
            } else {
                if (player.mediaPlayer().status().length() > 0) {
                    long time = tick * tickTime + (realPlaying ? (long) (TickUtils.getPartialTickTime() * tickTime) : 0);
                    if (player.mediaPlayer().status().isSeekable() && time > player.mediaPlayer().status().time())
                        if (loop)
                            time %= player.mediaPlayer().status().length();
                    if (Math.abs(time - player.mediaPlayer().status().time()) > ACCEPTABLE_SYNC_TIME)
                        player.mediaPlayer().submit(() -> {
                            long newTime = tick * tickTime + (realPlaying ? (long) (TickUtils.getPartialTickTime() * tickTime) : 0);
                            if (player.mediaPlayer().status().isSeekable() && newTime > player.mediaPlayer().status().length())
                                if (loop)
                                    newTime %= player.mediaPlayer().status().length();
                                
                            player.mediaPlayer().controls().setTime(newTime);
                            if (player.mediaPlayer().status().isPlaying() != realPlaying)
                                player.mediaPlayer().controls().setPause(!realPlaying);
                        });
                }
            }
        }
    }
    
    @Override
    public void release() {
        Runnable run = new Runnable() {
            
            @Override
            public void run() {
                player.release();
                synchronized (toBeRun) {
                    toBeRun.remove(this);
                }
            }
        };
        
        synchronized (toBeRun) {
            toBeRun.add(run);
        }
        player.mediaPlayer().submit(run);
    }
    
    @Override
    public int texture() {
        return texture;
    }
    
    @Override
    public void pause(String url, float volume, boolean playing, boolean loop, int tick) {
        player.mediaPlayer().submit(() -> {
            player.mediaPlayer().controls().setTime(tick * 50);
            player.mediaPlayer().controls().pause();
        });
    }
    
    @Override
    public void resume(String url, float volume, boolean playing, boolean loop, int tick) {
        player.mediaPlayer().submit(() -> {
            player.mediaPlayer().controls().setTime(tick * 50);
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
