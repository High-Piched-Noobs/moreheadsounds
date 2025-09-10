# MoreHeadSounds

![GitHub Release](https://img.shields.io/github/v/release/High-Piched-Noobs/moreheadsounds?display_name=release) ![GitHub License](https://img.shields.io/github/license/High-Piched-Noobs/moreheadsounds)


**Authors:** thojo0 & PYZR

## Description
MoreHeadSounds is a Minecraft plugin that enhances the gameplay experience by allowing custom sounds to be associated with player heads and note blocks. This plugin provides a unique way to customize the sounds in your Minecraft world, adding a layer of personalization and fun to player interactions and events.

## Features
- **Custom Sounds for Player Heads:** Assign specific sounds to player heads, which can be triggered when interacting with note blocks.
- **Dynamic Sound Configuration:** Easily configure which sounds should be included or excluded for specific player heads using the configuration file.
- **Commands for Easy Management:**
  - **/moreheadsounds config:** Allows changing the configuration.
  - **/moreheadsounds clearItem:** Clear the custom sound from the player head you are holding.
  - **/moreheadsounds getHash:** Retrieve the texture hash of the player head you are holding or looking at, useful for debugging and configuration.
- **Tab Completion:** Provides convenient tab completion for commands and coordinates, making it easier to use the plugin's features.

## Commands and Permissions

- **/moreheadsounds config reload**
  - **Permission:** `moreheadsounds.config`
  - **Description:** Reloads the plugin configuration.

- **/moreheadsounds config list [page]**
  - **Permission:** `moreheadsounds.config`
  - **Description:** Lists all the configured head hashes.

- **/moreheadsounds config include <sound>**
  - **Permission:** `moreheadsounds.config`
  - **Description:** Adds a sound to the include list of the held head.

- **/moreheadsounds config exclude <sound>**
  - **Permission:** `moreheadsounds.config`
  - **Description:** Adds a sound to the exclude list of the held head.

- **/moreheadsounds config remove <sound>**
  - **Permission:** `moreheadsounds.config`
  - **Description:** Removes a sound from the held head's configuration.

- **/moreheadsounds config show**
  - **Permission:** `moreheadsounds.config`
  - **Description:** Shows the configuration of the held head, including a preview of the active sounds.

- **/moreheadsounds clearItem**
  - **Permission:** `moreheadsounds.clearItem`
  - **Description:** Clears the custom sound from the player head in your main hand.

- **/moreheadsounds getHash**
  - **Permission:** `moreheadsounds.getHash`
  - **Description:** Retrieve the texture hash of the player head you are holding or looking at, useful for debugging and configuration.

## Configuration

The plugin's configuration file (`config.yml`) allows server administrators to specify which sounds should be included or excluded for specific player heads. This enables fine-tuned control over the custom sounds applied to player heads in your Minecraft world.

The configuration is based on the texture hash of the player heads. For each hash, you can specify `include` and `exclude` lists for sounds. The entries must be valid namespaced identifiers.

- **`include`**: A list of sounds or sound categories to be associated with the head.
- **`exclude`**: A list of sounds or sound categories to be excluded.

You can specify a single sound (e.g., `entity.creeper.primed`) including custom sounds from texture packs, or a whole category of sounds by ending with a dot (e.g., `entity.creeper.`).

The processing order is as follows:
1. All sounds from categories in the `include` list are added.
2. All sounds from categories in the `exclude` list are removed.
3. All single sounds from the `include` list are added.
4. All single sounds from the `exclude` list are removed.

**Example:**
```yml
e50294a1747310f104124c6373cc639b712baa57b7d926297b645188b7bb9ab9:
  include:
    - entity.allay.
a3c34722ac64496c9b84d0c54019daae6185d6094990133ad6810eea3d24067a:
  include:
    - entity.parrot.
  exclude:
    - entity.parrot.imitate.
```

## Installation

1. Download the MoreHeadSounds plugin jar file.
2. Place the jar file in your server's `plugins` directory.
3. Start or restart your server.
4. The plugin will create a default configuration file in the `plugins/MoreHeadSounds` directory.
5. Edit the configuration file as needed to customize the sounds for player heads.
6. Use `/moreheadsounds config reload` to apply any changes made to the configuration file.

## Usage

- Hold a player head and use the `/moreheadsounds clearItem` command to remove any custom sound from it.
- Use the `/moreheadsounds getHash` command to get the texture hash of a player head, which can be used in the configuration file.
- Place a note block and a player head above it to trigger the custom sound when interacting with the note block.

## Support

For support and more information, visit the plugin's official page or contact the authors.
