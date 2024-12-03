package team.creative.littleframes.client.display;

import java.util.ArrayList;
import java.util.List;

import org.watermedia.api.math.MathAPI;
import org.watermedia.api.player.PlayerAPI;
import org.watermedia.api.player.videolan.VideoPlayer;

import net.minecraft.client.Minecraft;
import team.creative.creativecore.client.CreativeCoreClient;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littleframes.common.data.LittleFrameData;

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
    
    public static FrameDisplay createVideoDisplay(Vec3d pos, LittleFrameData data) {
        if (PlayerAPI.isReady()) {
            FrameVideoDisplay display = new FrameVideoDisplay(pos, data);
            OPEN_DISPLAYS.add(display);
            return display;
        } else {
            return FramePictureDisplay.VLC_FAILED;
        }
    }
    
    public VideoPlayer player;
    
    private final Vec3d pos;
    private boolean stream = false;
    private volatile float lastSetVolume;
    private long lastCorrectedTime = Long.MIN_VALUE;
    
    public FrameVideoDisplay(Vec3d pos, LittleFrameData data) {
        super();
        this.pos = pos;
        
        player = new VideoPlayer(Minecraft.getInstance());
        float tempVolume = getVolume(data);
        player.setVolume((int) tempVolume);
        lastSetVolume = tempVolume;
        player.setRepeatMode(data.loop);
        player.start(data.getURI()); // <-- this method is ASYNC. doesn't need a new thread
    }
    
    public int getVolume(LittleFrameData data) {
        if (player == null)
            return 0;
        float distance = (float) pos.distance(Minecraft.getInstance().player.getPosition(CreativeCoreClient.getFrameTime()));
        if (data.minDistance > data.maxDistance) {
            float temp = data.maxDistance;
            data.maxDistance = data.minDistance;
            data.minDistance = temp;
        }
        
        float volume = data.ingameVolume();
        if (distance > data.minDistance)
            if (distance > data.maxDistance)
                volume = 0;
            else
                volume *= 1 - ((distance - data.minDistance) / (data.maxDistance - data.minDistance));
        return (int) (volume * 100F);
    }
    
    @Override
    public void tick(LittleFrameData data, boolean playing) {
        if (player == null)
            return;
        
        float volume = getVolume(data);
        if (volume != lastSetVolume) {
            player.setVolume((int) volume);
            lastSetVolume = volume;
        }
        
        if (player.isValid()) {
            boolean realPlaying = playing && !Minecraft.getInstance().isPaused();
            
            if (player.getRepeatMode() != data.loop)
                player.setRepeatMode(data.loop);
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
                        long time = data.tick * tickTime + (realPlaying ? (long) (CreativeCoreClient.getFrameTime() * tickTime) : 0);
                        if (time > player.getTime() && data.loop) {
                            long mediaDuration = player.getMediaInfoDuration();
                            time = (time == 0 || mediaDuration == 0) ? 0 : Math.floorMod(time, player.getMediaInfoDuration());
                        }
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
    public void prepare(LittleFrameData data, boolean playing) {
        if (player == null)
            return;
        player.preRender();
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
    public int texture() {
        return player.texture();
    }
    
    @Override
    public void pause(LittleFrameData data, boolean playing) {
        if (player == null)
            return;
        player.seekTo(MathAPI.tickToMs(data.tick));
        player.pause();
    }
    
    @Override
    public void resume(LittleFrameData data, boolean playing) {
        if (player == null)
            return;
        player.seekTo(MathAPI.tickToMs(data.tick));
        player.play();
    }
    
    @Override
    public int getWidth() {
        return player.width();
    }
    
    @Override
    public int getHeight() {
        return player.height();
    }
    
    @Override
    public boolean canTick() {
        return (player != null && player.isSafeUse());
    }
}