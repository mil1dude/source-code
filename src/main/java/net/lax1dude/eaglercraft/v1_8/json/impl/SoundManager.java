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

package net.lax1dude.eaglercraft.v1_8.json.impl;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import net.lax1dude.eaglercraft.v1_8.json.JSONTypeDeserializer;
import net.lax1dude.eaglercraft.v1_8.json.JSONTypeProvider;
import net.minecraft.client.sounds.SoundEngine; // Updated to MCP Reborn 1.21.4 package
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.sounds.SoundEvent; // Using SoundEvent instead of SoundList in 1.21.4
import net.minecraft.resources.ResourceLocation;

public class SoundManager implements JSONTypeDeserializer<JSONObject, SoundManager.SoundMap> {

    public static class SoundMap {
        private final Map<ResourceLocation, SoundEvent> soundsMap;

        public SoundMap(Map<ResourceLocation, SoundEvent> soundsMap) {
            this.soundsMap = soundsMap;
        }

        public Map<ResourceLocation, SoundEvent> getSoundsMap() {
            return soundsMap;
        }
    }

    @Override
    public SoundManager.SoundMap deserialize(JSONObject json) throws JSONException {
        Map<ResourceLocation, SoundEvent> soundsMap = new HashMap<>();
        for(String key : json.keySet()) {
            ResourceLocation location = new ResourceLocation(key);
            SoundEvent event = new SoundEvent(location);
            soundsMap.put(location, event);
        }
        return new SoundManager.SoundMap(soundsMap);
    }

}