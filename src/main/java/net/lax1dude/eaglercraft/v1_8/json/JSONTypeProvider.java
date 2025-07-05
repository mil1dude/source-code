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

package net.lax1dude.eaglercraft.v1_8.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;

import net.lax1dude.eaglercraft.v1_8.json.impl.JSONDataParserReader;
import net.lax1dude.eaglercraft.v1_8.json.impl.JSONDataParserStream;
import net.lax1dude.eaglercraft.v1_8.json.impl.JSONDataParserString;
import net.lax1dude.eaglercraft.v1_8.json.impl.SoundManager;

public class JSONTypeProvider {

	private static final Map<Class<?>,JSONTypeSerializer<?,?>> serializers = new HashMap<>();
	private static final Map<Class<?>,JSONTypeDeserializer<?,?>> deserializers = new HashMap<>();
	
	private static final List<JSONDataParserImpl> parsers = new ArrayList<>();

	public static <J> J serialize(Object object) throws JSONException {
		JSONTypeSerializer<Object,J> ser = (JSONTypeSerializer<Object,J>) serializers.get(object.getClass());
		if(ser == null) {
			for(Entry<Class<?>,JSONTypeSerializer<?,?>> etr : serializers.entrySet()) {
				if(etr.getKey().isInstance(object)) {
					ser = (JSONTypeSerializer<Object,J>)etr.getValue();
					break;
				}
			}
		}
		if(ser != null) {
			return ser.serializeToJson(object);
		}else {
			throw new JSONException("Could not find a serializer for " + object.getClass().getSimpleName());
		}
	}

	public static <O> O deserialize(Object object, Class<O> clazz) throws JSONException {
		return deserializeNoCast(parse(object), clazz);
	}

	public static <O> O deserializeNoCast(Object object, Class<O> clazz) throws JSONException {
		JSONTypeDeserializer<Object,O> ser = (JSONTypeDeserializer<Object,O>) deserializers.get(clazz);
		if(ser != null) {
			return (O)ser.deserializeFromJson(object);
		}else {
			throw new JSONException("Could not find a deserializer for " + object.getClass().getSimpleName());
		}
	}
	
	public static <O,J> JSONTypeSerializer<O,J> getSerializer(Class<O> object) {
		return (JSONTypeSerializer<O,J>)serializers.get(object);
	}
	
	public static <J,O> JSONTypeDeserializer<J,O> getDeserializer(Class<O> object) {
		return (JSONTypeDeserializer<J,O>)deserializers.get(object);
	}
	
	public static Object parse(Object object) {
		for(int i = 0, l = parsers.size(); i < l; ++i) {
			JSONDataParserImpl parser = parsers.get(i);
			if(parser.accepts(object)) {
				return parser.parse(object);
			}
		}
		return object;
	}
	
	public static void registerType(Class<?> clazz, Object obj) {
		boolean valid = false;
		if(obj instanceof JSONTypeSerializer<?,?>) {
			serializers.put(clazz, (JSONTypeSerializer<?,?>)obj);
			valid = true;
		}
		if(obj instanceof JSONTypeDeserializer<?,?>) {
			deserializers.put(clazz, (JSONTypeDeserializer<?,?>)obj);
			valid = true;
		}
		if(!valid) {
			throw new IllegalArgumentException("Object " + obj.getClass().getSimpleName() + " is not a JsonSerializer or JsonDeserializer object");
		}
	}
	
	public static void registerParser(JSONDataParserImpl obj) {
		parsers.add(obj);
	}
	
	static {
		
		registerType(Component.class, new Component.Serializer());
		registerType(ChatStyle.class, new ChatStyle.Serializer());
		registerType(ServerStatus.class, new ServerStatus.Serializer());
		registerType(ServerStatus.MinecraftProtocolVersionIdentifier.class,
				new ServerStatus.MinecraftProtocolVersionIdentifier.Serializer());
		registerType(ServerStatus.PlayerCountData.class,
				new ServerStatus.PlayerCountData.Serializer());
		registerType(ModelBlock.class, new ModelBlock.Deserializer());
		registerType(BlockPart.class, new BlockPart.Deserializer());
		registerType(BlockPartFace.class, new BlockPartFace.Deserializer());
		registerType(BlockFaceUV.class, new BlockFaceUV.Deserializer());
		registerType(ItemTransformVec3f.class, new ItemTransformVec3f.Deserializer());
		registerType(ItemCameraTransforms.class, new ItemCameraTransforms.Deserializer());
		registerType(ModelBlockDefinition.class, new ModelBlockDefinition.Deserializer());
		registerType(ModelBlockDefinition.Variant.class, new ModelBlockDefinition.Variant.Deserializer());
		registerType(SoundList.class, new SoundListSerializer());
		registerType(SoundManager.SoundMap.class, new SoundManager.SoundManager.SoundMapDeserializer());
		registerType(TextureMetadataSection.class, new TextureMetadataSectionSerializer());
		registerType(FontMetadataSection.class, new FontMetadataSectionSerializer());
		registerType(LanguageMetadataSection.class, new LanguageMetadataSectionSerializer());
		registerType(PackMetadataSection.class, new PackMetadataSectionSerializer());
		registerType(AnimationMetadataSection.class, new AnimationMetadataSectionSerializer());
		registerType(ChunkProviderSettings.Factory.class, new ChunkProviderSettings.Serializer());

		registerParser(new JSONDataParserString());
		registerParser(new JSONDataParserReader());
		registerParser(new JSONDataParserStream());
		
	}

}