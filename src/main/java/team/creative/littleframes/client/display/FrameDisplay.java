package team.creative.littleframes.client.display;

public abstract class FrameDisplay {
	
	public abstract int getWidth();
	
	public abstract int getHeight();
	
	public abstract void prepare(String url, float volume, boolean playing, boolean loop, int tick);
	
	public abstract void pause(String url, float volume, boolean playing, boolean loop, int tick);
	
	public abstract void resume(String url, float volume, boolean playing, boolean loop, int tick);
	
	public abstract int texture();
	
	public abstract void release();
	
}
