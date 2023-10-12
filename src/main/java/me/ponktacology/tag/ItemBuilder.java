package me.ponktacology.tag;


import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This is a chainable builder for {@link ItemStack}s in {@link Bukkit}
 * <br>
 * Example Usage:<br>
 * {@code ItemStack is = new ItemBuilder(Material.LEATHER_HELMET).amount(2).data(4).durability(4).enchantment(Enchantment.ARROW_INFINITE).enchantment(Enchantment.LUCK, 2).name(ChatColor.RED + "the name").lore(ChatColor.GREEN + "line 1").lore(ChatColor.BLUE + "line 2").color(Color.MAROON);
 *
 * @author MiniDigger, computerwizjared
 * @version 1.2
 */
public class ItemBuilder extends ItemStack {
    /**
     * Initializes the builder with the given {@link Material}
     *
     * @param mat the {@link Material} to start the builder from
     * @since 1.0
     */
    public ItemBuilder(Material mat) {
        super(mat);
    }

    /**
     * Initializes the builder with the given {@link Material} and damage
     *
     * @param mat the {@link Material} to start the builder from
     * @since 1.0
     */
    public ItemBuilder(Material mat, byte damage) {
        super(mat, 1, damage);
    }

    /**
     * Inits the builder with the given {@link ItemStack}
     *
     * @param is the {@link ItemStack} to start the builder from
     * @since 1.0
     */
    public ItemBuilder(ItemStack is) {
        super(is);
    }

    /**
     * Changes the amount of the {@link ItemStack}
     *
     * @param amount the new amount to set
     * @return this builder for chaining
     * @since 1.0
     */
    public ItemBuilder amount(int amount) {
        setAmount(amount);
        return this;
    }

    /**
     * Changes the display name of the {@link ItemStack}
     *
     * @param name the new display name to set
     * @return this builder for chaining
     * @since 1.0
     */
    public ItemBuilder name(String name) {
        ItemMeta meta = getItemMeta();
        meta.setDisplayName(StringUtil.color(name));
        setItemMeta(meta);
        return this;
    }

    /**
     * Appends to the display name of the {@link ItemStack}
     *
     * @param append the text to append to the display name
     * @return this builder for chaining
     * @since Joshi
     */
    public ItemBuilder appendName(String append) {
        ItemMeta meta = getItemMeta();
        meta.setDisplayName(StringUtil.color((meta.hasDisplayName() ? meta.getDisplayName() : "") + append));
        setItemMeta(meta);
        return this;
    }

    /**
     * Adds a new line to the lore of the {@link ItemStack}
     *
     * @param text the new line to add
     * @return this builder for chaining
     * @since 1.0
     */
    public ItemBuilder lore(String text) {
        ItemMeta meta = getItemMeta();
        List<String> lore = meta.getLore();
        if(lore == null)
            lore = new ArrayList<>();

        lore.add(StringUtil.color(text));
        meta.setLore(lore);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(String... text) {
        return lore(Arrays.asList(text));
    }

    public ItemBuilder lore(List<String> text) {
        ItemMeta meta = getItemMeta();
        List<String> lore = meta.getLore();
        if(lore == null)
            lore = new ArrayList<>();

        lore.addAll(text.stream().map(StringUtil::color).collect(Collectors.toList()));
        meta.setLore(lore);
        setItemMeta(meta);
        return this;
    }

    /**
     * Changes the durability of the {@link ItemStack}
     *
     * @param durability the new durability to set
     * @return this builder for chaining
     * @since 1.0
     */
    public ItemBuilder durability(int durability) {
        setDurability((short) durability);
        return this;
    }

    /**
     * Changes the data of the {@link ItemStack}
     *
     * @param data the new data to set
     * @return this builder for chaining
     * @since 1.0
     */
    @SuppressWarnings("deprecation")
    public ItemBuilder data(int data) {
        setData(new MaterialData(getType(), (byte) data));
        return this;
    }

    /**
     * Adds an {@link Enchantment} with the given level to the {@link ItemStack}
     *
     * @param enchantment the enchantment to add
     * @param level       the level of the enchantment
     * @return this builder for chaining
     * @since 1.0
     */
    public ItemBuilder enchantment(Enchantment enchantment, int level) {
        addUnsafeEnchantment(enchantment, level);
        return this;
    }

    /**
     * Adds an {@link Enchantment} with the level 1 to the {@link ItemStack}
     *
     * @param enchantment the enchantment to add
     * @return this builder for chaining
     * @since 1.0
     */
    public ItemBuilder enchantment(Enchantment enchantment) {
        addUnsafeEnchantment(enchantment, 1);
        return this;
    }

    public ItemBuilder enchantments(Map<Enchantment, Integer> enchantments) {
        addUnsafeEnchantments(enchantments);
        return this;
    }

    /**
     * Changes the {@link Material} of the {@link ItemStack}
     *
     * @param material the new material to set
     * @return this builder for chaining
     * @since 1.0
     */
    public ItemBuilder type(Material material) {
        setType(material);
        return this;
    }

    /**
     * Clears the lore of the {@link ItemStack}
     *
     * @return this builder for chaining
     * @since 1.0
     */
    public ItemBuilder clearLore() {
        ItemMeta meta = getItemMeta();
        meta.setLore(new ArrayList<>());
        setItemMeta(meta);
        return this;
    }

    /**
     * Clears the list of {@link Enchantment}s of the {@link ItemStack}
     *
     * @return this builder for chaining
     * @since 1.0
     */
    public ItemBuilder clearEnchantments() {
        getEnchantments().keySet().forEach(this::removeEnchantment);
        return this;
    }

    public ItemBuilder owner(String owner) {
        if(!(getItemMeta() instanceof SkullMeta))
            return this;

        SkullMeta meta = (SkullMeta) getItemMeta();
        meta.setOwner(owner);
        setItemMeta(meta);

        return this;
    }


    /**
     * Sets the {@link Color} of a part of leather armor
     *
     * @param color the {@link Color} to use
     * @return this builder for chaining
     * @since 1.1
     */
    public ItemBuilder color(Color color) {
        if (getType() == Material.LEATHER_BOOTS || getType() == Material.LEATHER_CHESTPLATE || getType() == Material.LEATHER_HELMET
                || getType() == Material.LEATHER_LEGGINGS) {
            LeatherArmorMeta meta = (LeatherArmorMeta) getItemMeta();
            meta.setColor(color);
            setItemMeta(meta);
            return this;
        } else {
            throw new IllegalArgumentException("color() only applicable for leather armor!");
        }
    }

    public ItemBuilder glow() {
        this.addUnsafeEnchantment(Enchantment.DURABILITY, 0);
        return flag(ItemFlag.HIDE_ENCHANTS);
    }

    /**
     * Adds an {@link ItemFlag} to the {@link ItemStack}
     *
     * @param flag the flag to add
     * @return this builder for chaining
     */
    public ItemBuilder flag(ItemFlag flag) {
        ItemMeta meta = getItemMeta();
        meta.addItemFlags(flag);
        setItemMeta(meta);
        return this;
    }

    /**
     * Clears the list of {@link ItemFlag}s of the {@link ItemStack}
     *
     * @return this builder for chaining
     */
    public ItemBuilder clearFlags() {
        ItemMeta meta = getItemMeta();
        meta.getItemFlags().forEach(meta::removeItemFlags);
        setItemMeta(meta);
        return this;
    }
}
