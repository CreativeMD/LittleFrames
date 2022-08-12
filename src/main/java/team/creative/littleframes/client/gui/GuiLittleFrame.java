package team.creative.littleframes.client.gui;

import com.creativemd.creativecore.common.gui.controls.gui.GuiAnalogeSlider;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NBTTagCompound;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiIconButton;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButton;
import team.creative.creativecore.common.gui.controls.simple.GuiSteppedSlider;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littleframes.LittleFrames;
import team.creative.littleframes.client.texture.TextureCache;
import team.creative.littleframes.client.texture.TextureSeeker;
import team.creative.littleframes.common.structure.LittleFrame;

public class GuiLittleFrame extends GuiLayer {
    
    public LittleFrame frame;
    
    public GuiTextfield url;
    public GuiButton save;
    
    public final GuiSyncLocal<EndTag> PLAY = getSyncHolder().register("play", x -> frame.play());
    public final GuiSyncLocal<EndTag> PAUSE = getSyncHolder().register("pause", x -> frame.pause());
    public final GuiSyncLocal<EndTag> STOP = getSyncHolder().register("stop", x -> frame.stop());
    
    public final GuiSyncLocal<CompoundTag> SET_DATA = getSyncHolder().register("set_data", nbt -> {
        String url = nbt.getString("url");
        if (LittleFrames.CONFIG.canUse(getPlayer(), url)) {
            frame.setURL(url);
            frame.renderDistance = Math.min(LittleFrames.CONFIG.maxRenderDistance, nbt.getInt("render"));
            frame.fitMode = LittleFrame.FitMode.values()[nbt.getInt("fit")];
            frame.loop = nbt.getBoolean("loop");
            frame.volume = nbt.getFloat("volume");
            frame.alpha = nbt.getFloat("transparency");
            frame.brightness = nbt.getFloat("brightness");
        }
        
        frame.updateStructure();
    });
    
    public GuiLittleFrame(LittleFrame frame) {
        this(frame, false, 16);
    }
    
    public GuiLittleFrame(LittleFrame frame, boolean editFacing, int scaleSize) {
        super("little_frame", 200, editFacing ? 220 : 200);
        this.frame = frame;
    }
    
    @Override
    public void create() {
        save = new GuiButton(translate("gui.creative_frame.save"), 144, 180, 50) {
            @Override
            public void onClicked(int x, int y, int button) {
                NBTTagCompound nbt = new NBTTagCompound();
                GuiTextfield url = (GuiTextfield) get("url");
                GuiSteppedSlider renderDistance = (GuiSteppedSlider) get("renderDistance");
                
                GuiStateButton fit = (GuiStateButton) get("fit");
                
                GuiAnalogeSlider transparency = (GuiAnalogeSlider) get("transparency");
                GuiAnalogeSlider brightness = (GuiAnalogeSlider) get("brightness");
                
                GuiCheckBox loop = (GuiCheckBox) get("loop");
                GuiAnalogeSlider volume = (GuiAnalogeSlider) get("volume");
                
                nbt.setInteger("fit", fit.getState());
                
                nbt.setInteger("render", (int) renderDistance.value);
                
                nbt.setFloat("transparency", (float) transparency.value);
                nbt.setFloat("brightness", (float) brightness.value);
                
                nbt.setBoolean("loop", loop.value);
                nbt.setFloat("volume", (float) volume.value);
                
                nbt.setString("url", url.text);
                
                nbt.setInteger("type", 0);
                sendPacketToServer(nbt);
            }
        };
        
        url = new GuiUrlTextfield(save, "url", frame.getRealURL(), 0, 0, 194, 16);
        url.maxLength = 512;
        controls.add(url);
        controls.add(new GuiLabel(translate(frame.cache != null && frame.cache.getError() != null ? frame.cache.getError() : ""), 0, 20, ColorUtils.RED));
        
        save.setEnabled(LittleFrames.CONFIG.canUse(mc.player, url.text));
        controls.add(save);
        
        String[] args = new String[LittleFrame.FitMode.values().length];
        for (int i = 0; i < args.length; i++)
            args[i] = translate("gui.little_frame.fitmode." + LittleFrame.FitMode.values()[i].name());
        controls.add(new GuiStateButton("fit", frame.fitMode.ordinal(), 0, 68, 70, args));
        
        controls.add(new GuiLabel(translate("gui.creative_frame.transparency"), 0, 110));
        controls.add(new GuiAnalogeSlider("transparency", 80, 112, 109, 5, frame.alpha, 0, 1));
        
        controls.add(new GuiLabel(translate("gui.creative_frame.brightness"), 0, 122));
        controls.add(new GuiAnalogeSlider("brightness", 80, 124, 109, 5, frame.brightness, 0, 1));
        
        controls.add(new GuiLabel(translate("gui.creative_frame.distance"), 0, 134));
        controls.add(new GuiSteppedSlider("renderDistance", 80, 136, 109, 5, frame.renderDistance, 5, 1024));
        
        controls.add(new GuiCheckBox("loop", translate("gui.creative_frame.loop"), 60, 168, frame.loop));
        controls.add(new GuiLabel(translate("gui.creative_frame.volume"), 0, 186));
        controls.add(new GuiAnalogeSlider("volume", 50, 188, 30, 5, frame.volume, 0, 1));
        
        controls.add(new GuiButton(translate("gui.creative_frame.reload"), 102, 180) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                synchronized (TextureSeeker.LOCK) {
                    if (GuiScreen.isShiftKeyDown())
                        TextureCache.reloadAll();
                    else if (frame.cache != null)
                        frame.cache.reload();
                }
            }
        }.setCustomTooltip(translate("gui.creative_frame.reloadtooltip")));
        
        controls.add(new GuiIconButton("play", 0, 168, 10) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setBoolean("play", true);
                sendPacketToServer(nbt);
            }
        });
        controls.add(new GuiIconButton("pause", 20, 168, 9) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setBoolean("pause", true);
                sendPacketToServer(nbt);
            }
        });
        controls.add(new GuiIconButton("stop", 40, 168, 11) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setBoolean("stop", true);
                sendPacketToServer(nbt);
            }
        });
    }
    
}
