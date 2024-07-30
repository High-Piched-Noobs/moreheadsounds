package de.thojo0.moreheadsounds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.SkullMeta;

public class EventListener implements Listener {
    // Mapping from texture hashes to lists of NamespacedKeys for sounds
    private HashMap<String, ArrayList<NamespacedKey>> textureToSounds = new HashMap<>();

    /**
     * Constructor: initializes the textureToSounds map based on the configuration
     * 
     * @param config The FileConfiguration object containing sound mappings.
     */
    EventListener(FileConfiguration config) {
        // Get all configuration values
        Map<String, Object> configValues = config.getValues(false);
        // Iterate over all available sounds
        for (Sound sound : Sound.values()) {
            // Get the NamespacedKey for the current sound
            NamespacedKey soundKey = sound.getKey();
            // Iterate over the configuration values
            configValues.forEach((textureHash, c) -> {
                // Get the list of excluded sounds for the current texture
                List<String> exclude = ((MemorySection) c).getStringList("exclude");
                for (String ex : exclude) {
                    // Split the exclude string into namespace and key
                    String[] splitEx = ex.split(":");
                    String namespace = NamespacedKey.MINECRAFT;
                    String excludeKey = splitEx[0];
                    if (splitEx.length > 1) {
                        namespace = splitEx[0];
                        excludeKey = splitEx[1];
                    }
                    // Skip the sound if it is in the exclude list
                    if (namespace.equals(soundKey.getNamespace()) && soundKey.getKey().startsWith(excludeKey))
                        return;
                }
                // Get the list of included sounds for the current texture
                List<String> include = ((MemorySection) c).getStringList("include");
                for (String in : include) {
                    // Split the include string into namespace and key
                    String[] splitIn = in.split(":");
                    String namespace = NamespacedKey.MINECRAFT;
                    String includeKey = splitIn[0];
                    if (splitIn.length > 1) {
                        namespace = splitIn[0];
                        includeKey = splitIn[1];
                    }
                    // Add the sound to the textureToSounds map if it is in the include list
                    if (namespace.equals(soundKey.getNamespace()) && soundKey.getKey().startsWith(includeKey)) {
                        textureToSounds.computeIfAbsent(textureHash, k -> new ArrayList<>()).add(soundKey);
                        return;
                    }
                }
            });
        }
    }

    @SuppressWarnings("deprecation") // Suppress warning for deprecated method getId()
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Get the block that was clicked
        Block block = event.getClickedBlock();
        // Check if the action was a right-click on a note block
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || block.getType() != Material.NOTE_BLOCK) {
            return;
        }
        // Get the data for the note block
        NoteBlock blockdata = (NoteBlock) block.getBlockData();
        // Check if the note block is associated with a custom head
        if (blockdata.getInstrument() != Instrument.CUSTOM_HEAD) {
            return;
        }
        // Get the block above the note block
        Block usedHead = block.getRelative(BlockFace.UP);
        // Check if the block above is a player head
        if (usedHead.getType() != Material.PLAYER_HEAD) {
            return;
        }
        // Get the skull data and texture hash for the player head
        Skull skull = (Skull) usedHead.getState();
        String[] textureUrl = skull.getOwnerProfile().getTextures().getSkin().toString().split("/");
        String textureHash = textureUrl[textureUrl.length - 1];
        // Check if textureHash is defined
        if (!textureToSounds.containsKey(textureHash)) {
            return;
        };
        // Get the possible sounds for the texture hash
        ArrayList<NamespacedKey> possibleSounds = textureToSounds.get(textureHash);
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
            SkullMeta metaData = (SkullMeta) item.getItemStack().getItemMeta();

            // Extracts the texture hash from the player head's skin URL
            String[] textureUrl = metaData.getOwnerProfile().getTextures().getSkin().toString().split("/");
            String textureHash = textureUrl[textureUrl.length - 1];

            // If the texture hash is in the textureToSounds map, remove the sound from the
            // metadata
            if (textureToSounds.containsKey(textureHash)) {
                metaData.setNoteBlockSound(null);
                item.getItemStack().setItemMeta(metaData);
            }
        }
    }
}