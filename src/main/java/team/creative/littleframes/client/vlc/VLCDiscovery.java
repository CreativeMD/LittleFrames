package team.creative.littleframes.client.vlc;

import team.creative.littleframes.LittleFrames;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;

public class VLCDiscovery {
    
    private static volatile boolean loaded = false;
    private static volatile boolean startedLoading = false;
    private static volatile boolean successful = false;
    private static volatile NativeDiscovery discovery;
    public static volatile MediaPlayerFactory factory;
    
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
            if (successful) {
                factory = new MediaPlayerFactory("--quiet");
                LittleFrames.LOGGER.info("Loaded VLC in '{}'", discovery.discoveredPath());
                Runtime.getRuntime().addShutdownHook(new Thread(() -> factory.release()));
            } else
                LittleFrames.LOGGER.info("Failed to load VLC");
        } catch (Exception e) {
            e.printStackTrace();
            loaded = true;
            successful = false;
            LittleFrames.LOGGER.error("Failed to load VLC");
        }
        return successful;
    }
    
}
