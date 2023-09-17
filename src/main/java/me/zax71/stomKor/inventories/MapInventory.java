package me.zax71.stomKor.inventories;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.zax71.stomKor.ParkourMap;
import me.zax71.stomKor.ParkourPlayer;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.inventory.condition.InventoryConditionResult;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemHideFlag;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static me.zax71.stomKor.Main.config;
import static me.zax71.stomKor.Main.configUtils;
import static me.zax71.stomKor.Main.logger;
import static me.zax71.stomKor.Main.parkourMaps;

public class MapInventory {
    private final Inventory inventory;
    private final int[] mapSlots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

    public MapInventory() {
        Inventory inventory = new Inventory(InventoryType.CHEST_5_ROW, "Select a map");


        // Create an ItemStack the size of the Inventory and fill it with black stained glass panes
        ItemStack[] itemStacks = new ItemStack[inventory.getSize()];
        Arrays.fill(itemStacks, ItemStack.builder(Material.BLACK_STAINED_GLASS_PANE).build());

        // Add the top buttons
        itemStacks[3] = ItemStack.of(Material.GREEN_CONCRETE)
                .withDisplayName(
                        Component.text("Easy Maps")
                                .color(NamedTextColor.GREEN)
                );

        itemStacks[4] = ItemStack.of(Material.ORANGE_CONCRETE)
                .withDisplayName(
                        Component.text("Medium Maps")
                                .color(NamedTextColor.GREEN)
                );

        itemStacks[5] = ItemStack.of(Material.RED_CONCRETE)
                .withDisplayName(
                        Component.text("Hard Maps")
                                .color(NamedTextColor.GREEN)
                );

        itemStacks[44] = ItemStack.of(Material.RED_BED)
                .withDisplayName(
                        Component.text("Back to hub")
                                .color(NamedTextColor.RED)
                );


        // put the contents of the itemStack in to the inventory
        inventory.copyContents(itemStacks);

        // Add the inventory condition for all the events of this inventory
        inventory.addInventoryCondition(this::inventoryCondition);

        this.inventory = inventory;

        // Set the state to easy by default
        setState("easy", null);
    }

    private void inventoryCondition(Player player, int slot, ClickType clickType, InventoryConditionResult inventoryConditionResult) {

        // Stop items from being moved around
        inventoryConditionResult.setCancel(true);

        if (inventoryConditionResult.getClickedItem() == ItemStack.AIR) return;

        // Deal with sending a player to a map
        if (Arrays.stream(mapSlots).anyMatch(i -> i == slot)) {
            String map = inventory.getItemStack(slot).getTag(Tag.String("map"));
            sendToMap((ParkourPlayer) player, map);
            player.sendMessage("Sending you to " + map);
        }

        switch (slot) {
            case 3 -> setState("easy", player);
            case 4 -> setState("medium", player);
            case 5 -> setState("hard", player);
            case 44 -> ((ParkourPlayer) player).gotoHub();
        }
    }

    private ItemStack getMapItem(String difficulty, int i) {

        List<ItemStack> maps = getParkourMapsFromRedis()
                .stream()
                .filter(obj -> Objects.equals(obj.get("difficulty"), difficulty))
                .map(obj -> ItemStack
                        .of(Objects.requireNonNull(Material.fromNamespaceId(obj.get("materialType"))))
                        .withDisplayName(MiniMessage.miniMessage().deserialize(obj.get("materialDisplayName")))
                        .withTag(Tag.String("map"), obj.get("name"))
                        .withTag(Tag.Integer("order"), Integer.valueOf(obj.get("order")))
                )
                .sorted(Comparator.comparing((ItemStack item) -> item.getTag(Tag.Integer("order"))))
                .toList();


        // Return the ItemStack if i is in bounds, else return AIR
        if (i < maps.size()) {
            return maps.get(i);
        } else {
            return ItemStack.AIR;
        }
    }

    private void setGlowing(int slot, boolean state) {
        if (state) {
            inventory.setItemStack(slot,
                    inventory
                            .getItemStack(slot)
                            .withMeta(builder -> builder
                                    .enchantment(Enchantment.KNOCKBACK, (short) 1)
                                    .hideFlag(ItemHideFlag.HIDE_ENCHANTS)
                            )
            );
        } else {
            inventory.setItemStack(slot,
                    inventory
                            .getItemStack(slot)
                            .withMeta(ItemMeta.Builder::clearEnchantment)
            );
        }

    }

    private void setState(String state, @Nullable Player player) {


        switch (state) {
            case "easy" -> {
                // Set the buttons to glow appropriately
                setGlowing(3, true);
                setGlowing(4, false);
                setGlowing(5, false);

                // Add the maps to the slots they have a space in
                int mapI = 0;
                for (int slot : mapSlots) {
                    inventory.setItemStack(slot, getMapItem("easy", mapI));
                    mapI++;
                }
            }
            case "medium" -> {
                // Set the buttons to glow appropriately
                setGlowing(3, false);
                setGlowing(4, true);
                setGlowing(5, false);

                // Add the maps to the slots they have a space in
                int mapI = 0;
                for (int slot : mapSlots) {
                    inventory.setItemStack(slot, getMapItem("medium", mapI));
                    mapI++;
                }
            }
            case "hard" -> {
                // Set the buttons to glow appropriately
                setGlowing(3, false);
                setGlowing(4, false);
                setGlowing(5, true);

                // Add the maps to the slots they have a space in
                int mapI = 0;
                for (int slot : mapSlots) {
                    inventory.setItemStack(slot, getMapItem("hard", mapI));
                    mapI++;
                }
            }
            default -> logger.error("State: " + state + " is not allowed in MapInventory#setState()");
        }

        if (player != null) {
            player.playSound(Sound.sound(
                    SoundEvent.UI_BUTTON_CLICK,
                    Sound.Source.PLAYER,
                    1f,
                    1f)
            );
        }

    }

    private List<HashMap<String, String>> getParkourMapsFromRedis() {
        // Init Redis
        Jedis redis = new Jedis(
                configUtils.getOrSetDefault(config.node("database", "redis", "hostname"), "localhost"),
                Integer.parseInt(configUtils.getOrSetDefault(config.node("database", "redis", "port"), "6379"))
        );


        Type typeToken = new TypeToken<ArrayList<HashMap<String, String>>>() {
        }.getType();

        return new Gson().fromJson(redis.get("parkourMaps"), typeToken);
    }

    private void sendToMap(ParkourPlayer player, String mapName) {
        player.playSound(Sound.sound(
                SoundEvent.BLOCK_NOTE_BLOCK_PLING,
                Sound.Source.PLAYER,
                1f,
                1f)
        );

        // Get a map from string
        ParkourMap map = parkourMaps.stream().filter(filterMap -> Objects.equals(filterMap.name(), mapName)).findFirst().orElse(null);

        Objects.requireNonNull(map).teleportSpawn(player);

        logger.info("Sent " + player.getUsername() + " to " + mapName);

        player.closeInventory();
    }

    public static Inventory getInventory() {
        return new MapInventory().inventory;
    }
}