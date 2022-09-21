package team.creative.littleframes.client.vlc;

import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;

public class VLCLoader extends Thread {
    
    private CallbackMediaPlayerComponent player;
    private final RenderCallback renderCallback;
    private final BufferFormatCallback bufferCallback;
    private volatile boolean done;
    
    public VLCLoader(RenderCallback renderCallback, BufferFormatCallback bufferCallback) {
        this.renderCallback = renderCallback;
        this.bufferCallback = bufferCallback;
        start();
    }
    
    @Override
    public void run() {
        if (VLCDiscovery.load())
            player = new CallbackMediaPlayerComponent(VLCDiscovery.factory, null, null, false, renderCallback, bufferCallback, null);
        done = true;
    }
    
    public CallbackMediaPlayerComponent getPlayer() {
        return player;
    }
    
    public boolean finished() {
        return done;
    }
    
}
