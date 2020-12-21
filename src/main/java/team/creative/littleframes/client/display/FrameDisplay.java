package team.creative.littleframes.client.display;

import team.creative.littleframes.block.TileEntityCreativeFrame;

public abstract class FrameDisplay {
	
	public abstract int getWidth();
	
	public abstract int getHeight();
	
	public abstract void prepare(TileEntityCreativeFrame frame);
	
	public abstract void pause(TileEntityCreativeFrame frame);
	
	public abstract void resume(TileEntityCreativeFrame frame);
	
	public abstract int texture();
	
	public abstract void release();
	
}
