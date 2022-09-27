package team.creative.littleframes.client.display;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import team.creative.creativecore.client.CreativeCoreClient;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littleframes.client.texture.TextureCache;
import team.creative.littleframes.client.vlc.VLCDiscovery;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;

public class FrameVideoDisplay extends FrameDisplay {
    
    private static final String VLC_DOWNLOAD_32 = "https://i.imgur.com/VF3LuvM.png";
    private static final String VLC_DOWNLOAD_64 = "https://i.imgur.com/2aN8ZQC.png";
    private static final int ACCEPTABLE_SYNC_TIME = 1000;
    
    private static final List<FrameVideoDisplay> OPEN_DISPLAYS = new ArrayList<>();
    
    public static void tick() {
        synchronized (OPEN_DISPLAYS) {
            for (FrameVideoDisplay display : OPEN_DISPLAYS) {
                if (Minecraft.getInstance().isPaused())
                    if (display.stream) {
                        if (display.player.mediaPlayer().status().isPlaying())
                            display.player.mediaPlayer().controls().setPause(true);
                    } else if (display.player.mediaPlayer().status().length() > 0 && display.player.mediaPlayer().status().isPlaying())
                        display.player.mediaPlayer().controls().setPause(true);
            }
        }
    }
    
    public static void unload() {
        synchronized (OPEN_DISPLAYS) {
            for (FrameVideoDisplay display : OPEN_DISPLAYS)
                display.free();
            OPEN_DISPLAYS.clear();
        }
    }
    
