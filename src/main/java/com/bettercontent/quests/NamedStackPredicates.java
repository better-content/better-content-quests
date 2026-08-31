package com.bettercontent.quests;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
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
    private static final Set<String> METALS = Set.of(
            "iron", "copper", "gold", "netherite", "cobalt", "manyullyn", "queens_slime", "hepatizon",
            "rose_gold", "pig_iron", "amethyst_bronze", "slimesteel", "brass", "bronze", "steel");

    private NamedStackPredicates() {}

    public static boolean test(String name, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ResourceLocation enchantment = ENCHANTMENTS.get(name);
        if (enchantment != null) return EnchantmentHelper.getEnchantments(stack).keySet().stream()
                .anyMatch(value -> enchantment.equals(BuiltInRegistries.ENCHANTMENT.getKey(value)));
        return switch (name) {
            case "water_purity_3" -> integerValue(stack.getTag(), "purity", "water_purity") == 3;
            case "food_temperature_changed" -> stack.isEdible() && stack.getTag() != null
                    && stack.getTag().contains("heat_sync_food", Tag.TAG_COMPOUND)
                    && stack.getTag().getCompound("heat_sync_food").getBoolean("thermally_changed");
            case "tempered_waterskin" -> id(stack).contains("waterskin")
                    && hasNonZeroNumber(stack.getTag(), "temperature", "temperature_value", "waterskin_temperature");
            case "armor_with_inserted_insulation" -> hasKeyLike(stack.getTag(), "insulat");
            case "any_tcon_sand_cast" -> inTag(stack, "tconstruct:casts/sand") || id(stack).contains("sand_cast");
            case "any_tcon_permanent_cast" -> inTag(stack, "tconstruct:casts/gold")
                    || id(stack).contains("gold_cast") || id(stack).contains("red_sand_cast");
            case "tcon_functional_metal_part" -> isFunctionalPart(stack) && containsMetalMaterial(stack.getTag());
            case "tcon_tool_with_metal_functional_part" -> isTconstructTool(stack) && functionalMaterialIsMetal(stack.getTag());
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

    private static String id(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().toLowerCase(Locale.ROOT);
    }

    private static boolean inTag(ItemStack stack, String id) {
        return stack.is(TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation(id)));
    }

    private static boolean isTconstructTool(ItemStack stack) {
        String id = id(stack);
        return id.startsWith("tconstruct:") && !id.contains("part") && !id.contains("cast") && stack.getTag() != null
                && (stack.getTag().contains("tic_materials") || stack.getTag().contains("tic_modifiers"));
    }

    private static boolean isFunctionalPart(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.endsWith("_head") || path.endsWith("_blade") || path.endsWith("_plate")
                || path.endsWith("_bowlimb") || path.endsWith("_limb") || path.endsWith("_maille")
                || path.endsWith("_shield_core") || path.endsWith("_tool_part");
    }

    private static boolean functionalMaterialIsMetal(CompoundTag tag) {
        if (tag == null) return false;
        Tag materials = findTagLike(tag, "tic_materials");
        if (materials instanceof ListTag list && !list.isEmpty()) {
            // TConstruct serializes functional/head materials first; handles and bindings follow.
            return stringIsMetal(list.get(0).getAsString());
        }
        return false;
    }

    private static boolean containsMetalMaterial(Tag tag) {
        if (tag == null) return false;
        if (!(tag instanceof CompoundTag) && stringIsMetal(tag.getAsString())) return true;
        if (tag instanceof CompoundTag compound) {
            for (String key : compound.getAllKeys()) if (containsMetalMaterial(compound.get(key))) return true;
        } else if (tag instanceof ListTag list) {
            for (Tag child : list) if (containsMetalMaterial(child)) return true;
        }
        return false;
    }

    private static boolean stringIsMetal(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        return METALS.stream().anyMatch(metal -> lower.equals(metal) || lower.endsWith(":" + metal));
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

    private static boolean hasNonZeroNumber(CompoundTag tag, String... fragments) {
        int value = integerValue(tag, fragments);
        return value != Integer.MIN_VALUE && value != 0;
    }

    private static boolean hasKeyLike(Tag tag, String fragment) {
        if (tag instanceof CompoundTag compound) {
            for (String key : compound.getAllKeys()) {
                if (key.toLowerCase(Locale.ROOT).contains(fragment)) return true;
                if (hasKeyLike(compound.get(key), fragment)) return true;
            }
        } else if (tag instanceof ListTag list) {
            for (Tag child : list) if (hasKeyLike(child, fragment)) return true;
        }
        return false;
    }

    private static Tag findTagLike(Tag tag, String fragment) {
        if (tag instanceof CompoundTag compound) {
            for (String key : compound.getAllKeys()) {
                if (key.toLowerCase(Locale.ROOT).contains(fragment)) return compound.get(key);
                Tag found = findTagLike(compound.get(key), fragment);
                if (found != null) return found;
            }
        } else if (tag instanceof ListTag list) {
            for (Tag child : list) {
                Tag found = findTagLike(child, fragment);
                if (found != null) return found;
            }
        }
        return null;
    }

}
