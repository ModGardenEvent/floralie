package dev.hephaestus.glowcase.client.gui.widget.ingame;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import org.jetbrains.annotations.NotNull;

public class SuggestionListWidget<T> extends ClickableWidget {
    private final TextRenderer textRenderer;
    private final MinecraftClient client;

    private final List<T> suggestions = new ArrayList<>();
	private @NotNull String filter = "";
    private int scrollOffset = 0;

    private final int baseLineHeight;
    private final int padding;
    private final int maxRows;
	/**
	 * The approximate maximum number of characters that'll fit inside the width of this widget
	 */
	private int characterWidth;

    private final Consumer<T> onSelect;
    private final Function<T, String> toStringFunction;
    
    public boolean draggingScrollbar = false;
    private int scrollbarDragStartY = 0;
    private int initialScrollOffset = 0;

    public SuggestionListWidget(TextRenderer textRenderer, int x, int y, int width, int height, int baseLineHeight, int padding, int maxRows, Consumer<T> onSelect, Function<T, String> toStringFunction) {
        super(x, y, width, height, Text.empty());

        this.client = MinecraftClient.getInstance();

        this.baseLineHeight = baseLineHeight;
        this.padding = padding;
        this.maxRows = maxRows;
        this.onSelect = onSelect;
        this.toStringFunction = toStringFunction;
        this.textRenderer = textRenderer;
		this.characterWidth = 1;
		setWidth(width);
    }

	public static <T> SuggestionListWidget<T> forTextField(TextFieldWidget textField, TextRenderer textRenderer, Function<T, String> toStringFunction) {
		return new SuggestionListWidget<>(textRenderer, textField.getX(), textField.getY() + textField.getHeight() + 5, textField.getWidth(), 100, 10, 4, 5,
			a -> textField.setText(toStringFunction.apply(a)), toStringFunction);
	}

	public static <T> SuggestionListWidget<T> forTextFieldWithStaticSuggestions(TextFieldWidget textField, TextRenderer textRenderer, List<T> suggestions, Function<T, String> toStringFunction) {
		var suggestionWidget = forTextField(textField, textRenderer, toStringFunction);

		textField.setChangedListener((text) -> {
			suggestionWidget.updateSuggestions(suggestions, text);
		});

		return suggestionWidget;
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		while (textRenderer.getWidth("m".repeat(characterWidth)) < this.width) {
			characterWidth++;
		}
	}

	public void updateSuggestions(List<T> newSuggestions, String filter) {
		updateSuggestions(newSuggestions, filter, true);
	}

	// update the suggestion list based on filter
    public void updateSuggestions(List<T> newSuggestions, String filter, boolean strict) {
        suggestions.clear();
		this.filter = filter;

        for (T suggestion : newSuggestions) {
			if (suggestion == null) {
				throw new NullPointerException("A suggestion can not be null!");
			}
            String text = toStringFunction.apply(suggestion);

            if (strict ? text.startsWith(filter) : text.contains(filter)) {
                suggestions.add(suggestion);
            }
        }

        // if there is only 1 suggestion & it's equal to input, hide the list
        if (suggestions.size() == 1 && toStringFunction.apply(suggestions.getFirst()).equals(filter)) {
            suggestions.clear();
        }

        scrollOffset = 0;

		if (suggestions.isEmpty()) {
			FRAMEBUFFER.resize(1, 1);
		}
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (suggestions.isEmpty()) return;

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 1);

        int adjustedLineHeight = baseLineHeight + padding * 2;
        int rows = Math.min(suggestions.size(), maxRows);
        int dynamicHeight = rows * adjustedLineHeight;

        boolean scrollable = suggestions.size() > maxRows;
        int totalLines = suggestions.size();

		int listWidth = scrollable ? this.getWidth() - 5 - 10 : this.getWidth();

		int x = SuggestionListWidget.this.getX();
		int y = SuggestionListWidget.this.getY();
		context.enableScissor(x, y, x + listWidth, y + dynamicHeight);

        float blurValue = (float) MinecraftClient.getInstance().options.getMenuBackgroundBlurrinessValue();
        if (blurValue >= 1.0F) {
            client.gameRenderer.renderBlur();
        }
		context.fill(x, y, x + listWidth, y + dynamicHeight, bgColor);

        drawOutline(context, x, y, listWidth, dynamicHeight, 0xFFFFFFFF);

        if (scrollOffset > totalLines - rows) {
            scrollOffset = Math.max(0, totalLines - rows);
        }
        
