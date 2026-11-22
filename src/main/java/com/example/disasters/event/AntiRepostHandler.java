package com.example.disasters.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import org.quiltmc.loader.api.QuiltLoader;

import java.nio.file.Path;

public class AntiRepostHandler {

    private static final String OFFICIAL_JAR_NAME = "naturaldisasters-quilt-1.21.1-1.0.0.jar";
    private static final String CURSEFORGE_URL = "https://" + "www." + "://curseforge.com";

    public static void checkAndWarn(ServerPlayer player) {
        if (QuiltLoader.isDevelopmentEnvironment()) return;

        try {
            Path jarPath = Path.of(AntiRepostHandler.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            String actualFileName = jarPath.getFileName().toString();

            if (!actualFileName.equals(OFFICIAL_JAR_NAME)) {
                Component msg = Component.literal("[CẢNH BÁO / WARNING] ")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                        .append(Component.literal("Bạn đang dùng bản mod re-up trái phép. Tải bản gốc tại CurseForge: ")
                                .withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal("[Tải bản chính chủ]")
                                .withStyle(Style.EMPTY
                                        .withColor(ChatFormatting.AQUA)
                                        .withUnderlined(true)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, CURSEFORGE_URL))));

                player.sendSystemMessage(msg);
            }
        } catch (Exception ignored) {
        }
    }
}
