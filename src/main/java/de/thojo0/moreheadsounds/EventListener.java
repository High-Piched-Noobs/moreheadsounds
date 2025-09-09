package de.thojo0.moreheadsounds;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

public class EventListener implements Listener {
    // Mapping from texture hashes to lists of NamespacedKeys for sounds
    private final Map<String, List<NamespacedKey>> textureToSounds = new HashMap<>();

    EventListener(FileConfiguration config) {
        reloadConfig(config);
    }

    /**
     * initializes the textureToSounds map based on the configuration
     * 
     * @param config The FileConfiguration object containing sound mappings.
     */
    public void reloadConfig(FileConfiguration config) {
        textureToSounds.clear();
        // Get all configuration values
        final Map<String, Object> configValues = config.getValues(false);

        // Iterate over each texture hash configuration
        configValues.forEach((textureHash, c) -> {
            if (!(c instanceof ConfigurationSection)) {
                return;
            }
            ConfigurationSection section = (ConfigurationSection) c;
            final List<String> include = section.getStringList("include");
            final List<String> exclude = section.getStringList("exclude");

            Set<NamespacedKey> sounds = new TreeSet<>();

            // 1. include: endsWith(".")
            for (String entry : include) {
                if (entry.endsWith(".")) {
                    String pattern = entry.substring(0, entry.length());
                    NamespacedKey key = NamespacedKey.fromString(pattern);
                    for (Sound sound : Sound.values()) {
                        if (sound.getKey().getNamespace().equals(key.getNamespace())
                                && sound.getKey().getKey().startsWith(key.getKey())) {
                            sounds.add(sound.getKey());
                        }
                    }
                }
            }

            // 2. exclude: endsWith(".")
            Set<NamespacedKey> toRemoveCategories = new HashSet<>();
            for (String entry : exclude) {
                if (entry.endsWith(".")) {
                    String pattern = entry.substring(0, entry.length());
                    NamespacedKey key = NamespacedKey.fromString(pattern);
                    for (NamespacedKey soundKey : sounds) {
                        if (soundKey.getNamespace().equals(key.getNamespace())
                                && soundKey.getKey().startsWith(key.getKey())) {
                            toRemoveCategories.add(soundKey);
                        }
                    }
                }
            }
            sounds.removeAll(toRemoveCategories);

            // 3. include: not endsWith(".")
            for (String entry : include) {
                if (!entry.endsWith(".")) {
                    sounds.add(NamespacedKey.fromString(entry));
                }
            }

            // 4. exclude: not endsWith(".")
            for (String entry : exclude) {
                if (!entry.endsWith(".")) {
                    sounds.remove(NamespacedKey.fromString(entry));
                }
            }

            if (!sounds.isEmpty()) {
                textureToSounds.put(textureHash, new ArrayList<>(sounds));
            }
        });
    }

    @SuppressWarnings("deprecation") // Suppress warning for deprecated method getId()
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Get the block that was clicked
        final Block block = event.getClickedBlock();
        // Check if the action was a right-click on a note block
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || block.getType() != Material.NOTE_BLOCK)
            return;
        // Get the data for the note block
        final NoteBlock blockdata = (NoteBlock) block.getBlockData();
        // Check if the note block is associated with a custom head
        if (blockdata.getInstrument() != Instrument.CUSTOM_HEAD)
            return;
        // Get the block above the note block
        final Block usedHead = block.getRelative(BlockFace.UP);
        // Check if the block above is a player head
        if (usedHead.getType() != Material.PLAYER_HEAD)
            return;
        // Get the skull data and texture hash for the player head
        final Skull skull = (Skull) usedHead.getState();
        final String textureHash = getTextureHash(skull.getOwnerProfile());
        // Check if textureHash is defined
        if (textureHash == null || !textureToSounds.containsKey(textureHash))
            return;
        // Get the possible sounds for the texture hash
        final List<NamespacedKey> possibleSounds = textureToSounds.get(textureHash);
        // Set the note block sound based on the note value
        skull.setNoteBlockSound(possibleSounds.get(((blockdata.getNote().getId() + 1) % 25) % possibleSounds.size()));
        // Update the skull without sending updates to clients or applying physics
        skull.update(false, false);
    }

    @EventHandler
    public void onBlockDropItem(BlockDropItemEvent event) {
        // Iterate over the items dropped from the block
        for (Item item : event.getItems()) {
            // Check if the item is a player head
            if (item.getItemStack().getType() != Material.PLAYER_HEAD)
                continue;

            // Get the metadata for the player head and its texture hash
            final SkullMeta metaData = (SkullMeta) item.getItemStack().getItemMeta();

            // Get the textureHash from the item
            String textureHash = getTextureHash(metaData.getOwnerProfile());
            // Try again from block
            if (textureHash == null)
                textureHash = getTextureHash(((Skull) event.getBlockState()).getOwnerProfile());

            // If the texture hash is in the textureToSounds map, remove the sound from the
            // metadata
            if (textureHash != null && textureToSounds.containsKey(textureHash)) {
                metaData.setNoteBlockSound(null);
                item.getItemStack().setItemMeta(metaData);
            }
        }
    }

    public List<NamespacedKey> getSounds(String textureHash) {
        return textureToSounds.get(textureHash);
    }

    // Extracts the texture hash from the player head's skin URL
    public static String getTextureHash(PlayerProfile owner) {
        if (owner == null)
            return null;
        final URL skin = owner.getTextures().getSkin();
        if (skin == null)
            return null;
        final String[] textureUrl = skin.toExternalForm().split("/");
        return textureUrl[textureUrl.length - 1];
    }
}