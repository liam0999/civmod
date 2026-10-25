package io.github.liam0999.civmod.snitch;

import io.github.liam0999.civmod.util.ChatFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JalistAutoPaginator {
    private static final String MAP_URL = "https://map.edenmc.dev/";

    private static final Minecraft mc = Minecraft.getInstance();
    private static JalistAutoPaginator instance;
    
    private boolean isActive = false;
    private boolean waitingForNextPage = false;
    private int pagesProcessed = 0;
    private int totalSnitchesFound = 0;
    private int snitchesWillCullSoon = 0;
    private int snitchesWillDormantSoon = 0;
    private Set<String> groupsFound = new HashSet<>();
    private long startTime = 0;
    private long waitingSince = 0;

    private static final String JALIST_TITLE = "JukeAlert snitches";
    private static final int NEXT_PAGE_SLOT = 53;
    private static final long PAGE_TIMEOUT_MS = 5000;
    
    public static JalistAutoPaginator getInstance() {
        if (instance == null) {
            instance = new JalistAutoPaginator();
        }
        return instance;
    }
    
    public void startAutoPagination() {
        if (isActive) {
            logToChat("JAList auto-pagination already running!");
            return;
        }
        
        if (!isJalistOpen()) {
            logToChat("JAList must be open to start auto-pagination! Press J key while in JAList.");
            return;
        }
        
        isActive = true;
        waitingForNextPage = false;
        pagesProcessed = 0;
        totalSnitchesFound = 0;
        snitchesWillCullSoon = 0;
        snitchesWillDormantSoon = 0;
        groupsFound.clear();
        startTime = System.currentTimeMillis();
        
        logToChat("Starting JAList auto-pagination... This will read all pages automatically.");
        
        mc.execute(() -> clickNextPage(((AbstractContainerScreen<?>) mc.gui.screen()).getMenu().getItems()));
    }
    
    // closing the JAList mid-scan means the next page never arrives, which would leave the scan active forever
    public void tick() {
        if (isActive && waitingForNextPage && System.currentTimeMillis() - waitingSince > PAGE_TIMEOUT_MS) {
            stop("JAList scan stopped, the next page never loaded.");
        }
    }

    public void stopAutoPagination() {
        stop("JAList scan complete!");
    }

    private void stop(String headline) {
        if (!isActive) return;

        isActive = false;
        waitingForNextPage = false;

        long duration = (System.currentTimeMillis() - startTime) / 1000;
        logToChat(String.format(
            "%s Found %d snitches across %d groups in %d pages (%ds)",
            headline, totalSnitchesFound, groupsFound.size(), pagesProcessed, duration
        ));
        
        if (snitchesWillCullSoon > 0 || snitchesWillDormantSoon > 0) {
            String detail;

            if (snitchesWillCullSoon > 0 && snitchesWillDormantSoon > 0) {
                detail = String.format("%d snitches that will be culled and %d that will go dormant in the next 48h!", snitchesWillCullSoon, snitchesWillDormantSoon);
            } else if (snitchesWillCullSoon > 0) {
                detail = String.format("%d snitches that will be culled in the next 48h!", snitchesWillCullSoon);
            } else {
                detail = String.format("%d snitches that will go dormant in the next 48h!", snitchesWillDormantSoon);
            }

            logToChat(Component.literal("⚠ Warning: You have " + detail + " Use ")
                .append(ChatFormat.link(MAP_URL))
                .append(Component.literal(" to see where they are!")));
        }
    }
    
    public void onJalistPageLoaded(List<ItemStack> stacks, int snitchCount) {
        if (!isActive) return;
        
        pagesProcessed++;
        totalSnitchesFound += snitchCount;
        waitingForNextPage = false;
        
        if (pagesProcessed % 10 == 0) {
            logToChat(String.format("Progress: %d pages (%d snitches so far)", pagesProcessed, totalSnitchesFound));
        }
        
        mc.execute(() -> clickNextPage(stacks));
    }
    
    public void addSnitchEntry(String group, long dormantTs, long cullTs) {
        if (group != null && !group.trim().isEmpty()) {
            groupsFound.add(group);
        }
        
        long currentTime = System.currentTimeMillis();
        long fortyEightHoursFromNow = currentTime + (48L * 60L * 60L * 1000L);
        
        if (cullTs != 0 && cullTs <= fortyEightHoursFromNow) {
            snitchesWillCullSoon++;
        }
        
        if (dormantTs != 0 && dormantTs <= fortyEightHoursFromNow) {
            snitchesWillDormantSoon++;
        }
    }
    
    private void clickNextPage(List<ItemStack> stacks) {
        if (!isActive || waitingForNextPage) {
            return;
        }
        
        var screen = mc.gui.screen();
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) {
            stopAutoPagination();
            return;
        }
        
        if (!isJalistOpen()) {
            stopAutoPagination();
            return;
        }
        
        var container = containerScreen.getMenu();
        if (stacks.size() <= NEXT_PAGE_SLOT) {
            stopAutoPagination();
            return;
        }

        ItemStack nextPageItem = stacks.get(NEXT_PAGE_SLOT);
        
        if (nextPageItem.isEmpty() || !isArrowItem(nextPageItem)) {
            stopAutoPagination();
            return;
        }
        
        waitingForNextPage = true;
        waitingSince = System.currentTimeMillis();

        if (mc.gameMode != null) {
            mc.gameMode.handleContainerInput(
                container.containerId, 
                NEXT_PAGE_SLOT, 
                0, 
                ContainerInput.PICKUP,
                mc.player
            );
        }
    }
    
    private boolean isJalistOpen() {
        var screen = mc.gui.screen();
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) {
            return false;
        }
        
        var title = containerScreen.getTitle().getString();
        return title.contains("JukeAlert") || title.contains("snitches") || title.toLowerCase().contains("your snitches");
    }
    
    private boolean isArrowItem(ItemStack item) {
        return item.is(Items.ARROW) || 
               item.is(Items.SPECTRAL_ARROW) ||
               item.getDisplayName().getString().toLowerCase().contains("next");
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public boolean isWaitingForNextPage() {
        return waitingForNextPage;
    }
    
    private void logToChat(Component message) {
        mc.gui.hud.getChat().addClientSystemMessage(ChatFormat.prefixed(message));
    }

    private void logToChat(String message) {
        logToChat(Component.literal(message));
    }
}