# Changelog

## 1.0.0-dev.1

First public dev snapshot of Magic Fishing.

### 🎣 Fishing
- **Magic Fishing Rod**: one rod for both Minecraft and Slimefun resource fish. It cannot be enchanted (enchanting table, anvil or Slimefun Auto Enchanter).
- **Rod bait**: every cast needs a **Fish Pellet** on the rod. Click the rod with a pellet in your inventory; one bait is used per fish caught. Casting without bait is blocked, which stops auto-fish mods and AFK fishing.
- **Rarity system**: Common, Uncommon, Rare, Epic and Legendary, based on how hard each resource is to obtain or craft.
- **Fair rolls**: category (Minecraft / Slimefun, 50 / 50), then rarity, then a fish. Big families (wool, glass, panes, dyes, flowers, farm crops) count as one fish, then a random color or type is picked.
- **Optional catch minigame**: boss-bar timing challenge, off by default (`minigame.enabled`). Works on Java and Bedrock.

### 🐟 Fish (200+)
- **Minecraft**: ores, logs, leaves, mob drops, all wool / glass / glass panes / dyes, flowers, stone types, ice, nether and end blocks, farm crops, honey, raw fish, Fish Pellet ingredients, Nether Star, Netherite Scrap and more.
- **Slimefun**: dusts, ingots, carat gold, alloys, Hardened Metal, Reinforced Alloy, Blistering Ingots, Magical / Ender Lumps, Heating Coil, Electromagnet, Electric Motor, Plastic Sheet, Uranium.
- Fish are vanilla fish items, so they **stack to 64** on Java and Bedrock. Epic and Legendary fish have an enchant glint.
- The fish's name color matches its output. The lore shows the rarity and catch chance.
- Magic fish can't be eaten, cooked or fed to animals.

### ⚙️ Machines
- **Magic Fish Refinery I / II / III**: turns a fish into its resource every 15 / 8 / 2 seconds. The fish is never consumed.
- **Magic Aquarium**: breeds 2 fish of the same type into a new one, using 16 Fish Pellet per fish (consumed gradually).
- Machine progress is saved and survives restarts.
- Cargo can pull outputs from every machine and insert Fish Pellet into the Aquarium. All machines use the Slimefun energy network.

### 🛠️ Server
- `/mf` commands: `help`, `github`, `give`, `list`, `reload` (`magicfishing.admin` for admin commands).
- Slimefun researches for the pellet, Aqua Core, rod and machines.
- GitHub button in the Magic Fishing guide category.
- Configurable chances, minigame and machine settings, with automatic config migration.

### ⚠️ Known notes
- Dev snapshot: balance may change between versions. Back up your server first.
- Fish from older test builds (bucket / head versions) still work in machines but don't stack with new fish.
