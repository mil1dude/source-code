/*
 * Copyright (c) 2024 lax1dude. All Rights Reserved.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package net.lax1dude.eaglercraft.v1_8.recording;

import net.lax1dude.eaglercraft.v1_8.HString;
import net.lax1dude.eaglercraft.v1_8.internal.ScreenRecordParameters;
import net.lax1dude.eaglercraft.v1_8.log4j.LogManager;
import net.lax1dude.eaglercraft.v1_8.log4j.Logger;
import net.lax1dude.eaglercraft.v1_8.minecraft.ScreenGenericErrorMessage;
import net.lax1dude.eaglercraft.v1_8.sp.gui.GuiSlider2;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;

public class ScreenRecordingSettings extends Screen {

	private static final Logger logger = LogManager.getLogger("ScreenRecordingSettings");

	protected final Screen parent;

	protected Button recordButton;
	protected Button codecButton;
	protected GuiSlider2 videoResolutionSlider;
	protected GuiSlider2 videoFrameRateSlider;
	protected GuiSlider2 audioBitrateSlider;
	protected GuiSlider2 videoBitrateSlider;
	protected GuiSlider2 microphoneVolumeSlider;
	protected GuiSlider2 gameVolumeSlider;
	protected boolean dirty = false;

	public ScreenRecordingSettings(Screen parent) {
		this.parent = parent;
	}

	public void initGui() {
		buttonList.clear();
		buttonList.add(new net.minecraft.client.gui.components.Button(0, this.width / 2 - 100, this.height / 6 + 168, I18n.get("gui.done")));
		buttonList.add(codecButton = new net.minecraft.client.gui.components.Button(1, this.width / 2 + 65, this.height / 6 - 2, 75, 20, I18n.get("options.screenRecording.codecButton")));
		boolean isRecording = ScreenRecordingController.isRecording();
		buttonList.add(recordButton = new net.minecraft.client.gui.components.Button(2, this.width / 2 + 15, this.height / 6 + 28, 125, 20,
				I18n.get(isRecording ? "options.screenRecording.stop" : "options.screenRecording.start")));
		buttonList.add(videoResolutionSlider = new GuiSlider2(3, this.width / 2 - 155, this.height / 6 + 64, 150, 20, (mc.options.screenRecordResolution - 1) / 3.999f, 1.0f) {
			@Override
			protected String updateDisplayString() {
				int i = (int)(sliderValue * 3.999f);
				return I18n.get("options.screenRecording.videoResolution") + ": x" + HString.format("%.2f", 1.0f / (int)Math.pow(2.0, i));
			}
			@Override
			protected void onChange() {
				mc.options.screenRecordResolution = 1 + (int)(sliderValue * 3.999f);
				dirty = true;
			}
		});
		buttonList.add(videoFrameRateSlider = new GuiSlider2(4, this.width / 2 + 5, this.height / 6 + 64, 150, 20, (Math.max(mc.options.screenRecordFPS, 9) - 9) / 51.999f, 1.0f) {
			@Override
			protected String updateDisplayString() {
				int i = (int)(sliderValue * 51.999f);
				return I18n.get("options.screenRecording.videoFPS") + ": " + (i <= 0 ? I18n.get("options.screenRecording.onVSync") : 9 + i);
			}
			@Override
			protected void onChange() {
				int i = (int)(sliderValue * 51.999f);
				mc.options.screenRecordFPS = i <= 0 ? -1 : 9 + i;
				dirty = true;
			}
		});
		buttonList.add(videoBitrateSlider = new GuiSlider2(5, this.width / 2 - 155, this.height / 6 + 98, 150, 20, Mth.sqrt_float(Mth.clamp_float((mc.options.screenRecordVideoBitrate - 250) / 19750.999f, 0.0f, 1.0f)), 1.0f) {
			@Override
			protected String updateDisplayString() {
				return I18n.get("options.screenRecording.videoBitrate") + ": " + (250 + (int)(sliderValue * sliderValue * 19750.999f)) + "kbps";
			}
			@Override
			protected void onChange() {
				mc.options.screenRecordVideoBitrate = 250 + (int)(sliderValue * sliderValue * 19750.999f);
				dirty = true;
			}
		});
		buttonList.add(audioBitrateSlider = new GuiSlider2(6, this.width / 2 + 5, this.height / 6 + 98, 150, 20, Mth.sqrt_float(Mth.clamp_float((mc.options.screenRecordAudioBitrate - 24) / 232.999f, 0.0f, 1.0f)), 1.0f) {
			@Override
			protected String updateDisplayString() {
				return I18n.get("options.screenRecording.audioBitrate") + ": " + (24 + (int)(sliderValue * sliderValue * 232.999f)) + "kbps";
			}
			@Override
			protected void onChange() {
				mc.options.screenRecordAudioBitrate = 24 + (int)(sliderValue * sliderValue * 232.999f);
				dirty = true;
			}
		});
		buttonList.add(gameVolumeSlider = new GuiSlider2(7, this.width / 2 - 155, this.height / 6 + 130, 150, 20, mc.options.screenRecordGameVolume, 1.0f) {
			@Override
			protected String updateDisplayString() {
				return I18n.get("options.screenRecording.gameVolume") + ": " + (int)(sliderValue * 100.999f) + "%";
			}
			@Override
			protected void onChange() {
				mc.options.screenRecordGameVolume = sliderValue;
				ScreenRecordingController.setGameVolume(sliderValue);
				dirty = true;
			}
		});
		buttonList.add(microphoneVolumeSlider = new GuiSlider2(8, this.width / 2 + 5, this.height / 6 + 130, 150, 20, mc.options.screenRecordMicVolume, 1.0f) {
			@Override
			protected String updateDisplayString() {
				return I18n.get("options.screenRecording.microphoneVolume") + ": " + (int)(sliderValue * 100.999f) + "%";
			}
			@Override
			protected void onChange() {
				mc.options.screenRecordMicVolume = sliderValue;
				ScreenRecordingController.setMicrophoneVolume(sliderValue);
				dirty = true;
			}
		});
		codecButton.enabled = !isRecording;
		videoResolutionSlider.enabled = !isRecording;
		videoFrameRateSlider.enabled = !isRecording;
		audioBitrateSlider.enabled = !isRecording;
		videoBitrateSlider.enabled = !isRecording;
		microphoneVolumeSlider.enabled = !ScreenRecordingController.isMicVolumeLocked();
	}

	protected void actionPerformed(net.minecraft.client.gui.components.Button parButton) {
		if(parButton.id == 0) {
			if(dirty) {
				mc.options.saveOptions();
				dirty = false;
			}
			mc.displayScreen(parent);
		}else if(parButton.id == 1) {
			mc.displayScreen(new ScreenSelectCodec(this, mc.options.screenRecordCodec));
		}else if(parButton.id == 2) {
			if(!ScreenRecordingController.isRecording()) {
				try {
					ScreenRecordingController.startRecording(new ScreenRecordParameters(mc.options.screenRecordCodec,
							mc.options.screenRecordResolution, mc.options.screenRecordVideoBitrate,
							mc.options.screenRecordAudioBitrate, mc.options.screenRecordFPS));
				}catch(Throwable t) {
					logger.error("Failed to begin screen recording!");
					logger.error(t);
					mc.displayScreen(new ScreenGenericErrorMessage("options.screenRecording.failed", t.toString(), parent));
				}
			}else {
				ScreenRecordingController.endRecording();
			}
		}
	}

	public void drawScreen(int i, int j, float var3) {
		drawDefaultBackground();
		drawCenteredString(fontRendererObj, I18n.get("options.screenRecording.title"), this.width / 2, 15, 16777215);
		if(mc.options.screenRecordCodec == null) {
			mc.options.screenRecordCodec = ScreenRecordingController.getDefaultCodec();
		}
		
		String codecString = mc.options.screenRecordCodec.name;
		int codecStringWidth = fontRendererObj.getStringWidth(codecString);
		drawString(fontRendererObj, codecString, this.width / 2 + 60 - codecStringWidth, this.height / 6 + 4, 0xFFFFFF);
		
		boolean isRecording = ScreenRecordingController.isRecording();
		codecButton.enabled = !isRecording;
		videoResolutionSlider.enabled = !isRecording;
		videoFrameRateSlider.enabled = !isRecording;
		audioBitrateSlider.enabled = !isRecording;
		videoBitrateSlider.enabled = !isRecording;
		microphoneVolumeSlider.enabled = !ScreenRecordingController.isMicVolumeLocked();
		recordButton.displayString = I18n.get(isRecording ? "options.screenRecording.stop" : "options.screenRecording.start");
		String statusString = I18n.get("options.screenRecording.status",
				(isRecording ? ChatFormatting.GREEN : ChatFormatting.RED) + I18n.get(isRecording ? "options.screenRecording.status.1" : "options.screenRecording.status.0"));
		int statusStringWidth = fontRendererObj.getStringWidth(statusString);
		drawString(fontRendererObj, statusString, this.width / 2 + 10 - statusStringWidth, this.height / 6 + 34, 0xFFFFFF);
		
		super.drawScreen(i, j, var3);
	}

	protected void handleCodecCallback(EnumScreenRecordingCodec codec) {
		EnumScreenRecordingCodec oldCodec = mc.options.screenRecordCodec;
		if(ScreenRecordingController.codecs.contains(codec)) {
			mc.options.screenRecordCodec = codec;
		}else {
			mc.options.screenRecordCodec = ScreenRecordingController.getDefaultCodec();
		}
		if(oldCodec != mc.options.screenRecordCodec) {
			dirty = true;
		}
	}

}