package team.creative.littleframes.client.vlc;

import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import com.sun.jna.NativeLibrary;

import team.creative.creativecore.reflection.ReflectionHelper;
import team.creative.littleframes.LittleFrames;
import uk.co.caprica.vlcj.binding.support.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.factory.discovery.strategy.NativeDiscoveryStrategy;

public class VLCDiscovery {
    
    private static volatile boolean loaded = false;
    private static volatile boolean startedLoading = false;
    private static volatile boolean successful = false;
    private static volatile NativeDiscovery discovery;
    public static volatile MediaPlayerFactory factory;
    private static Field searchPaths;
    private static Field libraries;
    
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
            WindowsNativeDiscoveryStrategyFixed windows = new WindowsNativeDiscoveryStrategyFixed();
            discovery = new NativeDiscovery(new LinuxNativeDiscoveryStrategyFixed(), new MacOsNativeDiscoveryStrategyFixed(), windows) {
                
                @Override
                public boolean attemptFix(String path, NativeDiscoveryStrategy discoveryStrategy) {
                    if (searchPaths == null) {
                        searchPaths = ReflectionHelper.findField(NativeLibrary.class, "searchPaths");
                        libraries = ReflectionHelper.findField(NativeLibrary.class, "libraries");
                    }
                    try {
                        Map<String, Reference<NativeLibrary>> libs = (Map<String, Reference<NativeLibrary>>) libraries.get(null);
                        Map<String, List<String>> paths = (Map<String, List<String>>) searchPaths.get(null);
                        libs.remove(RuntimeUtil.getLibVlcCoreLibraryName());
                        paths.remove(RuntimeUtil.getLibVlcCoreLibraryName());
                        libs.remove(RuntimeUtil.getLibVlcLibraryName());
                        paths.remove(RuntimeUtil.getLibVlcLibraryName());
                        LittleFrames.LOGGER.info("Failed to load VLC in '{}'", path);
                        return true;
                    } catch (IllegalArgumentException | IllegalAccessException e) {}
                    return false;
                }
                
                @Override
                protected void onFailed(String path, NativeDiscoveryStrategy strategy) {
                    LittleFrames.LOGGER.info("Failed to load VLC in '{}' stop searching", path);
                    super.onFailed(path, strategy);
                }
                
                @Override
                protected void onNotFound() {
                    LittleFrames.LOGGER.info("Could not find VLC in any of the given paths");
                    super.onNotFound();
                }
                
            };
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
