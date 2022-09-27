package team.creative.littleframes.client.vlc;

import team.creative.littleframes.LittleFrames;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;

public class VLCDiscovery {
    
    private static volatile boolean loaded = false;
    private static volatile boolean startedLoading = false;
    private static volatile boolean successful = false;
    private static volatile NativeDiscovery discovery;
    
    public static boolean isLoaded() {
        return loaded;
    }
    
    public static boolean isLoadedOrRequest() {
        if (loaded)
            return true;
        if (!startedLoading) {
            startedLoading = true;
            new Thread(() -> load()).start();
        }
        return false;
    }
    
    public static boolean isAvailable() {
        return successful;
    }
    
    public static synchronized boolean load() {
        if (loaded)
            return successful;
        try {
            discovery = new NativeDiscovery(new LinuxNativeDiscoveryStrategyFixed(), new MacOsNativeDiscoveryStrategyFixed(), new WindowsNativeDiscoveryStrategyFixed());
            
            successful = discovery.discover();
            loaded = true;
            if (successful)
                LittleFrames.LOGGER.info("Loaded VLC in '{}'", discovery.discoveredPath());
            else
                LittleFrames.LOGGER.info("Failed to load VLC");
        } catch (Exception e) {
            e.printStackTrace();
            loaded = true;
            successful = false;
            LittleFrames.LOGGER.error("Failed to load VLC");
        }
        return successful;
    }
    
    public static VLCLoader createLoader(RenderCallback renderCallback, BufferFormatCallback bufferCallback) {
        return new VLCLoader(renderCallback, bufferCallback);
    }
    
}
