package team.creative.littleframes.client.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import me.srrapero720.watermedia.api.WaterMediaAPI;
import me.srrapero720.watermedia.api.external.GifDecoder;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littleframes.LittleFrames;
import team.creative.littleframes.client.display.FrameDisplay;
import team.creative.littleframes.client.display.FramePictureDisplay;
import team.creative.littleframes.client.display.FrameVideoDisplay;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;

public class TextureCache {
    
    private static final HashMap<String, TextureCache> cached = new HashMap<>();
    
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
    
    @SubscribeEvent
    public static void render(ClientTickEvent event) {
        if (event.phase == Phase.START)
            FrameVideoDisplay.tick();
    }
    
    public static void reloadAll() {
        for (TextureCache cache : cached.values())
            cache.reload();
    }
    
    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            for (TextureCache cache : cached.values())
                cache.remove();
            cached.clear();
            FrameVideoDisplay.unload();
        }
    }
    
    public static TextureCache get(String url) {
        TextureCache cache = cached.get(url);
        if (cache != null) {
            cache.use();
            return cache;
        }
        cache = new TextureCache(url);
        cached.put(url, cache);
        return cache;
    }
    
    public final String url;
    private int[] textures;
    private int width;
    private int height;
    private long[] delay;
    private long duration;
    private boolean isVideo;
    private final boolean canSeek;
    
    private TextureSeeker seeker;
    private boolean ready = false;
    private String error;
    
    private int usage = 0;
    
    private GifDecoder decoder;
    private int remaining;
    
    public TextureCache(String url) {
        this.url = url;
        this.canSeek = true;
        use();
        trySeek();
    }

    // Tweak between WaterMedia and LittleFrames
    public TextureCache(BufferedImage image) {
        this.url = null;
        this.canSeek = false;
        process(image);
    }

    // Tweak between WaterMedia and LittleFrames
    public TextureCache(GifDecoder image) {
        this.url = null;
        this.canSeek = false;
        process(image);
    }
    
    private void trySeek() {
        if (seeker != null || !canSeek || url == null || url.isEmpty()) return;
        synchronized (TextureSeeker.LOCK) {
            if (TextureSeeker.activeDownloads < TextureSeeker.MAXIMUM_ACTIVE_DOWNLOADS)
                this.seeker = new TextureSeeker(this);
        }
    }
    
    private int getTexture(int index) {
        if (textures[index] == -1 && decoder != null) {
            textures[index] = WaterMediaAPI.preRender(decoder.getFrame(index), width, height);
            remaining--;
            if (remaining <= 0)
                decoder = null;
        }
        return textures[index];
    }
    
    public int getTexture(long time) {
        if (textures == null)
            return -1;
        if (textures.length == 1)
            return getTexture(0);
        int last = getTexture(0);
        for (int i = 1; i < delay.length; i++) {
            if (delay[i] > time)
                break;
            last = getTexture(i);
        }
        return last;
    }
    
    public FrameDisplay createDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop) {
        return createDisplay(pos, url, volume, minDistance, maxDistance, loop, false);
    }
    
    public FrameDisplay createDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop, boolean noVideo) {
        volume *= Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
        if (textures == null && !noVideo && LittleFrames.CONFIG.useVLC)
            return FrameVideoDisplay.createVideoDisplay(pos, url, volume, minDistance, maxDistance, loop);
        return new FramePictureDisplay(this);
    }
    
    public String getError() {
        return error;
    }
    
    public void processVideo() {
        this.textures = null;
        this.error = null;
        this.isVideo = true;
        this.ready = true;
        this.seeker = null;
    }
    
    public void processFailed(String error) {
        this.textures = null;
        this.error = error;
        this.ready = true;
        this.seeker = null;
    }
    
    public void process(BufferedImage image) {
        width = image.getWidth();
        height = image.getHeight();
        textures = new int[] { WaterMediaAPI.preRender(image, width, height) };
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
        
        this.decoder = decoder;
        this.remaining = decoder.getFrameCount();
        long time = 0;
        for (int i = 0; i < decoder.getFrameCount(); i++) {
            textures[i] = -1;
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
    
    public boolean isVideo() {
        return isVideo;
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
    }
    
    public boolean isUsed() {
        return usage > 0;
    }
    
    public void remove() {
        if (!canSeek) return; // If can't seek then DO NOT delete texture...
        ready = false;
        if (textures != null)
            for (int i = 0; i < textures.length; i++)
                GlStateManager._deleteTexture(textures[i]);
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

}
