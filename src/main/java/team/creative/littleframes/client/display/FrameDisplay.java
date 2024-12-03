package team.creative.littleframes.client.display;

import team.creative.littleframes.common.data.LittleFrameData;

public abstract class FrameDisplay {
    
    public abstract int getWidth();
    
    public abstract int getHeight();
    
    public abstract void prepare(LittleFrameData data, boolean playing);
    
    public abstract void tick(LittleFrameData data, boolean playing);
    
    public abstract void pause(LittleFrameData data, boolean playing);
    
    public abstract void resume(LittleFrameData data, boolean playing);
    
    public abstract int texture();
    
    public abstract void release();
    
    public abstract boolean canTick();
}