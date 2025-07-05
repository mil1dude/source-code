/*
 * Copyright (c) 2022-2023 lax1dude, ayunami2000. All Rights Reserved.
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

package net.lax1dude.eaglercraft.v1_8.profile;

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.opengl.EaglerMeshLoader;
import net.lax1dude.eaglercraft.v1_8.opengl.EaglercraftGPU;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

// Simple model interface for skin rendering
interface ISkinModel {
    void render(float scale, boolean isSneaking, boolean isRiding, float limbSwing, float limbSwingAmount, 
               float ageInTicks, float netHeadYaw, float headPitch);
    void setTexture(ResourceLocation texture);
    void setCapeTexture(ResourceLocation capeTexture);
}

// Simple model implementation
class SimplePlayerModel implements ISkinModel {
    private ResourceLocation texture;
    private ResourceLocation capeTexture;
    
    @Override
    public void render(float scale, boolean isSneaking, boolean isRiding, float limbSwing, float limbSwingAmount, 
                      float ageInTicks, float netHeadYaw, float headPitch) {
        // Basic player model rendering logic would go here
        // This is a simplified placeholder
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        
        // Render body parts here (simplified)
        renderBox(-4.0f, 0.0f, -2.0f, 8, 12, 4, 0.0625f); // Body
        renderBox(-4.0f, 0.0f, -2.0f, 8, 12, 4, 0.25f); // Overlay
        
        GlStateManager.popMatrix();
    }
    
    private void renderBox(float x, float y, float z, float width, float height, float depth, float scale) {
        // Simple box rendering
        GlStateManager.pushMatrix();
        GlStateManager.translate(x * scale, y * scale, z * scale);
        GlStateManager.scale(width * scale, height * scale, depth * scale);
        
        // Simple cube rendering (would be replaced with actual model rendering)
        GlStateManager.beginQuads();
        // Front
        GlStateManager.vertex(0.0f, 0.0f, 0.0f);
        GlStateManager.vertex(1.0f, 0.0f, 0.0f);
        GlStateManager.vertex(1.0f, 1.0f, 0.0f);
        GlStateManager.vertex(0.0f, 1.0f, 0.0f);
        // Back
        GlStateManager.vertex(0.0f, 0.0f, 1.0f);
        GlStateManager.vertex(0.0f, 1.0f, 1.0f);
        GlStateManager.vertex(1.0f, 1.0f, 1.0f);
        GlStateManager.vertex(1.0f, 0.0f, 1.0f);
        // ... other faces ...
        GlStateManager.endQuads();
        
        GlStateManager.popMatrix();
    }
    
    @Override
    public void setTexture(ResourceLocation texture) {
        this.texture = texture;
    }
    
    @Override
    public void setCapeTexture(ResourceLocation capeTexture) {
        this.capeTexture = capeTexture;
    }
}

public class SkinPreviewRenderer {

	private static ISkinModel playerModelSteve = null;
	private static ISkinModel playerModelAlex = null;
	private static ISkinModel playerModelZombie = null;
	private static boolean initialized = false;
	
	public static void initialize() {
		if (initialized) return;
		
		try {
			playerModelSteve = new SimplePlayerModel();
			playerModelAlex = new SimplePlayerModel();
			playerModelZombie = new SimplePlayerModel();
			initialized = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void renderPreview(int x, int y, int mx, int my, SkinModel skinModel) {
		renderPreview(x, y, mx, my, false, skinModel, null, null);
	}

	public static void renderPreview(int x, int y, int mx, int my, boolean capeMode, SkinModel skinModel, ResourceLocation skinTexture, ResourceLocation capeTexture) {
		ISkinModel model;
		switch(skinModel) {
		case STEVE:
		default:
			model = playerModelSteve;
			break;
		case ALEX:
			model = playerModelAlex;
			break;
		case ZOMBIE:
			model = playerModelZombie;
			break;
		case LONG_ARMS:
		case WEIRD_CLIMBER_DUDE:
		case LAXATIVE_DUDE:
		case BABY_CHARLES:
		case BABY_WINSTON:
			if(skinModel.highPoly != null && Minecraft.getMinecraft().gameSettings.enableFNAWSkins) {
				renderHighPoly(x, y, mx, my, skinModel.highPoly);
				return;
			}
			model = playerModelSteve;
			break;
		}
		
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.disableCull();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y - 80.0f, 100.0f);
		GlStateManager.scale(50.0f, 50.0f, 50.0f);
		GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);
		GlStateManager.scale(1.0f, -1.0f, 1.0f);
		
		RenderHelper.enableGUIStandardItemLighting();
		
		GlStateManager.translate(0.0f, 1.0f, 0.0f);
		if(capeMode) {
			GlStateManager.rotate(140.0f, 0.0f, 1.0f, 0.0f);
			mx = x - (x - mx) - 20;
			GlStateManager.rotate(((y - my) * -0.02f), 1.0f, 0.0f, 0.0f);
		}else {
			GlStateManager.rotate(((y - my) * -0.06f), 1.0f, 0.0f, 0.0f);
		}
		GlStateManager.rotate(((x - mx) * 0.06f), 0.0f, 1.0f, 0.0f);
		GlStateManager.translate(0.0f, -1.0f, 0.0f);
		
		if(skinTexture != null) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(skinTexture);
		}
		
		if (model != null) {
			model.render(0.0625f, false, false, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
		}
		
		if(capeTexture != null && model != null) {
			// Render cape if needed
			Minecraft.getMinecraft().getTextureManager().bindTexture(capeTexture);
			// Simplified cape rendering would go here
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0F, 0.0F, 0.125F);
			GlStateManager.rotate(6.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
			// Add your simplified cape rendering code here
			GlStateManager.popMatrix();
		}
		
		GlStateManager.popMatrix();
		GlStateManager.disableLighting();
	}

	private static void renderHighPoly(int x, int y, int mx, int my, HighPolySkin msh) {
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.disableCull();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y - 80.0f, 100.0f);
		GlStateManager.scale(50.0f, 50.0f, 50.0f);
		GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);
		GlStateManager.scale(1.0f, -1.0f, 1.0f);
		
		RenderHelper.enableGUIStandardItemLighting();
		
		GlStateManager.translate(0.0f, 1.0f, 0.0f);
		GlStateManager.rotate(((y - my) * -0.06f), 1.0f, 0.0f, 0.0f);
		GlStateManager.rotate(((x - mx) * 0.06f), 0.0f, 1.0f, 0.0f);
		GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
		GlStateManager.translate(0.0f, -0.6f, 0.0f);
		
		GlStateManager.scale(HighPolySkin.highPolyScale, HighPolySkin.highPolyScale, HighPolySkin.highPolyScale);
		Minecraft.getMinecraft().getTextureManager().bindTexture(msh.texture);
		
		if(msh.bodyModel != null) {
			EaglercraftGPU.drawHighPoly(EaglerMeshLoader.getEaglerMesh(msh.bodyModel));
		}
		
		if(msh.headModel != null) {
			EaglercraftGPU.drawHighPoly(EaglerMeshLoader.getEaglerMesh(msh.headModel));
		}
		
		if(msh.limbsModel != null && msh.limbsModel.length > 0) {
			for(int i = 0; i < msh.limbsModel.length; ++i) {
				float offset = 0.0f;
				if(msh.limbsOffset != null) {
					if(msh.limbsOffset.length == 1) {
						offset = msh.limbsOffset[0];
					}else {
						offset = msh.limbsOffset[i];
					}
				}
				if(offset != 0.0f || msh.limbsInitialRotation != 0.0f) {
					GlStateManager.pushMatrix();
					if(offset != 0.0f) {
						GlStateManager.translate(0.0f, offset, 0.0f);
					}
					if(msh.limbsInitialRotation != 0.0f) {
						GlStateManager.rotate(msh.limbsInitialRotation, 1.0f, 0.0f, 0.0f);
					}
				}
				
				EaglercraftGPU.drawHighPoly(EaglerMeshLoader.getEaglerMesh(msh.limbsModel[i]));
				
				if(offset != 0.0f || msh.limbsInitialRotation != 0.0f) {
					GlStateManager.popMatrix();
				}
			}
		}

		GlStateManager.popMatrix();
		GlStateManager.disableLighting();
	}

}