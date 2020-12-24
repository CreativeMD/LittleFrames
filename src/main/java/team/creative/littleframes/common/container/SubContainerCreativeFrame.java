package team.creative.littleframes.common.container;

import com.creativemd.creativecore.common.gui.premade.SubContainerTileEntity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import team.creative.littleframes.LittleFrames;
import team.creative.littleframes.common.block.TileEntityCreativeFrame;

public class SubContainerCreativeFrame extends SubContainerTileEntity {
	
	public TileEntityCreativeFrame frame;
	
	public SubContainerCreativeFrame(TileEntityCreativeFrame frame, EntityPlayer player) {
		super(player, frame);
		this.frame = frame;
	}
	
	@Override
	public boolean shouldTick() {
		return false;
	}
	
	@Override
	public void createControls() {
		
	}
	
	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		if (nbt.hasKey("play"))
			frame.play();
		else if (nbt.hasKey("pause"))
			frame.pause();
		else if (nbt.hasKey("stop"))
			frame.stop();
		else {
			String url = nbt.getString("url");
			if (LittleFrames.CONFIG.canUse(player, url)) {
				frame.setURL(url);
				float sizeX = (float) Math.min(LittleFrames.CONFIG.sizeLimitation, nbt.getFloat("x"));
				float sizeY = (float) Math.min(LittleFrames.CONFIG.sizeLimitation, nbt.getFloat("y"));
				int posX = nbt.getByte("posX");
				int posY = nbt.getByte("posY");
				if (posX == 0) {
					frame.min.x = 0;
					frame.max.x = sizeX;
				} else if (posX == 1) {
					float middle = sizeX / 2;
					frame.min.x = 0.5F - middle;
					frame.max.x = 0.5F + middle;
				} else {
					frame.min.x = 1 - sizeX;
					frame.max.x = 1;
				}
				
				if (posY == 0) {
					frame.min.y = 0;
					frame.max.y = sizeY;
				} else if (posY == 1) {
					float middle = sizeY / 2;
					frame.min.y = 0.5F - middle;
					frame.max.y = 0.5F + middle;
				} else {
					frame.min.y = 1 - sizeY;
					frame.max.y = 1;
				}
				
				frame.renderDistance = Math.min(LittleFrames.CONFIG.maxRenderDistance, nbt.getInteger("render"));
				frame.rotation = nbt.getFloat("rotation");
				frame.visibleFrame = nbt.getBoolean("visibleFrame");
				frame.bothSides = nbt.getBoolean("bothSides");
				frame.loop = nbt.getBoolean("loop");
				frame.flipX = nbt.getBoolean("flipX");
				frame.flipY = nbt.getBoolean("flipY");
				frame.volume = nbt.getFloat("volume");
				frame.alpha = nbt.getFloat("transparency");
				frame.brightness = nbt.getFloat("brightness");
			}
			
			frame.updateBlock();
		}
	}
	
}
