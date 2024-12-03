package team.creative.littleframes.client.display;

import org.watermedia.api.image.ImageAPI;
import org.watermedia.api.image.ImageCache;
import org.watermedia.api.image.ImageRenderer;
import org.watermedia.api.math.MathAPI;

import team.creative.creativecore.client.CreativeCoreClient;
import team.creative.littleframes.common.data.LittleFrameData;

public class FramePictureDisplay extends FrameDisplay {
    public static final FrameDisplay VLC_FAILED = new FramePictureDisplay(ImageAPI.failedVLC());
    
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
    public void prepare(LittleFrameData data, boolean playing) {
        long time = MathAPI.tickToMs(data.tick) + (playing ? (long) (CreativeCoreClient.getFrameTime() * 50) : 0);
        long duration = image.duration;
        if ((duration > 0) && (time > duration) && data.loop)
            time %= duration;
        textureId = image.texture(time);
    }
    
    @Override
    public void tick(LittleFrameData data, boolean playing) {}
    
    @Override
    public void pause(LittleFrameData data, boolean playing) {}
    
    @Override
    public void resume(LittleFrameData data, boolean playing) {}
    
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