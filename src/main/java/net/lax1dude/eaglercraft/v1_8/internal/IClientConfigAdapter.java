/*
 * Copyright (c) 2022-2024 lax1dude. All Rights Reserved.
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

package net.lax1dude.eaglercraft.v1_8.internal;

import java.util.List;

import net.lax1dude.eaglercraft.v1_8.sp.relay.RelayEntry;
import org.json.JSONObject;

public interface IClientConfigAdapter {

	public static class DefaultServer {

		public final String name;
		public final String addr;
		public final boolean hideAddress;

		public DefaultServer(String name, String addr, boolean hideAddress) {
			this.name = name;
			this.addr = addr;
			this.hideAddress = hideAddress;
		}

	}

	String getDefaultLocale();

	List<DefaultServer> getDefaultServerList();

	String getServerToJoin();

	String getLevelsDB();

	String getResourcePacksDB();

	JSONObject getIntegratedServerOpts();

	List<RelayEntry> getRelays();

	boolean isCheckGLErrors();

	boolean isCheckShaderGLErrors();

	boolean isDemo();

	boolean allowUpdateSvc();

	boolean allowUpdateDL();

	boolean isEnableDownloadOfflineButton();

	String getDownloadOfflineButtonLink();

	boolean useSpecialCursors();

	boolean isLogInvalidCerts();

	boolean isCheckRelaysForUpdates();

	boolean isEnableSignatureBadge();

	boolean isAllowVoiceClient();

	boolean isAllowFNAWSkins();

	String getLocalStorageNamespace();

	boolean isEnableMinceraft();

	boolean isEnableServerCookies();

	boolean isAllowServerRedirects();

	boolean isOpenDebugConsoleOnLaunch();

	boolean isForceWebViewSupport();

	boolean isEnableWebViewCSP();

	boolean isAllowBootMenu();

	boolean isForceProfanityFilter();

	boolean isEaglerNoDelay();

	boolean isRamdiskMode();

	boolean isEnforceVSync();

	IClientConfigAdapterHooks getHooks();

}