        // render each suggestion
        for (int i = 0; i < rows; i++) {
            int suggestionIndex = i + scrollOffset;
            if (suggestionIndex >= totalLines) break;
            
            T suggestion = suggestions.get(suggestionIndex);
            String suggestionText = toStringFunction.apply(suggestion);
            int suggestionY = y + i * adjustedLineHeight;
            
            // highlight hovered suggestion
			boolean hover = mouseX >= x && mouseX <= x + listWidth && mouseY >= suggestionY && mouseY < suggestionY + adjustedLineHeight;
			if (hover) {
                context.fill(x, suggestionY, x + listWidth, suggestionY + adjustedLineHeight, 0xFF217C08);
                drawOutline(context, x, suggestionY, listWidth, adjustedLineHeight, 0xFFFFFFFF);
            }

            // detect if the text is too long AND if the item is hovered, then scroll, otherwise don't
            if (textRenderer.getWidth(suggestionText) > (this.getWidth() - padding - 20)) {
                drawOverflowText(context, textRenderer, Text.literal(suggestionText), x + padding, suggestionY + padding - 2, x + listWidth - padding, suggestionY + adjustedLineHeight, 0xFFFFFFFF, hover);
            } else {
                context.drawTextWithShadow(textRenderer, Text.literal(suggestionText), x + padding, suggestionY + padding + 1, 0xFFFFFFFF);
            }
        }

        context.disableScissor();

        // scrollbar thingy
        if (scrollable) {
            int scrollbarWidth = 10;

            int sbX = x + listWidth + 5;

			context.enableScissor(sbX, y, sbX + scrollbarWidth, y + dynamicHeight);

            float blurScrollbar = (float) client.options.getMenuBackgroundBlurrinessValue();
            if (blurScrollbar >= 1.0F) {
                client.gameRenderer.renderBlur();
            }

            context.fill(sbX, sbY, sbX + scrollbarWidth, sbY + scrollbarHeight, scrollBarBgColor);
            context.disableScissor();

            drawOutline(context, sbX, y, scrollbarWidth, dynamicHeight, 0xFFFFFFFF);

            float visibleRatio = (float) rows / totalLines;
            int handleHeight = Math.max((int)(visibleRatio * (dynamicHeight - 2 * 2)), 4);

            int availableScroll = totalLines - rows;
            int handleYOffset = availableScroll > 0 ? (int)(((float)scrollOffset / availableScroll) * ((dynamicHeight - 2 * 2) - handleHeight)) : 0;
            int handleX = sbX + 2;
            int handleY = y + 2 + handleYOffset;
            int handleWidth = scrollbarWidth - 2 * 2;
            
            context.fill(handleX, handleY, handleX + handleWidth, handleY + handleHeight, 0xFFFFFFFF);
        }

        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean scrollable = suggestions.size() > maxRows;

        int adjustedLineHeight = baseLineHeight + padding * 2;
        int listWidth = scrollable ? this.getWidth() - 5 - 10 : this.getWidth();

        int relativeY = (int)mouseY - this.getY();
        int clickedIndex = relativeY / adjustedLineHeight + scrollOffset;

        if (scrollable) {
            int sbX = getX() + listWidth + 5;
            int sbY = getY();
            int scrollbarHeight = Math.min(suggestions.size(), maxRows) * adjustedLineHeight;

            if (mouseX >= sbX && mouseX <= sbX + 10 && mouseY >= sbY && mouseY <= sbY + scrollbarHeight) {
                draggingScrollbar = true;

                scrollbarDragStartY = (int) mouseY;
                initialScrollOffset = scrollOffset;

                return true;
            }
        }

        if (mouseX >= this.getX() && mouseX <= this.getX() + listWidth) {
            if (clickedIndex >= 0 && clickedIndex < suggestions.size()) {
                onSelect.accept(suggestions.get(clickedIndex));
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingScrollbar) {
            int adjustedLineHeight = baseLineHeight + padding * 2;
            int rows = Math.min(suggestions.size(), maxRows);

            int dynamicHeight = rows * adjustedLineHeight;
            int totalLines = suggestions.size();
            int availableScroll = totalLines - maxRows;

            float visibleRatio = (float) maxRows / totalLines;
            int handleHeight = Math.max((int)(visibleRatio * (dynamicHeight - 2 * 2)), 4);

            int dragDelta = (int) (mouseY - scrollbarDragStartY);

            if ((dynamicHeight - 2 * 2) - handleHeight > 0) {
                int newOffset = initialScrollOffset + (int) ((float) dragDelta / ((dynamicHeight - 2 * 2) - handleHeight) * availableScroll);
                scrollOffset = Math.max(0, Math.min(newOffset, availableScroll));
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int rows = Math.min(suggestions.size(), maxRows);
        
        int totalLines = suggestions.size();
        int maxLines = rows;
        scrollOffset -= (int) verticalAmount;
        
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > totalLines - maxLines) scrollOffset = Math.max(0, totalLines - maxLines);
        
        return true;
    }
    
    @Override
    protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {}
    
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        int adjustedLineHeight = baseLineHeight + padding * 2;
        int rows = Math.min(suggestions.size(), maxRows);
        int dynamicHeight = rows * adjustedLineHeight;

        boolean scrollable = suggestions.size() > maxRows;
        int listWidth = scrollable ? this.getWidth() - 5 - 10 : this.getWidth();

        boolean overList = (mouseX >= this.getX() && mouseX <= this.getX() + listWidth && mouseY >= this.getY() && mouseY < this.getY() + dynamicHeight);
        boolean overScrollbar = false;

        if (scrollable) {
            int sbX = this.getX() + listWidth + 5;
            int sbY = this.getY();

            overScrollbar = (mouseX >= sbX && mouseX <= sbX + 10 && mouseY >= sbY && mouseY <= sbY + dynamicHeight);
        }

        return overList || overScrollbar;
    }

