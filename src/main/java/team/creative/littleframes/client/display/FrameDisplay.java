package team.creative.littleframes.client.display;

public abstract class FrameDisplay {
    
    public abstract int getWidth();
    
    public abstract int getHeight();
    
    public abstract int prepare(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick);
    
    public abstract void tick(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick);
    
    public abstract void pause(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick);
    
    public abstract void resume(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick);

    public abstract void release();

    public abstract boolean canTick();
}
