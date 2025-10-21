package com.modvane.hologenica.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;

// Manual screen showing recipes - extends Screen directly like vanilla book
public class ManualScreen extends Screen {
    
    private int currentPage = 0;
    private static final int TOTAL_PAGES = 8;
    
    private Button prevButton;
    private Button nextButton;
    
    private int leftPos;
    private int topPos;
    private int imageWidth = 280;
    private int imageHeight = 180;
    
    public ManualScreen() {
        super(Component.literal("Hologenica Manual"));
    }
    
    @Override
    protected void init() {
        super.init();
        
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        
        int buttonY = topPos + imageHeight - 30;
        
        // Previous page button
        this.prevButton = Button.builder(Component.literal("<"), button -> {
            if (currentPage > 0) currentPage--;
        }).bounds(leftPos + 20, buttonY, 40, 20).build();
        
        // Next page button
        this.nextButton = Button.builder(Component.literal(">"), button -> {
            if (currentPage < TOTAL_PAGES - 1) currentPage++;
        }).bounds(leftPos + imageWidth - 60, buttonY, 40, 20).build();
        
        // Close button
        Button closeButton = Button.builder(Component.literal("Close"), button -> {
            this.onClose();
        }).bounds(leftPos + imageWidth / 2 - 30, buttonY, 60, 20).build();
        
        this.addRenderableWidget(prevButton);
        this.addRenderableWidget(nextButton);
        this.addRenderableWidget(closeButton);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // NO renderBackground call - prevents blur!
        
        // Draw dark background panel (same transparency as Hologram GUI)
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xC0101010);
        
        // Draw title
        graphics.drawCenteredString(this.font, "Hologenica Manual", leftPos + imageWidth / 2, topPos + 10, 0xFFD700);
        graphics.drawCenteredString(this.font, "Page " + (currentPage + 1) + " / " + TOTAL_PAGES, 
            leftPos + imageWidth / 2, topPos + 22, 0xAAAAAA);
        
        // Draw current page content
        renderPage(graphics);
        
