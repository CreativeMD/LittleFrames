package team.creative.littleframes.client.gui;

import java.net.URI;
import java.net.URISyntaxException;

import org.watermedia.api.image.ImageAPI;
import org.watermedia.api.image.ImageCache;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.control.parent.GuiColumn;
import team.creative.creativecore.common.gui.control.parent.GuiLabeledControl;
import team.creative.creativecore.common.gui.control.parent.GuiRow;
import team.creative.creativecore.common.gui.control.parent.GuiTable;
import team.creative.creativecore.common.gui.control.simple.GuiButton;
import team.creative.creativecore.common.gui.control.simple.GuiButtonIcon;
import team.creative.creativecore.common.gui.control.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.control.simple.GuiDuration;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.creativecore.common.gui.control.simple.GuiSlider;
import team.creative.creativecore.common.gui.control.simple.GuiStateButton;
import team.creative.creativecore.common.gui.control.simple.GuiSteppedSlider;
import team.creative.creativecore.common.gui.control.simple.GuiTextfield;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.Icon;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littleframes.LittleFrames;
import team.creative.littleframes.common.structure.LittlePictureFrame;
import team.creative.littleframes.common.structure.LittlePictureFrame.FitMode;

public class GuiLittlePictureFrame extends GuiLayer {
    
    public LittlePictureFrame frame;
    
    public GuiTextfield url;
    
    public final GuiSyncLocal<EndTag> PLAY = getSyncHolder().register("play", x -> frame.play());
    public final GuiSyncLocal<EndTag> PAUSE = getSyncHolder().register("pause", x -> frame.pause());
    public final GuiSyncLocal<EndTag> STOP = getSyncHolder().register("stop", x -> frame.stop());
    
    public final GuiSyncLocal<CompoundTag> SET_DATA = getSyncHolder().register("set_data", nbt -> {
        String url = nbt.getString("url");
        if (LittleFrames.CONFIG.canUse(getPlayer(), url)) {
            try {
                frame.setURL(new URI(url));
            } catch (URISyntaxException e) {
                LittleFrames.LOGGER.error("Failed to save url '{}'", url);
            }
            frame.data.renderDistance = Math.min(LittleFrames.CONFIG.maxRenderDistance, nbt.getInt("render"));
            frame.fitMode = LittlePictureFrame.FitMode.values()[nbt.getInt("fit")];
            frame.data.loop = nbt.getBoolean("loop");
            frame.data.volume(nbt.getFloat("volume"));
            frame.data.minDistance = nbt.getFloat("min");
            frame.data.maxDistance = nbt.getFloat("max");
            frame.data.alpha = nbt.getFloat("transparency");
            frame.data.brightness = nbt.getFloat("brightness");
            frame.data.playbackSpeed = nbt.getDouble("speed");
            frame.data.refreshCounter = frame.data.refreshInterval = nbt.getInt("refresh");
        }
        
        frame.updateStructure();
    });
    
    public GuiLittlePictureFrame(LittlePictureFrame frame) {
        super("little_frame", 200, 180);
        this.frame = frame;
    }
    
