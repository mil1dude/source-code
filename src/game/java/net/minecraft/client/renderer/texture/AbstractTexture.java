package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.TriState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractTexture implements AutoCloseable {
    public static final int NOT_ASSIGNED = -1;
    protected int id = -1;
    protected boolean defaultBlur;
    private int wrapS = 10497;
    private int wrapT = 10497;
    private int minFilter = 9986;
    private int magFilter = 9729;

    public void setClamp(boolean p_377282_) {
        RenderSystem.assertOnRenderThreadOrInit();
        int i;
        int j;
        if (p_377282_) {
            i = 33071;
            j = 33071;
        } else {
            i = 10497;
            j = 10497;
        }

        boolean flag = this.wrapS != i;
        boolean flag1 = this.wrapT != j;
        if (flag || flag1) {
            this.bind();
            if (flag) {
                GlStateManager._texParameter(3553, 10242, i);
                this.wrapS = i;
            }

            if (flag1) {
                GlStateManager._texParameter(3553, 10243, j);
                this.wrapT = j;
            }
        }
    }

    public void setFilter(TriState p_377375_, boolean p_378683_) {
        this.setFilter(p_377375_.toBoolean(this.defaultBlur), p_378683_);
    }

    public void setFilter(boolean p_117961_, boolean p_117962_) {
        RenderSystem.assertOnRenderThreadOrInit();
        int i;
        int j;
        if (p_117961_) {
            i = p_117962_ ? 9987 : 9729;
            j = 9729;
        } else {
            i = p_117962_ ? 9986 : 9728;
            j = 9728;
        }

        boolean flag = this.minFilter != i;
        boolean flag1 = this.magFilter != j;
        if (flag1 || flag) {
            this.bind();
            if (flag) {
                GlStateManager._texParameter(3553, 10241, i);
                this.minFilter = i;
            }

            if (flag1) {
                GlStateManager._texParameter(3553, 10240, j);
                this.magFilter = j;
            }
        }
    }

    public int getId() {
        RenderSystem.assertOnRenderThreadOrInit();
        if (this.id == -1) {
            this.id = TextureUtil.generateTextureId();
        }

        return this.id;
    }

    public void releaseId() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                if (this.id != -1) {
                    TextureUtil.releaseTextureId(this.id);
                    this.id = -1;
                }
            });
        } else if (this.id != -1) {
            TextureUtil.releaseTextureId(this.id);
            this.id = -1;
        }
    }

    public void bind() {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> GlStateManager._bindTexture(this.getId()));
        } else {
            GlStateManager._bindTexture(this.getId());
        }
    }

    @Override
    public void close() {
    }
}