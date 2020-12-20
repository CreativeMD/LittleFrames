package team.creative.lf.gui;

import com.creativemd.creativecore.common.gui.premade.SubContainerTileEntity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import team.creative.lf.LittleFrames;
import team.creative.lf.block.TileEntityCreativeFrame;

public class SubContainerPic extends SubContainerTileEntity {
	
	public TileEntityCreativeFrame frame;
	public Object tile;
	
	public SubContainerPic(TileEntityCreativeFrame frame, EntityPlayer player, Object tile) {
		super(player, frame);
		this.frame = frame;
		this.tile = tile;
	}
	
	public SubContainerPic(TileEntityCreativeFrame frame, EntityPlayer player) {
		this(frame, player, null);
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
		int type = nbt.getInteger("type");
		if (type == 0) {
			String url = nbt.getString("url");
			if (LittleFrames.CONFIG.canUse(player, url)) {
				frame.url = url;
				/*frame.sizeX = (float) Math.min(LittleFrames.CONFIG.sizeLimitation, nbt.getFloat("x"));
				frame.sizeY = (float) Math.min(LittleFrames.CONFIG.sizeLimitation, nbt.getFloat("y"));
				
				frame.renderDistance = Math.min(LittleFrames.CONFIG.maxRenderDistance, nbt.getInteger("render"));
				frame.posX = nbt.getByte("posX");
				frame.posY = nbt.getByte("posY");
				frame.rotation = nbt.getByte("rotation");
				frame.visibleFrame = nbt.getBoolean("visibleFrame");
				frame.flippedX = nbt.getBoolean("flippedX");
				frame.flippedY = nbt.getBoolean("flippedY");
				
				frame.rotationX = nbt.getFloat("rotX");
				frame.rotationY = nbt.getFloat("rotY");
				
				frame.transparency = nbt.getFloat("transparency");
				frame.brightness = nbt.getFloat("brightness");*/
			}
			
			frame.updateBlock();
		}
	}
	
}