    public static FrameDisplay createVideoDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop) {
        if (VLCDiscovery.isLoadedOrRequest()) {
            if (VLCDiscovery.isAvailable()) {
                FrameVideoDisplay display = new FrameVideoDisplay(pos, url, volume, minDistance, maxDistance, loop);
                OPEN_DISPLAYS.add(display);
                return display;
            }
        } else
            return null;
        String failURL = System.getProperty("sun.arch.data.model").equals("32") ? VLC_DOWNLOAD_32 : VLC_DOWNLOAD_64;
        TextureCache cache = TextureCache.get(failURL);
        if (cache.ready())
            return cache.createDisplay(pos, failURL, volume, minDistance, maxDistance, loop, true);
        return null;
    }
    
    public volatile int width = 1;
    public volatile int height = 1;
    
    public CallbackMediaPlayerComponent player;
    public MediaPlayerFactory factory;
    
    private final Vec3d pos;
    private volatile IntBuffer buffer;
    public int texture;
    private boolean stream = false;
    private float lastSetVolume;
    private volatile boolean needsUpdate = false;
    private ReentrantLock lock = new ReentrantLock();
    private volatile boolean first = true;
    private long lastCorrectedTime = Long.MIN_VALUE;
    
    public FrameVideoDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop) {
        super();
        this.pos = pos;
        texture = GlStateManager._genTexture();
        
        player = new CallbackMediaPlayerComponent(VLCDiscovery.factory, null, null, false, new RenderCallback() {
            
            @Override
            public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
                lock.lock();
                try {
                    buffer.put(nativeBuffers[0].asIntBuffer());
                    buffer.rewind();
                    needsUpdate = true;
                } finally {
                    lock.unlock();
                }
            }
        }, new BufferFormatCallback() {
            
            @Override
            public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
                lock.lock();
                try {
                    FrameVideoDisplay.this.width = sourceWidth;
                    FrameVideoDisplay.this.height = sourceHeight;
                    FrameVideoDisplay.this.first = true;
                    buffer = MemoryTracker.create(sourceWidth * sourceHeight * 4).asIntBuffer();
                    needsUpdate = true;
                } finally {
                    lock.unlock();
                }
                return new BufferFormat("RGBA", sourceWidth, sourceHeight, new int[] { sourceWidth * 4 }, new int[] { sourceHeight });
            }
            
            @Override
            public void allocatedBuffers(ByteBuffer[] buffers) {}
            
        }, null);
        volume = getVolume(volume, minDistance, maxDistance);
        player.mediaPlayer().audio().setVolume((int) volume);
        lastSetVolume = volume;
        player.mediaPlayer().controls().setRepeat(loop);
        player.mediaPlayer().media().start(url);
        
    }
    
    public int getVolume(float volume, float minDistance, float maxDistance) {
        if (player == null)
            return 0;
        float distance = (float) pos.distance(Minecraft.getInstance().player.getPosition(CreativeCoreClient.getFrameTime()));
        if (minDistance > maxDistance) {
            float temp = maxDistance;
            maxDistance = minDistance;
            minDistance = temp;
        }
        
        if (distance > minDistance)
            if (distance > maxDistance)
                volume = 0;
            else
                volume *= 1 - ((distance - minDistance) / (maxDistance - minDistance));
        return (int) (volume * 100F);
    }
    
    @Override
    public void tick(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        if (player == null)
            return;
        
        volume = getVolume(volume, minDistance, maxDistance);
        if (volume != lastSetVolume) {
            player.mediaPlayer().audio().setVolume((int) volume);
            lastSetVolume = volume;
        }
        
        if (player.mediaPlayer().media().isValid()) {
            boolean realPlaying = playing && !Minecraft.getInstance().isPaused();
            
            if (player.mediaPlayer().controls().getRepeat() != loop)
                player.mediaPlayer().controls().setRepeat(loop);
            long tickTime = 50;
            long newDuration = player.mediaPlayer().status().length();
            if (!stream && newDuration != -1 && newDuration != 0 && player.mediaPlayer().media().info().duration() == 0)
                stream = true;
            if (stream) {
                if (player.mediaPlayer().status().isPlaying() != realPlaying)
                    player.mediaPlayer().controls().setPause(!realPlaying);
            } else {
                if (player.mediaPlayer().status().length() > 0) {
                    if (player.mediaPlayer().status().isPlaying() != realPlaying)
                        player.mediaPlayer().controls().setPause(!realPlaying);
                    
                    long time = tick * tickTime + (realPlaying ? (long) (CreativeCoreClient.getFrameTime() * tickTime) : 0);
                    if (time > player.mediaPlayer().status().time() && loop)
                        time %= player.mediaPlayer().status().length();
                    if (Math.abs(time - player.mediaPlayer().status().time()) > ACCEPTABLE_SYNC_TIME && Math.abs(time - lastCorrectedTime) > ACCEPTABLE_SYNC_TIME) {
                        lastCorrectedTime = time;
                        player.mediaPlayer().controls().setTime(time);
                    }
                }
            }
        }
    }
    
    @Override
    public void prepare(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        if (player == null)
            return;
        lock.lock();
        try {
            if (needsUpdate) {
                // fixes random crash, when values are too high it causes a jvm crash, caused weird behavior when game is paused
                GlStateManager._pixelStore(3314, 0);
                GlStateManager._pixelStore(3316, 0);
                GlStateManager._pixelStore(3315, 0);
                RenderSystem.bindTexture(texture);
                if (first) {
                    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
                    first = false;
                } else
                    GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
                needsUpdate = false;
            }
        } finally {
            lock.unlock();
        }
        
    }
    
    public void free() {
        if (player != null)
            player.mediaPlayer().release();
        if (texture != -1) {
            GlStateManager._deleteTexture(texture);
            texture = -1;
        }
        player = null;
    }
    
    @Override
    public void release() {
        free();
        synchronized (OPEN_DISPLAYS) {
            OPEN_DISPLAYS.remove(this);
        }
    }
    
    @Override
    public int texture() {
        return texture;
    }
    
    @Override
    public void pause(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        if (player == null)
            return;
        player.mediaPlayer().controls().setTime(tick * 50);
        player.mediaPlayer().controls().pause();
    }
    
    @Override
    public void resume(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        if (player == null)
            return;
        player.mediaPlayer().controls().setTime(tick * 50);
        player.mediaPlayer().controls().play();
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
