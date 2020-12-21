package team.creative.littleframes.client.display;

import com.creativemd.creativecore.common.utils.mc.TickUtils;

import team.creative.littleframes.block.TileEntityCreativeFrame;
import team.creative.littleframes.client.texture.TextureCache;

public class FramePictureDisplay extends FrameDisplay {
	
	public final TextureCache texture;
	private int textureId;
	
	public FramePictureDisplay(TextureCache texture) {
		this.texture = texture;
	}
	
	@Override
	public void prepare(TileEntityCreativeFrame frame) {
		long time = frame.tick * 50 + (long) (TickUtils.getPartialTickTime() * 50);
		if (texture.getDuration() > 0 && time > texture.getDuration())
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
	
	@Override
	public int getWidth() {
		return texture.getWidth();
	}
	
	@Override
	public int getHeight() {
		return texture.getHeight();
	}
	
}
