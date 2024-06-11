package team.creative.littleframes.client.display;

import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageCache;
import me.srrapero720.watermedia.api.image.ImageRenderer;
import team.creative.creativecore.client.CreativeCoreClient;

public class FramePictureDisplay extends FrameDisplay {
    public static final FrameDisplay VLC_FAILED = new FramePictureDisplay(ImageAPI.failedVLC());

    public final ImageRenderer image;
    public final ImageCache cache;

    public FramePictureDisplay(ImageRenderer renderer) {
        this.cache = null;
        this.image = renderer;
    }

    public FramePictureDisplay(ImageCache cache) {
        this.cache = cache;
        this.image = this.cache.getRenderer();
    }

    @Override
    public int prepare(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        return image.texture(tick, playing ? CreativeCoreClient.getFrameTime() : 0, loop);
    }

    @Override
    public void tick(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}

    @Override
    public void pause(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}

    @Override
    public void resume(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}

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