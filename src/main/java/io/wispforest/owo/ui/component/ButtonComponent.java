package io.wispforest.owo.ui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.owo.ui.util.OwoNinePatchRenderers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.function.Consumer;

public class ButtonComponent extends ButtonWidget {

    protected Renderer renderer = Renderer.VANILLA;
    protected boolean textShadow = true;

    protected ButtonComponent(Text message, Consumer<ButtonComponent> onPress) {
        super(0, 0, 0, 0, message, button -> onPress.accept((ButtonComponent) button));
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderer.draw(matrices, this, delta);

        var textRenderer = MinecraftClient.getInstance().textRenderer;
        int color = this.active ? 0xffffff : 0xa0a0a0;

        if (this.textShadow) {
            Drawer.drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, color);
        } else {
            textRenderer.draw(matrices, this.getMessage(), this.x + this.width / 2f - textRenderer.getWidth(this.getMessage()) / 2f, this.y + (this.height - 8) / 2f, color);
        }

        if (this.hovered) this.renderTooltip(matrices, mouseX, mouseY);
    }

    public ButtonComponent renderer(Renderer renderer) {
        this.renderer = renderer;
        return this;
    }

    public Renderer renderer() {
        return this.renderer;
    }

    public ButtonComponent textShadow(boolean textShadow) {
        this.textShadow = textShadow;
        return this;
    }

    public boolean textShadow() {
        return this.textShadow;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "text-shadow", UIParsing::parseBool, this::textShadow);
    }

    @FunctionalInterface
    public interface Renderer {
        Renderer VANILLA = (matrices, button, delta) -> {
            if (button.active) {
                if (button.hovered) {
                    OwoNinePatchRenderers.HOVERED_BUTTON.draw(matrices, button.x, button.y, button.width, button.height);
                } else {
                    OwoNinePatchRenderers.ACTIVE_BUTTON.draw(matrices, button.x, button.y, button.width, button.height);
                }
            } else {
                OwoNinePatchRenderers.BUTTON_DISABLED.draw(matrices, button.x, button.y, button.width, button.height);
            }
        };

        static Renderer flat(int color, int hoveredColor, int disabledColor) {
            return (matrices, button, delta) -> {
                if (button.active) {
                    if (button.hovered) {
                        Drawer.fill(matrices, button.x, button.y, button.x + button.width, button.y + button.height, hoveredColor);
                    } else {
                        Drawer.fill(matrices, button.x, button.y, button.x + button.width, button.y + button.height, color);
                    }
                } else {
                    Drawer.fill(matrices, button.x, button.y, button.x + button.width, button.y + button.height, disabledColor);
                }
            };
        }

        static Renderer texture(Identifier texture, int u, int v, int textureWidth, int textureHeight) {
            return (matrices, button, delta) -> {
                int renderV = v;
                if (!button.active) {
                    renderV += button.height * 2;
                } else if (button.isHovered()) {
                    renderV += button.height;
                }

                RenderSystem.enableDepthTest();
                RenderSystem.setShaderTexture(0, texture);
                Drawer.drawTexture(matrices, button.x, button.y, u, renderV, button.width, button.height, textureWidth, textureHeight);
            };
        }

        void draw(MatrixStack matrices, ButtonComponent button, float delta);
    }
}
