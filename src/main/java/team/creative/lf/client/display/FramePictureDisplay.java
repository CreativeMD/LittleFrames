package team.creative.lf.client.display;

import com.creativemd.creativecore.common.utils.mc.TickUtils;

import team.creative.lf.block.TileEntityCreativeFrame;
import team.creative.lf.client.texture.TextureCache;

public class FramePictureDisplay extends FrameDisplay {
	
	public final TextureCache texture;
	private int textureId;
	
	public FramePictureDisplay(TextureCache texture) {
		this.texture = texture;
	}
	
	@Override
	public void prepare(TileEntityCreativeFrame frame) {
		long time = frame.tick * 50 + (long) (TickUtils.getPartialTickTime() * 50);
		if (time > texture.getDuration())
			if (frame.loop)
				time %= texture.getDuration();
		textureId = texture.getTexture(time);
	}
	
	@Override
	public void pause(TileEntityCreativeFrame frame) {}
	
	@Override
	public void resume(TileEntityCreativeFrame frame) {}
	
	@Override
	public int texture() {
		return textureId;
	}
	
	@Override
	public void release() {
		texture.unuse();
	}
	
}
