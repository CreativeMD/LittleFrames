package team.creative.littleframes.common.container;

import com.creativemd.creativecore.common.gui.container.SubContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import team.creative.littleframes.LittleFrames;
import team.creative.littleframes.common.structure.LittleFrame;

public class SubContainerLittleFrame extends SubContainer {
	
	public LittleFrame frame;
	
	public SubContainerLittleFrame(EntityPlayer player, LittleFrame frame) {
		super(player);
		this.frame = frame;
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
				frame.renderDistance = Math.min(LittleFrames.CONFIG.maxRenderDistance, nbt.getInteger("render"));
				frame.fitMode = LittleFrame.FitMode.values()[nbt.getInteger("fit")];
				frame.loop = nbt.getBoolean("loop");
				frame.volume = nbt.getFloat("volume");
				frame.alpha = nbt.getFloat("transparency");
				frame.brightness = nbt.getFloat("brightness");
			}
			
			frame.updateStructure();
		}
	}
	
}
