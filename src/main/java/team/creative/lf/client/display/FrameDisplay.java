package team.creative.lf.client.display;

import team.creative.lf.block.TileEntityCreativeFrame;

public abstract class FrameDisplay {
	
	public abstract void prepare(TileEntityCreativeFrame frame);
	
	public abstract void pause(TileEntityCreativeFrame frame);
	
	public abstract void resume(TileEntityCreativeFrame frame);
	
	public abstract int texture();
	
	public abstract void release();
	
}
