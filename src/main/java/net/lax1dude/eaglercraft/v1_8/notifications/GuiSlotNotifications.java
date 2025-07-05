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

package net.lax1dude.eaglercraft.v1_8.notifications;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import com.mojang.blaze3d.vertex.PoseStack;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.lax1dude.eaglercraft.v1_8.socket.protocol.pkt.server.SPacketNotifBadgeShowV4EAG.EnumBadgePriority;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

public class GuiSlotNotifications extends ObjectSelectionList<GuiSlotNotifications.NotifBadgeSlot> {
    
    private static final int SLOT_HEIGHT = 68;
    

    protected static class ClickEventZone {
        public final int x;
        public final int y;
        public final int width;
        public final int height;
        public final Component chatComponent;
        public final boolean hasClickEvent;
        public final boolean hasHoverEvent;

        public ClickEventZone(int x, int y, int width, int height, Component chatComponent, boolean hasClickEvent, boolean hasHoverEvent) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.chatComponent = chatComponent;
            this.hasClickEvent = hasClickEvent;
            this.hasHoverEvent = hasHoverEvent;
        }

        public int getX() { return x; }
        public int getY() { return y; }
    }

    protected final Minecraft mc;
    protected final RandomSource rand = RandomSource.create();
    protected int selectedElement = -1;

	private static final ResourceLocation eaglerGui = new ResourceLocation("eagler", "textures/gui/eagler_gui.png");
	private static final ResourceLocation largeNotifBk = new ResourceLocation("eagler", "textures/gui/notif_bk_large.png");

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");

	final ScreenNotifications parent;
	final List<NotifBadgeSlot> currentDisplayNotifs;

	int mouseX;
	int mouseY;

	protected static class NotifBadgeSlot {
		
		protected final NotificationBadge badge;
		protected final List<ClickEventZone> cursorEvents = new ArrayList<>();
		protected int currentScreenX = -69420;
		protected int currentScreenY = -69420;
		
		protected NotifBadgeSlot(NotificationBadge badge) {
			this.badge = badge;
		}
		
	}

	public GuiSlotNotifications(ScreenNotifications parent) {
        super(parent.getMinecraft(), parent.width, parent.height, 32, parent.height - 44, SLOT_HEIGHT);
        this.mc = parent.getMinecraft();
        this.parent = parent;
        this.currentDisplayNotifs = new ArrayList<>();
        this.setRenderSelection(true);
        this.setRenderHeader(false, 0);
        this.refreshList();
    }

	@Override
	protected int getSize() {
		return currentDisplayNotifs.size();
	}

	@Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        int slotIndex = this.getSlotIndexFromScreenCoords(mouseX, mouseY);
        if (slotIndex >= 0 && slotIndex < this.getItemCount()) {
            this.selectItem(slotIndex);
            this.setSelected(this.getEntry(slotIndex));
            this.elementClicked(slotIndex, button == 0 && slotIndex == this.getSelectedItem(), mouseX, mouseY);
            return true;
        }
        return false;
    }
    
    @Override
    protected void elementClicked(int slotIndex, boolean isDoubleClick, double mouseX, double mouseY) {
		if(selectedElement != slotIndex) return; //workaround for vanilla bs
		if(slotIndex < currentDisplayNotifs.size()) {
			NotifBadgeSlot slot = currentDisplayNotifs.get(slotIndex);
			if(slot.currentScreenY != -69420) {
				int w = getListWidth();
				int localX = (int)mouseX - slot.currentScreenX;
				int localY = (int)mouseY - slot.currentScreenY;
				if(localX >= w - 22 && localX < w - 5 && localY >= 5 && localY < 21) {
					slot.badge.removeNotif();
					mc.getSoundManager().playSound(SimpleSoundInstance.forUI(SimpleSoundInstance.create(new ResourceLocation("gui.button.press"), 1.0F)));
					return;
				}
				Component cmp = slot.badge.bodyComponent;
				if(cmp != null) {
					if(isDoubleClick) {
						if (cmp.getChatStyle().getChatClickEvent() != null
								&& cmp.getChatStyle().getChatClickEvent().getAction().shouldAllowInChat()) {
							if(parent.handleComponentClick(cmp)) {
								mc.getSoundManager().playSound(SimpleSoundInstance.forUI(SimpleSoundInstance.create(new ResourceLocation("gui.button.press"), 1.0F)));
								return;
							}
						}
					}else {
						if(parent.selected != slotIndex) {
							parent.selected = slotIndex;
						}else {
							List<ClickEventZone> cursorEvents = slot.cursorEvents;
							if(cursorEvents != null && !cursorEvents.isEmpty()) {
								for(int j = 0, m = cursorEvents.size(); j < m; ++j) {
									ClickEventZone evt = cursorEvents.get(j);
									if(evt.hasClickEvent) {
										int offsetPosX = slot.currentScreenX + evt.getX();
										int offsetPosY = slot.currentScreenY + evt.getY();
										if((int)mouseX >= offsetPosX && (int)mouseY >= offsetPosY && (int)mouseX < offsetPosX + evt.width && (int)mouseY < offsetPosY + evt.height) {
											if(parent.handleComponentClick(evt.chatComponent)) {
												mc.getSoundManager().playSound(SimpleSoundInstance.forUI(SimpleSoundInstance.create(new ResourceLocation("gui.button.press"), 1.0F)));
												return;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected boolean isSelectedItem(int index) {
		return index == selectedElement;
	}

	@Override
    public int getRowWidth() {
        return this.width - 10;
    }
    
    @Override
    public int getScrollbarPosition() {
        return this.x0 + this.width - 6;
    }
    
    @Override
    protected int getRowTop(int index) {
        return this.y0 + 4 + index * this.itemHeight;
    }
    
    @Override
    public int getRowLeft() {
        return this.x0 + 4;
    }
    
    @Override
    protected void renderBackground(GuiGraphics guiGraphics) {
        this.parent.renderBackground(guiGraphics);
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        for(int i = 0, l = currentDisplayNotifs.size(); i < l; ++i) {
            NotifBadgeSlot slot = currentDisplayNotifs.get(i);
            slot.currentScreenX = -69420;
            slot.currentScreenY = -69420;
        }
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
    
    protected void refreshList() {
        this.clearEntries();
        for (NotifBadgeSlot slot : currentDisplayNotifs) {
            this.addEntry(slot);
        }
    }

	@Override
	protected void drawBackground() {
		parent.drawBackground(0);
	}

    @Override
    protected void renderItem(GuiGraphics guiGraphics, int slotIndex, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        if (slotIndex < 0 || slotIndex >= this.getItemCount()) {
            return;
        }
        
        NotifBadgeSlot slot = this.getEntry(slotIndex);
        if (slot == null) {
            return;
        }
        
        slot.currentScreenX = x;
        slot.currentScreenY = y;
        NotificationBadge bd = slot.badge;
        
        // Mark as read if visible
        if (y + 32 > this.getY() && y + 32 < this.getY() + this.getHeight()) {
            bd.markRead();
        }
        
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0.0f);
        
        int badgeWidth = this.width - 10;
        int badgeHeight = this.itemHeight - 4;
        
        // Draw background with color tint
        float r = ((bd.backgroundColor >> 16) & 0xFF) / 255.0f;
        float g = ((bd.backgroundColor >> 8) & 0xFF) / 255.0f;
        float b = (bd.backgroundColor & 0xFF) / 255.0f;
        
        if (parent.selected != slotIndex) {
            r *= 0.85f;
            g *= 0.85f;
            b *= 0.85f;
        }
        
        // Draw background texture
        guiGraphics.setColor(r, g, b, 1.0f);
        guiGraphics.blit(largeNotifBk, 0, 0, 0, bd.unreadFlagRender ? 64 : 0, badgeWidth - 32, 64);
        guiGraphics.blit(largeNotifBk, badgeWidth - 32, 0, 224, bd.unreadFlagRender ? 64 : 0, 32, 64);
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        
        // Draw priority icon
        int priorityX = badgeWidth - 21;
        int priorityY = badgeHeight - 21;
        switch (bd.priority) {
            case LOW:
                guiGraphics.blit(eaglerGui, priorityX, priorityY, 192, 176, 16, 16);
                break;
            case NORMAL:
                guiGraphics.blit(eaglerGui, priorityX, priorityY, 208, 176, 16, 16);
                break;
            case HIGHER:
                guiGraphics.blit(eaglerGui, priorityX, priorityY, 224, 176, 16, 16);
                break;
            case HIGHEST:
                guiGraphics.blit(eaglerGui, priorityX, priorityY, 240, 176, 16, 16);
                break;
        }
        
        int bodyYOffset = 16;
        int leftPadding = 6;
        int rightPadding = 26;
        
        // Handle main icon
        int mainIconSW = 32;
        boolean mainIconEn = bd.mainIcon != null && bd.mainIcon.isValid();
        if (mainIconEn) {
            int iw = bd.mainIcon.texture.getWidth();
            int ih = bd.mainIcon.texture.getHeight();
            float iaspect = (float) iw / (float) ih;
            mainIconSW = (int) (32 * iaspect);
            leftPadding += Math.min(mainIconSW, 64) + 3;
            
            // Draw main icon
            guiGraphics.blit(bd.mainIcon.resource, 6, bodyYOffset, 0, 0, mainIconSW, 32, mainIconSW, 32);
        }
        
        int textZoneWidth = badgeWidth - leftPadding - rightPadding;
        
        // Handle title icon
        boolean titleIconEn = bd.titleIcon != null && bd.titleIcon.isValid();
        if (titleIconEn) {
            guiGraphics.blit(bd.titleIcon.resource, 6, 5, 0, 0, 8, 8, 8, 8);
        }
        
        // Handle title text
        String titleText = "";
        Component titleComponent = bd.getTitleProfanityFilter();
        if (titleComponent != null) {
            titleText = titleComponent.getString();
        }
        
        // Add timestamp to title
        titleText += ChatFormatting.GRAY + (titleText.length() > 0 ? " @ " : "@ ")
                + (bd.unreadFlagRender ? ChatFormatting.YELLOW : ChatFormatting.GRAY)
                + formatAge(bd.serverTimestamp);

        // Draw title
        poseStack.pushPose();
        poseStack.translate(6 + (titleIconEn ? 10 : 0), 6, 0);
        poseStack.scale(0.75f, 0.75f, 1.0f);
        guiGraphics.drawString(mc.font, titleText, 0, 0, bd.titleTxtColor, false);
        poseStack.popPose();
        
        // Handle source text
        String sourceText = null;
        Component sourceComponent = bd.getSourceProfanityFilter();
        if (sourceComponent != null) {
            sourceText = sourceComponent.getString();
            if (sourceText.isEmpty()) {
                sourceText = null;
            }
        }
        
        // Handle body text
        List<Component> bodyLines = null;
        float bodyFontSize = (sourceText != null || titleIconEn) ? 0.75f : 1.0f;
        Component bodyComponent = bd.getBodyProfanityFilter();
        
        if (bodyComponent != null) {
            bodyLines = new ArrayList<>();
            List<FormattedCharSequence> wrapped = mc.font.split(bodyComponent, (int)(textZoneWidth / bodyFontSize));
            for (FormattedCharSequence seq : wrapped) {
                bodyLines.add(Component.literal(seq.toString()));
            }
            
            int maxHeight = badgeHeight - (sourceText != null ? 32 : 22);
            int maxLines = Mth.floor(maxHeight / (9 * bodyFontSize));
            
            if (bodyLines.size() > maxLines) {
                bodyLines = new ArrayList<>(bodyLines.subList(0, maxLines));
                MutableComponent lastLine = (MutableComponent) bodyLines.get(maxLines - 1);
                lastLine.append(Component.literal("..."));
            }
        }
        
        // Clear previous cursor events
        slot.cursorEvents.clear();
        
        // Draw body text
        if (bodyLines != null && !bodyLines.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(leftPadding, bodyYOffset, 0);
            poseStack.scale(bodyFontSize, bodyFontSize, 1.0f);
            
            Component toolTip = null;
            for (int i = 0; i < bodyLines.size(); i++) {
                Component line = bodyLines.get(i);
                guiGraphics.drawString(mc.font, line, 0, i * 9, bd.bodyTxtColor, false);
                
                // Handle click and hover events
                Style style = line.getStyle();
                if (style.getClickEvent() != null || style.getHoverEvent() != null) {
                    int lineWidth = mc.font.width(line);
                    slot.cursorEvents.add(new ClickEventZone(
                        (int)(leftPadding * bodyFontSize),
                        (int)((bodyYOffset + i * 9) * bodyFontSize),
                        (int)(lineWidth * bodyFontSize),
                        (int)(9 * bodyFontSize),
                        line,
                        style.getClickEvent() != null,
                        style.getHoverEvent() != null
                    ));
                }
            }
            
            poseStack.popPose();
            
            // Handle tooltip
            if (toolTip != null) {
                parent.renderTooltip(guiGraphics, toolTip, mouseX - x, mouseY - y);
            }
        }
        
        // Draw source text if available
        if (sourceText != null) {
            poseStack.pushPose();
            poseStack.translate(badgeWidth - 21, badgeHeight - 5, 0);
            poseStack.scale(0.75f, 0.75f, 1.0f);
            int textWidth = mc.font.width(sourceText);
            guiGraphics.drawString(mc.font, sourceText, -textWidth - 4, -10, bd.sourceTxtColor, false);
            poseStack.popPose();
        }
        
        poseStack.popPose();
    }

    private String formatAge(long serverTimestamp) {
        long cur = System.currentTimeMillis();
        long daysAgo = Math.round((cur - serverTimestamp) / 86400000.0);
        String ret = dateFormat.format(new Date(serverTimestamp));
        if (daysAgo > 0L) {
            ret += " (" + daysAgo + (daysAgo == 1L ? " day" : " days") + " ago)";
        } else if (daysAgo < 0L) {
            ret += " (in " + -daysAgo + (daysAgo == -1L ? " day" : " days") + ")";
        }
        return ret;
    }

	@Override
	public int getListWidth() {
		return 220;
	}

	@Override
	public void drawScreen(int mouseXIn, int mouseYIn, float parFloat1) {
		mouseX = mouseXIn;
		mouseY = mouseYIn;
		for(int i = 0, l = currentDisplayNotifs.size(); i < l; ++i) {
			NotifBadgeSlot slot = currentDisplayNotifs.get(i);
			slot.currentScreenX = -69420;
			slot.currentScreenY = -69420;
		}
		super.drawScreen(mouseXIn, mouseYIn, parFloat1);
	}
}