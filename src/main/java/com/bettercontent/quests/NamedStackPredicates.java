package com.bettercontent.quests;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** Named, version-tolerant stack checks used by authored quest data. */
public final class NamedStackPredicates {
    private static final Set<String> BASE_SUPPORTED = Set.of("water_purity_3");
    private static final Map<String, ResourceLocation> ENCHANTMENTS = Map.ofEntries(
            enchantment("ars_nouveau", "mana_boost"), enchantment("ars_nouveau", "mana_regen"),
            enchantment("ars_nouveau", "reactive"), enchantment("minecraft", "aqua_affinity"),
            enchantment("minecraft", "bane_of_arthropods"), enchantment("minecraft", "blast_protection"),
            enchantment("minecraft", "depth_strider"), enchantment("minecraft", "efficiency"),
            enchantment("minecraft", "feather_falling"), enchantment("minecraft", "fire_aspect"),
            enchantment("minecraft", "fire_protection"), enchantment("minecraft", "flame"),
            enchantment("minecraft", "fortune"), enchantment("minecraft", "infinity"),
            enchantment("minecraft", "knockback"), enchantment("minecraft", "looting"),
            enchantment("minecraft", "multishot"), enchantment("minecraft", "piercing"),
            enchantment("minecraft", "power"), enchantment("minecraft", "projectile_protection"),
            enchantment("minecraft", "protection"), enchantment("minecraft", "punch"),
            enchantment("minecraft", "quick_charge"), enchantment("minecraft", "respiration"),
            enchantment("minecraft", "sharpness"), enchantment("minecraft", "silk_touch"),
            enchantment("minecraft", "smite"), enchantment("minecraft", "sweeping"),
            enchantment("minecraft", "thorns"), enchantment("minecraft", "unbreaking"));
    public static final Set<String> SUPPORTED = supportedNames();
    private NamedStackPredicates() {}

    public static boolean test(String name, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ResourceLocation enchantment = ENCHANTMENTS.get(name);
        if (enchantment != null) return EnchantmentHelper.getEnchantments(stack).keySet().stream()
                .anyMatch(value -> enchantment.equals(BuiltInRegistries.ENCHANTMENT.getKey(value)));
        return switch (name) {
            case "water_purity_3" -> integerValue(stack.getTag(), "purity", "water_purity") == 3;
            default -> false;
        };
    }

    private static Map.Entry<String, ResourceLocation> enchantment(String namespace, String path) {
        return Map.entry("enchantment_" + namespace + "_" + path, new ResourceLocation(namespace, path));
    }

    private static Set<String> supportedNames() {
        Set<String> names = new HashSet<>(BASE_SUPPORTED);
        names.addAll(ENCHANTMENTS.keySet());
        return Set.copyOf(names);
    }

    private static int integerValue(CompoundTag tag, String... fragments) {
        if (tag == null) return Integer.MIN_VALUE;
        for (String key : tag.getAllKeys()) {
            String lower = key.toLowerCase(Locale.ROOT);
            for (String fragment : fragments) {
                if (lower.contains(fragment) && tag.contains(key, Tag.TAG_ANY_NUMERIC)) return tag.getInt(key);
            }
            Tag child = tag.get(key);
            if (child instanceof CompoundTag nested) {
                int found = integerValue(nested, fragments);
                if (found != Integer.MIN_VALUE) return found;
            }
        }
        return Integer.MIN_VALUE;
    }

}
