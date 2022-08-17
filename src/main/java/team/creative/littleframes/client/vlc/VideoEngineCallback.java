package team.creative.littleframes.client.vlc;

import org.lwjgl.glfw.GLFW;

import com.sun.jna.Pointer;

import uk.co.caprica.vlcj.player.embedded.videosurface.videoengine.VideoEngineCallbackAdapter;

public class VideoEngineCallback extends VideoEngineCallbackAdapter {
    
    @Override
    public void onSwap(Pointer opaque) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public boolean onMakeCurrent(Pointer opaque, boolean enter) {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public long onGetProcAddress(Pointer opaque, String functionName) {
        return GLFW.glfwGetProcAddress(functionName);
    }
    
}
