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

package net.lax1dude.eaglercraft.v1_8.cookie;

import org.json.JSONObject;

import net.lax1dude.eaglercraft.v1_8.cookie.ServerCookieDataStore.ServerCookie;
import net.lax1dude.eaglercraft.v1_8.internal.IServerQuery;
import net.lax1dude.eaglercraft.v1_8.internal.QueryResponse;
import net.lax1dude.eaglercraft.v1_8.log4j.LogManager;
import net.lax1dude.eaglercraft.v1_8.log4j.Logger;
import net.lax1dude.eaglercraft.v1_8.minecraft.ScreenGenericErrorMessage;
import net.lax1dude.eaglercraft.v1_8.socket.ServerQueryDispatch;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ScreenSendRevokeRequest extends Screen {

    private static final Logger logger = LogManager.getLogger("SessionRevokeRequest");

    private final Screen parent;
    private final ServerCookie cookie;
    private String title;
    private String message;
    private int timer = 0;
    private boolean cancelRequested = false;
    private IServerQuery query = null;
    private boolean hasSentPacket = false;

    public ScreenSendRevokeRequest(Screen parent, ServerCookie cookie) {
        super(Component.literal("Revoking Session Token"));
        this.parent = parent;
        this.cookie = cookie;
        this.title = "Revoking Session Token";
        this.message = "Connecting to " + cookie.server + "...";
    }

    @Override
    protected void init() {
        this.clearWidgets();
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> {
            cancelRequested = true;
            button.active = false;
        }).bounds(this.width / 2 - 100, this.height / 6 + 96, 200, 20).build());
    }

    // Button actions are now handled in the button builder lambda

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        drawCenteredString(this.font, title, this.width / 2, 70, 0xAAAAAA);
        drawCenteredString(this.font, message, this.width / 2, 90, 0xFFFFFF);
        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public void tick() {
        ++timer;
        if (timer > 1) {
            if(query == null) {
                logger.info("Attempting to revoke session tokens for: {}", cookie.server);
                query = ServerQueryDispatch.sendServerQuery(cookie.server, "revoke_session_token");
                if(query == null) {
                    this.minecraft.setScreen(new ScreenGenericErrorMessage(Component.literal("Connection Error"), Component.literal("Failed to connect to server"), parent));
                    return;
                }
            } else {
                query.update();
                QueryResponse resp = query.getResponse();
                if(resp != null) {
                    if(resp.responseType.equalsIgnoreCase("revoke_session_token") && (hasSentPacket ? resp.isResponseJSON() : resp.isResponseString())) {
                        if(!hasSentPacket) {
                            String str = resp.getResponseString();
                            if("ready".equalsIgnoreCase(str)) {
                                hasSentPacket = true;
                                message = "Sending revocation request...";
                                query.send(cookie.cookie);
                                return;
                            } else {
                                this.mc.displayGuiScreen(new ScreenGenericErrorMessage("Error", "Invalid server response", parent));
                                return;
                            }
                        } else {
                            JSONObject json = resp.getResponseJSON();
                            String stat = json.optString("status");
                            if("ok".equalsIgnoreCase(stat)) {
                                if(hasSentPacket) {
                                    query.close();
                                    this.minecraft.setScreen(new ScreenGenericErrorMessage("Success", Component.literal("Session token successfully revoked"), parent));
                                    ServerCookieDataStore.clearCookie(cookie.server);
                                    return;
                                } else {
                                    query.close();
                                    this.mc.displayGuiScreen(new ScreenGenericErrorMessage("Error", "Invalid server response", parent));
                                    return;
                                }
                            } else if("error".equalsIgnoreCase(stat)) {
                                int code = json.optInt("code", -1);
                                if(code == -1) {
                                    query.close();
                                    this.mc.displayGuiScreen(new ScreenGenericErrorMessage("Error", "Invalid server response", parent));
                                    return;
                                } else {
                                    String errorMsg;
                                    switch(code) {
                                        case 1: errorMsg = "Session revocation not supported"; break;
                                        case 2: errorMsg = "Not allowed to revoke session"; break;
                                        case 3: errorMsg = "Session not found"; break;
                                        case 4: errorMsg = "Server error"; break;
                                        default: errorMsg = "Error code: " + code;
                                    }
                                    logger.error("Received error code {}: {}", code, errorMsg);
                                    query.close();
                                    this.minecraft.setScreen(new ScreenGenericErrorMessage(Component.literal("Error"), Component.literal("Server error: " + errorMsg), parent));
                                    if(json.optBoolean("delete", false)) {
                                        ServerCookieDataStore.clearCookie(cookie.server);
                                    }
                                    return;
                                }
                            } else {
                                logger.error("Received unknown status: {}", stat);
                                query.close();
                                this.minecraft.setScreen(new ScreenGenericErrorMessage(Component.literal("Error"), Component.literal("Invalid server response"), parent));
                                return;
                            }
                        }
                    } else {
                        query.close();
                        this.minecraft.setScreen(new ScreenGenericErrorMessage(Component.literal("Error"), Component.literal("Invalid server response"), parent));
                        return;
                    }
                }
                
                if(query.isClosed()) {
                    if(!hasSentPacket || query.responsesAvailable() == 0) {
                        this.minecraft.setScreen(new ScreenGenericErrorMessage(Component.literal("Connection Error"), Component.literal("Connection to server lost"), parent));
                        return;
                    }
                } else {
                    if(timer > 400) {
                        query.close();
                        this.minecraft.setScreen(new ScreenGenericErrorMessage(Component.literal("Timeout"), Component.literal("Connection timed out"), parent));
                        return;
                    }
                }
                
                if(cancelRequested) {
                    query.close();
                    this.minecraft.setScreen(new ScreenGenericErrorMessage(Component.literal("Cancelled"), Component.literal("Operation cancelled by user"), parent));

                    return;
                }
            }
        }
    }
}
