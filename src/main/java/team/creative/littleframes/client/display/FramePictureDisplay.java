package team.creative.littleframes.client.display;

import me.srrapero720.watermedia.api.WaterMediaAPI;
import me.srrapero720.watermedia.api.image.ImageCache;
import me.srrapero720.watermedia.api.image.ImageRenderer;
import team.creative.creativecore.client.CreativeCoreClient;

public class FramePictureDisplay extends FrameDisplay {
    public static final FrameDisplay VLC_FAILED = new FramePictureDisplay(WaterMediaAPI.img_getFailedVLC());
    
    public final ImageRenderer image;
    public final ImageCache cache;
    private int textureId;
    
    public FramePictureDisplay(ImageRenderer renderer) {
        this.cache = null;
        this.image = renderer;
    }
    
    public FramePictureDisplay(ImageCache cache) {
        this.cache = cache;
        this.image = this.cache.getRenderer();
    }
    
    @Override
    public void prepare(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        long time = WaterMediaAPI.math_ticksToMillis(tick) + (playing ? (long) (CreativeCoreClient.getFrameTime() * 50) : 0);
        long duration = cache.getRenderer().duration;
        if ((duration > 0) && (time > duration) && loop)
            time %= duration;
        textureId = cache.getRenderer().texture(time);
    }
    
    @Override
    public void tick(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}
    
    @Override
    public void pause(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}
    
    @Override
    public void resume(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}
    
    @Override
    public int texture() {
        return textureId;
    }
    
    @Override
    public void release() {
        if (cache != null)
            cache.deuse();
    }
    
    @Override
    public int getWidth() {
        return image.width;
    }
    
    @Override
    public int getHeight() {
        return image.height;
    }
    
    @Override
    public boolean canTick() {
        return true;
    }
}