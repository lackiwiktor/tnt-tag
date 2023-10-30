package me.ponktacology.tag;

public class Messages {

    public static String get(String path) {
        return Color.translate(Plugin.get().getConfig().getString("messages." + path));
    }
}