    @Override
    public void create() {
        GuiButton save = new GuiButton("save", x -> {
            CompoundTag nbt = new CompoundTag();
            GuiTextfield url = get("url");
            GuiSteppedSlider renderDistance = get("distance");
            
            GuiStateButton<FitMode> fit = get("fit");
            
            GuiSlider transparency = get("transparency");
            GuiSlider brightness = get("brightness");
            
            GuiCheckBox loop = get("loop");
            GuiSlider volume = get("volume");
            GuiSteppedSlider min = get("range_min");
            GuiSteppedSlider max = get("range_max");
            
            GuiCheckBox autoRefresh = get("autoRefresh");
            GuiDuration duration = get("duration");
            
            GuiSlider speed = get("speed");
            
            nbt.putInt("fit", fit.selected().ordinal());
            
            nbt.putInt("render", (int) renderDistance.getValue());
            
            nbt.putFloat("transparency", (float) transparency.getValue());
            nbt.putFloat("brightness", (float) brightness.getValue());
            
            nbt.putBoolean("loop", loop.value);
            nbt.putFloat("volume", (float) volume.getValue());
            nbt.putFloat("min", min.getIntValue());
            nbt.putFloat("max", max.getIntValue());
            
            nbt.putString("url", url.getText());
            
            nbt.putDouble("speed", Math.max(0.1, speed.getValue()));
            
            nbt.putInt("refresh", autoRefresh.value ? duration.getDuration() : -1);
            
            nbt.putInt("type", 0);
            SET_DATA.send(nbt);
        });
        
        save.setTranslate("gui.save");
        
        align = Align.STRETCH;
        flow = GuiFlow.STACK_Y;
        
        url = new GuiUrlTextfield(save, "url", frame.data.getURIPath());
        add(url);
        GuiLabel error = new GuiLabel("error").setDefaultColor(ColorUtils.RED);
        if (frame.isClient() && frame.cache != null) {
            if (frame.cache.getStatus().equals(ImageCache.Status.FAILED)) {
                Exception e = frame.cache.getException();
                if (frame.cache.isVideo()) {
                    if (!LittleFrames.CONFIG.useVLC)
                        error.setTitle(Component.literal("Image not found"));
                } else {
                    if (e.getMessage() == null)
                        error.setTranslate("download.exception.invalid");
                    else if (e.getMessage().startsWith("Server returned HTTP response code: 403"))
                        error.setTranslate("download.exception.forbidden");
                    else if (e.getMessage().startsWith("Server returned HTTP response code: 404"))
                        error.setTranslate("download.exception.notfound");
                    else
                        error.setTranslate("download.exception.invalid");
                }
            }
        }
        add(error);
        GuiStateButton<FitMode> button = new GuiStateButton<FitMode>("fit", new TextMapBuilder<FitMode>().addComponent(FitMode.values(), x -> Component.translatable(
            "gui.little_frame.fitmode." + x.name())));
        button.select(frame.fitMode);
        add(button);
        
        GuiTable table = new GuiTable();
        add(table);
        GuiColumn left;
        GuiColumn right;
        
        table.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        left.add(new GuiLabel("t_label").setTitle(Component.translatable("gui.creative_frame.transparency").append(":")));
        right.add(new GuiSlider("transparency", frame.data.alpha, 0, 1).setExpandableX());
        
        table.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        left.add(new GuiLabel("b_label").setTitle(Component.translatable("gui.creative_frame.brightness").append(":")));
        right.add(new GuiSlider("brightness", frame.data.brightness, 0, 1).setExpandableX());
        
        table.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        left.add(new GuiLabel("d_label").setTitle(Component.translatable("gui.creative_frame.distance").append(":")));
        right.add(new GuiSteppedSlider("distance", frame.data.renderDistance, 5, 1024).setExpandableX());
        
        GuiParent play = new GuiParent(GuiFlow.STACK_X);
        add(play);
        
        play.add(new GuiButtonIcon("play", Icon.PLAY, x -> PLAY.send(EndTag.INSTANCE)));
        play.add(new GuiButtonIcon("pause", Icon.PAUSE, x -> PAUSE.send(EndTag.INSTANCE)));
        play.add(new GuiButtonIcon("stop", Icon.STOP, x -> STOP.send(EndTag.INSTANCE)));
        
        add(new GuiCheckBox("loop", frame.data.loop).setTranslate("gui.creative_frame.loop"));
        GuiParent playOther = new GuiParent(GuiFlow.STACK_X);
        add(playOther);
        playOther.add(new GuiLabeledControl("gui.creative_frame.volume", new GuiSlider("volume", frame.data.volume(), 0, 1).setExpandableX()));
        playOther.add(new GuiLabeledControl("gui.creative_frame.speed", new GuiSliderSingleDigit("speed", frame.data.playbackSpeed, 0.1, 2).setExpandableX()));
        
        GuiParent range = new GuiParent();
        add(range);
        range.add(new GuiLabel("range_label").setTranslate("gui.creative_frame.range"));
        range.add(new GuiSteppedSlider("range_min", (int) frame.data.minDistance, 0, 512).setExpandableX());
        range.add(new GuiSteppedSlider("range_max", (int) frame.data.maxDistance, 0, 512).setExpandableX());
        
        GuiParent refresh = new GuiParent();
        refresh.spacing = 10;
        add(refresh.setVAlign(VAlign.CENTER));
        refresh.add(new GuiCheckBox("autoRefresh", frame.data.refreshInterval > 0).setTranslate("gui.creative_frame.autoReload"));
        refresh.add(new GuiDuration("duration", frame.data.refreshInterval, false, true, true, true));
        
        GuiParent bottom = new GuiParent(GuiFlow.STACK_X);
        bottom.align = Align.RIGHT;
        add(bottom);
        save.setEnabled(LittleFrames.CONFIG.canUse(getPlayer(), url.getText()));
        bottom.add(save);
        bottom.add(new GuiButton("reload", x -> {
            if (Screen.hasShiftDown())
                ImageAPI.reloadCache();
            else if (frame.cache != null)
                frame.cache.reload();
        }).setTranslate("gui.creative_frame.reload").setTooltip(new TextBuilder().translate("gui.creative_frame.reloadtooltip").build()));
    }
    
}