        // Render buttons on top
        super.render(graphics, mouseX, mouseY, partialTick);
    }
    
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Override to do NOTHING - prevents automatic blur rendering
    }
    
    private void renderPage(GuiGraphics graphics) {
        int centerX = leftPos + imageWidth / 2;
        int startY = topPos + 40;
        
        switch (currentPage) {
            case 0 -> renderManualRecipe(graphics, centerX, startY);
            case 1 -> renderBioscannerRecipe(graphics, centerX, startY);
            case 2 -> renderImprinterRecipe(graphics, centerX, startY);
            case 3 -> renderNeurocellRecipe(graphics, centerX, startY);
            case 4 -> renderReformerRecipe(graphics, centerX, startY);
            case 5 -> renderNeurolinkRecipe(graphics, centerX, startY);
            case 6 -> renderHologramRecipe(graphics, centerX, startY);
            case 7 -> renderTelepadRecipe(graphics, centerX, startY);
        }
    }
    
    private void renderRecipeTitle(GuiGraphics graphics, String title, int centerX, int y) {
        graphics.drawCenteredString(this.font, title, centerX, y, 0xFFD700);
    }
    
    private void renderCraftingGrid(GuiGraphics graphics, ItemStack[][] recipe, int centerX, int startY) {
        int gridStartX = centerX - 80;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int x = gridStartX + (col * 18);
                int y = startY + (row * 18);
                graphics.fill(x, y, x + 16, y + 16, 0xAA555555);
                if (recipe[row][col] != null && !recipe[row][col].isEmpty()) {
                    graphics.renderItem(recipe[row][col], x, y);
                }
            }
        }
        graphics.drawString(this.font, "=>", centerX - 10, startY + 22, 0xFFFFFF);
        graphics.fill(centerX + 20, startY + 18, centerX + 36, startY + 34, 0xAA555555);
    }
    
    private void renderBioscannerRecipe(GuiGraphics graphics, int centerX, int y) {
        renderRecipeTitle(graphics, "Bioscanner Recipe", centerX, y);
        ItemStack[][] recipe = new ItemStack[3][3];
        recipe[0][0] = new ItemStack(Items.GLASS_PANE);
        recipe[0][1] = new ItemStack(Items.REDSTONE);
        recipe[0][2] = new ItemStack(Items.GLASS_PANE);
        recipe[1][0] = new ItemStack(Items.IRON_INGOT);
        recipe[1][1] = new ItemStack(Items.REDSTONE_BLOCK);
        recipe[1][2] = new ItemStack(Items.IRON_INGOT);
        recipe[2][0] = new ItemStack(Items.IRON_INGOT);
        recipe[2][1] = new ItemStack(Items.REDSTONE);
        recipe[2][2] = new ItemStack(Items.IRON_INGOT);
        renderCraftingGrid(graphics, recipe, centerX, y + 15);
        ItemStack result = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("hologenica", "bioscanner")));
        graphics.renderItem(result, centerX + 20, y + 33);
    }
    
    private void renderNeurolinkRecipe(GuiGraphics graphics, int centerX, int y) {
        renderRecipeTitle(graphics, "Neurolink Recipe (yields 8)", centerX, y);
        ItemStack[][] recipe = new ItemStack[3][3];
        recipe[0][1] = new ItemStack(Items.IRON_NUGGET);
        recipe[1][0] = new ItemStack(Items.COPPER_INGOT);
        recipe[1][1] = new ItemStack(Items.REDSTONE);
        recipe[1][2] = new ItemStack(Items.COPPER_INGOT);
        recipe[2][1] = new ItemStack(Items.IRON_NUGGET);
        renderCraftingGrid(graphics, recipe, centerX, y + 15);
        ItemStack result = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("hologenica", "neurolink")), 8);
        graphics.renderItem(result, centerX + 20, y + 33);
        graphics.renderItemDecorations(this.font, result, centerX + 20, y + 33);
    }
    
    private void renderImprinterRecipe(GuiGraphics graphics, int centerX, int y) {
        renderRecipeTitle(graphics, "Imprinter Recipe", centerX, y);
        ItemStack[][] recipe = new ItemStack[3][3];
        recipe[0][0] = new ItemStack(Items.IRON_INGOT);
        recipe[0][1] = new ItemStack(Items.PISTON);
        recipe[0][2] = new ItemStack(Items.IRON_INGOT);
        recipe[1][0] = new ItemStack(Items.REDSTONE_BLOCK);
        recipe[1][1] = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("hologenica", "bioscanner")));
        recipe[1][2] = new ItemStack(Items.REDSTONE_BLOCK);
        recipe[2][0] = new ItemStack(Items.IRON_INGOT);
        recipe[2][1] = new ItemStack(Items.HOPPER);
        recipe[2][2] = new ItemStack(Items.IRON_INGOT);
        renderCraftingGrid(graphics, recipe, centerX, y + 15);
        ItemStack result = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("hologenica", "imprinter")));
        graphics.renderItem(result, centerX + 20, y + 33);
    }
    
    private void renderNeurocellRecipe(GuiGraphics graphics, int centerX, int y) {
        renderRecipeTitle(graphics, "Neurocell Recipe", centerX, y);
        ItemStack[][] recipe = new ItemStack[3][3];
        recipe[0][0] = new ItemStack(Items.IRON_BLOCK);
        recipe[0][1] = new ItemStack(Items.GLASS_PANE);
        recipe[0][2] = new ItemStack(Items.IRON_BLOCK);
        recipe[1][0] = new ItemStack(Items.GLOWSTONE);
        recipe[1][1] = new ItemStack(Items.DIAMOND);
        recipe[1][2] = new ItemStack(Items.GLOWSTONE);
        recipe[2][0] = new ItemStack(Items.IRON_BLOCK);
        recipe[2][1] = new ItemStack(Items.NETHER_STAR);
        recipe[2][2] = new ItemStack(Items.IRON_BLOCK);
        renderCraftingGrid(graphics, recipe, centerX, y + 15);
        ItemStack result = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("hologenica", "neurocell")));
        graphics.renderItem(result, centerX + 20, y + 33);
    }
    
    private void renderReformerRecipe(GuiGraphics graphics, int centerX, int y) {
        renderRecipeTitle(graphics, "Reformer Recipe", centerX, y);
        ItemStack[][] recipe = new ItemStack[3][3];
        recipe[0][0] = new ItemStack(Items.OBSIDIAN);
        recipe[0][1] = new ItemStack(Items.ENDER_PEARL);
        recipe[0][2] = new ItemStack(Items.OBSIDIAN);
        recipe[1][0] = new ItemStack(Items.DIAMOND);
        recipe[1][1] = new ItemStack(Items.ENDER_EYE);
        recipe[1][2] = new ItemStack(Items.DIAMOND);
        recipe[2][0] = new ItemStack(Items.OBSIDIAN);
        recipe[2][1] = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("hologenica", "neurolink")));
        recipe[2][2] = new ItemStack(Items.OBSIDIAN);
        renderCraftingGrid(graphics, recipe, centerX, y + 15);
        ItemStack result = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("hologenica", "reformer")));
        graphics.renderItem(result, centerX + 20, y + 33);
    }
    
    private void renderHologramRecipe(GuiGraphics graphics, int centerX, int y) {
        renderRecipeTitle(graphics, "Hologram Projector Recipe", centerX, y);
        ItemStack[][] recipe = new ItemStack[3][3];
        recipe[0][0] = new ItemStack(Items.GLASS_PANE);
        recipe[0][1] = new ItemStack(Items.GLOWSTONE);
        recipe[0][2] = new ItemStack(Items.GLASS_PANE);
        recipe[1][0] = new ItemStack(Items.REDSTONE);
        recipe[1][1] = new ItemStack(Items.DIAMOND);
        recipe[1][2] = new ItemStack(Items.REDSTONE);
        recipe[2][0] = new ItemStack(Items.IRON_BLOCK);
        recipe[2][1] = new ItemStack(Items.REDSTONE_BLOCK);
        recipe[2][2] = new ItemStack(Items.IRON_BLOCK);
        renderCraftingGrid(graphics, recipe, centerX, y + 15);
        ItemStack result = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("hologenica", "hologram")));
        graphics.renderItem(result, centerX + 20, y + 33);
    }
    
    private void renderTelepadRecipe(GuiGraphics graphics, int centerX, int y) {
        renderRecipeTitle(graphics, "Telepad Recipe", centerX, y);
        ItemStack[][] recipe = new ItemStack[3][3];
        recipe[0][0] = new ItemStack(Items.ENDER_PEARL);
        recipe[0][1] = new ItemStack(Items.DIAMOND);
        recipe[0][2] = new ItemStack(Items.ENDER_PEARL);
        recipe[1][0] = new ItemStack(Items.OBSIDIAN);
        recipe[1][1] = new ItemStack(Items.ENDER_EYE);
        recipe[1][2] = new ItemStack(Items.OBSIDIAN);
        recipe[2][0] = new ItemStack(Items.IRON_BLOCK);
        recipe[2][1] = new ItemStack(Items.REDSTONE_BLOCK);
        recipe[2][2] = new ItemStack(Items.IRON_BLOCK);
        renderCraftingGrid(graphics, recipe, centerX, y + 15);
        ItemStack result = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("hologenica", "telepad")));
        graphics.renderItem(result, centerX + 20, y + 33);
    }
    
    private void renderManualRecipe(GuiGraphics graphics, int centerX, int y) {
        renderRecipeTitle(graphics, "Manual Recipe", centerX, y);
        ItemStack[][] recipe = new ItemStack[3][3];
        recipe[1][0] = new ItemStack(Items.PAPER);
        recipe[1][1] = new ItemStack(Items.BOOK);
        recipe[1][2] = new ItemStack(Items.PAPER);
        renderCraftingGrid(graphics, recipe, centerX, y + 15);
        ItemStack result = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("hologenica", "manual")));
        graphics.renderItem(result, centerX + 20, y + 33);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
