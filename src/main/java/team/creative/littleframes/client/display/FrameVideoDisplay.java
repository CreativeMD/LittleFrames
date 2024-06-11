package team.creative.littleframes.client.display;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import me.srrapero720.watermedia.api.math.MathAPI;
import me.srrapero720.watermedia.api.player.PlayerAPI;
import me.srrapero720.watermedia.api.player.SyncVideoPlayer;
import net.minecraft.client.Minecraft;
import team.creative.creativecore.client.CreativeCoreClient;
import team.creative.creativecore.common.util.math.vec.Vec3d;

public class FrameVideoDisplay extends FrameDisplay {
    private static final int ACCEPTABLE_SYNC_TIME = 1000;

    private static final List<FrameVideoDisplay> OPEN_DISPLAYS = new ArrayList<>();

    public static void tick() {
        synchronized (OPEN_DISPLAYS) {
            for (FrameVideoDisplay display : OPEN_DISPLAYS) {
                if (Minecraft.getInstance().isPaused())
                    if (display.stream) {
                        if (display.player.isPlaying())
                            display.player.setPauseMode(true);
                    } else if (display.player.getDuration() > 0 && display.player.isPlaying())
                        display.player.setPauseMode(true);
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
        if (PlayerAPI.isReady()) {
            FrameVideoDisplay display = new FrameVideoDisplay(pos, url, volume, minDistance, maxDistance, loop);
            OPEN_DISPLAYS.add(display);
            return display;
        } else {
            return FramePictureDisplay.VLC_FAILED;
        }
    }

    public SyncVideoPlayer player;

    private final Vec3d pos;
    private boolean stream = false;
    private volatile float lastSetVolume;
    private long lastCorrectedTime = Long.MIN_VALUE;

    public FrameVideoDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop) {
        super();
        this.pos = pos;

        player = new SyncVideoPlayer(Minecraft.getInstance());
        float tempVolume = getVolume(volume, minDistance, maxDistance);
        player.setVolume((int) tempVolume);
        lastSetVolume = tempVolume;
        player.setRepeatMode(loop);
        player.start(url); // <-- this method is ASYNC. doesn't need a new thread
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
            player.setVolume((int) volume);
            lastSetVolume = volume;
        }

        if (player.isValid()) {
            boolean realPlaying = playing && !Minecraft.getInstance().isPaused();

            if (player.getRepeatMode() != loop)
                player.setRepeatMode(loop);
            long tickTime = 50;
            stream = player.isLive();
            if (stream) {
                if (player.isPlaying() != realPlaying)
                    player.setPauseMode(!realPlaying);
            } else {
                if (player.getDuration() > 0) {
                    if (player.isPlaying() != realPlaying)
                        player.setPauseMode(!realPlaying);

                    if (player.isSeekAble()) {
                        long time = tick * tickTime + (realPlaying ? (long) (CreativeCoreClient.getFrameTime() * tickTime) : 0);
                        if (time > player.getTime() && loop)
                            time %= player.getDuration();
                        if (Math.abs(time - player.getTime()) > ACCEPTABLE_SYNC_TIME && Math.abs(time - lastCorrectedTime) > ACCEPTABLE_SYNC_TIME) {
                            lastCorrectedTime = time;
                            player.seekTo(time);
                        }
                    }
                }
            }
        }
    }

    @Override
    public int prepare(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        if (player == null)
            return -1;
        return player.getGlTexture();
    }

    public void free() {
        if (player != null) {
            var tempPlayer = player;
            tempPlayer.release();
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
    public void pause(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        if (player == null)
            return;
        player.seekTo(MathAPI.tickToMs(tick));
        player.pause();
    }

    @Override
    public void resume(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        if (player == null)
            return;
        player.seekTo(MathAPI.tickToMs(tick));
        player.play();
    }

    @Override
    public int getWidth() {
        return player.getWidth();
    }

    @Override
    public int getHeight() {
        return player.getHeight();
    }

    @Override
    public boolean canTick() {
        return (player != null && player.isSafeUse());
    }
}