    private void drawOutline(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    // similar to drawScrollableText but not centered
    private void drawOverflowText(DrawContext context, TextRenderer textRenderer, Text text, int startX, int startY, int endX, int endY, int color, boolean hovered) {
        int textRendererWidth = textRenderer.getWidth(text);
        int availableWidth = endX - startX;
        int y = startY + ((endY - startY) - 9) / 2;
    
        // if hovered, we scroll
        if (hovered) {
            int extra = textRendererWidth - availableWidth;
            double time = Util.getMeasuringTimeMs() / 1000.0;
            double period = Math.max(extra / 8.0, 2.0);
            double scroll = 0.5 - 0.5 * Math.cos(2 * Math.PI * time / period);
            int offset = (int)(scroll * extra);
            
            context.enableScissor(startX, startY, endX, endY);
            context.drawTextWithShadow(textRenderer, text, startX - offset, y, color);
            context.disableScissor();
        } else {
            // otherwise try to shorten the text as much as possible
            String rawText = text.getString();

            String collapsedText;
            int colonIndex = rawText.indexOf(':');

            // if no colon, prob nothing to collapse
            if (colonIndex == -1) {
                collapsedText = rawText;
            } else {
                int lastSlashIndex = rawText.lastIndexOf('/');

                // if no slash after colon, leave as-is
                if (lastSlashIndex == -1 || lastSlashIndex < colonIndex) {
                    collapsedText = rawText;
                } else {
                    String namespace = rawText.substring(0, colonIndex + 1);
                    String lastPart = rawText.substring(lastSlashIndex + 1);

                    collapsedText = namespace + ".../" + lastPart;
                }
            }

            String finalText;
            if (filter.trim().isEmpty()) {
                finalText = collapsedText;  
            } else if (filter.length() >= (rawText.indexOf(':') + 1)) {
                if (rawText.lastIndexOf('/') == -1) {
                    //... and no slash, put ... before
                    finalText = "..." + collapsedText.substring(rawText.indexOf(':') + 1);
                } else {
                    // no namespace
                    finalText = collapsedText.substring(rawText.indexOf(':') + 1);
                }
            } else if (filter.length() > 1) {
                int removeCount = Math.min(filter.length(), collapsedText.length());
                finalText = "..." + collapsedText.substring(removeCount);
            } else {
                finalText = collapsedText;
            }

            if (filter.trim().isEmpty()) {
                int slashIndex = collapsedText.lastIndexOf("/");

                if (slashIndex != -1) {
                    String prefix = collapsedText.substring(0, slashIndex + 1);
                    String lastPart = collapsedText.substring(slashIndex + 1);

                    if (textRenderer.getWidth(collapsedText) > availableWidth) {
                        int prefixWidth = textRenderer.getWidth(prefix);
                        int allowedForLast = availableWidth - prefixWidth;

                        if (allowedForLast < 0) {
                            finalText = trimToWidth(collapsedText, availableWidth, textRenderer);
                        } else {
                            if (textRenderer.getWidth(lastPart) > allowedForLast) {
                                lastPart = trimToWidth(lastPart, allowedForLast, textRenderer);
                            }

                            finalText = prefix + lastPart;
                        }
                    }
                } else {
                    finalText = trimToWidth(collapsedText, availableWidth, textRenderer);
                }
            } else {
                if (textRenderer.getWidth(finalText) > availableWidth) {
                    finalText = trimToWidth(finalText, availableWidth, textRenderer);
                }
            }

            context.enableScissor(startX, startY, endX, endY);
            context.drawTextWithShadow(textRenderer, Text.literal(finalText), startX, y, color);
            context.disableScissor();
        }
    }

    private String trimToWidth(String rawText, int availableWidth, TextRenderer textRenderer) {
        if (textRenderer.getWidth(rawText) <= availableWidth) {
            return rawText;
        }

        int maxWidth = availableWidth - textRenderer.getWidth("...");
        int trimIndex = rawText.length();

        while (trimIndex > 0 && textRenderer.getWidth(rawText.substring(0, trimIndex)) > maxWidth) {
            trimIndex--;
        }
        
        return rawText.substring(0, trimIndex) + "...";
    }
}
