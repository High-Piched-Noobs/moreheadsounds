# MoreHeadSounds

**Latest Version:** (Placeholder für Latest Version Badge!)

**Authors:** thojo0 & PYZR

## Description
MoreHeadSounds is a Minecraft plugin that enhances the gameplay experience by allowing custom sounds to be associated with player heads and note blocks. This plugin provides a unique way to customize the sounds in your Minecraft world, adding a layer of personalization and fun to player interactions and events.

## Features
- **Custom Sounds for Player Heads:** Assign specific sounds to player heads, which can be triggered when interacting with note blocks.
- **Dynamic Sound Configuration:** Easily configure which sounds should be included or excluded for specific player heads using the configuration file.
- **Commands for Easy Management:**
  - **/moreheadsounds reload:** Reload the plugin's configuration without restarting the server.
  - **/moreheadsounds clearItem:** Clear the custom sound from the player head you are holding.
  - **/moreheadsounds getHash:** Retrieve the texture hash of the player head you are holding or looking at, useful for debugging and configuration.
- **Tab Completion:** Provides convenient tab completion for commands and coordinates, making it easier to use the plugin's features.
- **Event Handling:** Automatically handles events such as player interactions with note blocks and block drops to ensure the custom sounds are correctly applied.

## Commands and Permissions

- **/moreheadsounds reload**
  - **Permission:** moreheadsounds.reload
  - **Description:** Reloads the plugin configuration.

- **/moreheadsounds clearItem**
  - **Permission:** moreheadsounds.clearItem
  - **Description:** Clears the custom sound from the player head in your main hand.

- **/moreheadsounds getHash**
  - **Permission:** moreheadsounds.getHash
  - **Description:** Retrieves the texture hash of the player head in your main hand or the one you are looking at.

## Configuration

The plugin's configuration file allows server administrators to specify which sounds should be included or excluded for specific player heads. This enables fine-tuned control over the custom sounds applied to player heads in your Minecraft world.

## Installation

1. Download the MoreHeadSounds plugin jar file.
2. Place the jar file in your server's `plugins` directory.
3. Start or restart your server.
4. The plugin will create a default configuration file in the `plugins/MoreHeadSounds` directory.
5. Edit the configuration file as needed to customize the sounds for player heads.
6. Use `/moreheadsounds reload` to apply any changes made to the configuration file.

## Usage

- Hold a player head and use the `/moreheadsounds clearItem` command to remove any custom sound from it.
- Use the `/moreheadsounds getHash` command to get the texture hash of a player head, which can be used in the configuration file.
- Place a note block and a player head above it to trigger the custom sound when interacting with the note block.

## Support

For support and more information, visit the plugin's official page or contact the author.

Enhance your Minecraft world with custom sounds for player heads and create a unique audio experience for your players with MoreHeadSounds!
