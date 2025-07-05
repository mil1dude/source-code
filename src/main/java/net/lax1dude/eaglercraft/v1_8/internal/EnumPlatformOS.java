/*
 * Copyright (c) 2022 lax1dude. All Rights Reserved.
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

import net.minecraft.util.Mth;

public enum EnumPlatformOS {
	WINDOWS("Windows"),
	MACOS("MacOS"),
	LINUX("Linux"),
	CHROMEBOOK_LINUX("ChromeOS"),
	OTHER("Unknown");

	private final String name;
	
	private EnumPlatformOS(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return name;
	}
	
	public static EnumPlatformOS getFromJVM(String osNameProperty) {
		if(osNameProperty == null) {
			return OTHER;
		}
		String osName = osNameProperty.toLowerCase();
		if (osName.contains("chrome")) {
			return CHROMEBOOK_LINUX;
		}
		if (osName.contains("win")) {
			return WINDOWS;
		}
		if (osName.contains("mac")) {
			return MACOS;
		}
		if (osName.contains("linux") || osName.contains("unix") || 
				osName.contains("solaris") || osName.contains("sunos")) {
			return LINUX;
		}
		return OTHER;
	}
	
	public static EnumPlatformOS getFromUA(String ua) {
		if(ua == null) {
			return OTHER;
		}
		String uaLower = " " + ua.toLowerCase() + " ";
		if(uaLower.contains(" cros ") || uaLower.contains("chrome")) {
			return CHROMEBOOK_LINUX;
		}
		if(uaLower.contains(" win")) {
			return WINDOWS;
		}
		if(uaLower.contains(" mac ") || uaLower.contains(" os x ")) {
			return MACOS;
		}
		if(uaLower.contains(" linux") || uaLower.contains(" unix") || 
		   uaLower.contains(" bsd") || uaLower.contains(" sunos") ||
		   uaLower.contains(" x11 ") || uaLower.contains(" xorg ")) {
			return LINUX;
		}
		return OTHER;
	}
	
}