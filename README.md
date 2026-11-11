# 🎣 Magic Fishing

**A fishing-themed Slimefun4 addon.** Catch magic resource fish with a special rod, refine them into
resources forever, and breed them in an aquarium. Built with skyblock servers in mind: no ore
generation needed, just water.

> ⚠️ **Status: experimental.** Expect balance changes between versions. Back up your server before updating.

---

## ✨ Features

- **Magic Fishing Rod**: catches *resource fish* for both Minecraft and Slimefun materials.
- **Rarity system**: Common, Uncommon, Rare, Epic and Legendary, based on how hard each resource is to obtain or craft.
- **Rod bait**: every cast needs a Fish Pellet put on the rod by hand, which stops auto-fish mods and AFK fishing.
- **Optional catch minigame**: a boss-bar timing challenge (off by default). Works on Java and Bedrock (Geyser).
- **Magic Fish Refinery I / II / III**: turns a fish into its resource. **The fish is never consumed.**
- **Magic Aquarium**: breeds 2 fish of the same type into a new one, using Fish Pellet.
- **200+ resource fish**: ores, logs, leaves, mob drops, wool, glass, stone types, farm crops, honey, Slimefun dusts, ingots, alloys, components and more.
- **Stackable fish** (up to 64): fish are vanilla fish items, so they stack on every version, Java and Bedrock.
- Rods **cannot be enchanted** (enchanting table, anvil or Slimefun Auto Enchanter).
- **Admin commands** (`/mf give`, `/mf list`, `/mf reload`) and Slimefun **researches**.
- **GitHub button** in the Magic Fishing guide category.
- Almost everything is **configurable**.

---

## 📦 Requirements

| | |
|---|---|
| Server | Paper (or a fork) **1.20+** |
| Java | 17+ |
| Dependency | [Slimefun4](https://github.com/Slimefun/Slimefun4) (built against **RC-37**) |
| Optional | [Geyser](https://geysermc.org/) for Bedrock players |

## 🔧 Installation

1. Download `MagicFishing-<version>.jar` from the [Releases](../../releases) page (if available), or from
   the latest successful [Actions build](../../actions) under *Artifacts*.
2. Put it in your server's `plugins/` folder, next to Slimefun.
3. Restart the server. A `plugins/MagicFishing/config.yml` file is created.
4. Open the Slimefun Guide and find the **Magic Fishing** category.

---

## 🚀 Getting started

```
Fish Pellet ──┐
Aqua Core ────┼──► Magic Fishing Rod ──► fish! ──► Magic Fish Refinery ──► resources (forever)
              │                             │
              └─────────────────────────────┴────► Magic Aquarium ──► more fish
```

1. Craft **Fish Pellet** (bait and fish food). One craft gives 8.
2. Craft an **Aqua Core**. It is used in the rod and every machine.
3. Craft the **Magic Fishing Rod**.
4. **Bait the rod**: open your inventory, pick up a Fish Pellet and click the rod with it.
5. Fish normally. Each fish caught uses the bait, so bait the rod again before the next cast.
6. Put the fish in a **Magic Fish Refinery** to get its resource, over and over.
7. Use the **Magic Aquarium** to breed more fish of a type.

---

## 🧰 Items and recipes

### Materials

| Item | Recipe type | Recipe |
|---|---|---|
| **Fish Pellet** ×8 | Enhanced Crafting Table | Wheat, Magical Lump I, Wheat / Dried Kelp, Cod, Dried Kelp / Bone Meal, Salmon, Bone Meal |
| **Aqua Core** | Magic Workbench | Synthetic Sapphire, Magical Lump III, Synthetic Sapphire / Ender Lump III, Ancient Rune [Water], Ender Lump III / Prismarine Crystals, Magical Lump III, Prismarine Crystals |

### Rod

| Item | Recipe type | Recipe |
|---|---|---|
| **Magic Fishing Rod** | Magic Workbench | Magical Lump III, Aqua Core, Magical Lump III / Hardened Metal, Fishing Rod, Hardened Metal / Magical Lump III, Aqua Core, Magical Lump III |

### Machines

All machines are crafted in the **Enhanced Crafting Table** and need **electricity**.

| Machine | Energy | Speed | Recipe |
|---|---|---|---|
| **Magic Fish Refinery I** | 100 J/tick | 1 output / 15 s | Hardened Metal, Aqua Core, Hardened Metal / Electric Motor, Ancient Rune [Water], Electric Motor / Hardened Metal, Medium Capacitor, Hardened Metal |
| **Magic Fish Refinery II** | 250 J/tick | 1 output / 8 s | Reinforced Alloy, Aqua Core, Reinforced Alloy / Advanced Circuit Board, Refinery I, Advanced Circuit Board / Reinforced Alloy, Large Capacitor, Reinforced Alloy |
| **Magic Fish Refinery III** | 1000 J/tick | 1 output / 2 s | Blistering Ingot, Aqua Core, Blistering Ingot / Carbonado, Refinery II, Carbonado / Reinforced Plate, Carbonado Edged Capacitor, Reinforced Plate |
| **Magic Aquarium** | 150 J/tick | 1 fish / 180 s | Hardened Glass ×3 / Aqua Core, Ancient Rune [Water], Aqua Core / Reinforced Alloy, Advanced Circuit Board, Reinforced Alloy |

> **Cargo:** cargo nodes can **pull outputs** from every machine and **insert Fish Pellet** into the
> Magic Aquarium. Fish inputs are placed by hand, since they are never consumed. All machines use the
> normal Slimefun **energy network**.

---

## 🐟 Fish and rarity

Every catch is rolled in three steps:

1. **Category**: Minecraft fish or Slimefun fish (50 / 50 by default).
2. **Rarity** tier.
3. A random fish of that tier. Big families (**wool, glass, glass panes, dyes, flowers, common farm crops**) count as
   *one* fish in this step, and then a random color or crop is picked, so they don't flood the Common pool.

| Rarity | Tier chance | Per catch (Minecraft) | Per catch (Slimefun) | Icon |
|---|---|---|---|---|
| Common | 50% | 25% | 25% | Cod |
| Uncommon | 28% | 14% | 14% | Salmon |
| Rare | 15% | 7.5% | 7.5% | Tropical Fish |
| Epic | 6% | 3% | 3% | Salmon ✨ (glint) |
| Legendary | 1% | 0.5% | 0.5% | Tropical Fish ✨ (glint) |

The fish's **name color matches its output item**, and each fish's lore shows its rarity and catch chance.

<details>
<summary><b>Minecraft fish</b> (click to expand)</summary>

| Rarity | Fish |
|---|---|
| **Common** | Coal, Copper · Oak / Spruce / Birch / Jungle / Acacia / Dark Oak / Mangrove Log and Leaves, Azalea Leaves · Rotten Flesh, Bone, Arrow, String, Spider Eye, Gunpowder, Leather, Feather, Egg, Beef, Porkchop, Chicken, Mutton, Ink Sac, Gold Nugget · All Wool · Glass and all Stained Glass · Glass Pane and all Stained Glass Panes · Stone, Cobblestone, Gravel, Sand, Diorite, Granite, Andesite, Clay, Clay Ball, Snow Block, Snowball, Flint, Bone Block, Netherrack, Lily Pad, Ice · Wheat, Dried Kelp, Bone Meal, Cod, Salmon · All 16 Dyes · Flowers (Dandelion, Poppy, Blue Orchid, Allium, Azure Bluet, all Tulips, Oxeye Daisy, Cornflower, Lily of the Valley, Sunflower, Lilac, Rose Bush, Peony, Pink Petals, Eyeblossoms) · Farm crops (Seeds, Carrot, Potato, Beetroot, Beetroot Seeds, Melon Slice, Pumpkin, Sugar Cane, Cactus, Bamboo, Kelp, Sweet Berries, Cocoa Beans, Mushrooms) |
| **Uncommon** | Iron, Gold, Lapis, Redstone, Quartz · Cherry / Pale Oak Log and Leaves, Flowering Azalea Leaves, Crimson / Warped Stem · Slime Ball, Magma Cream, Rabbit, Rabbit Hide, Glow Ink Sac, Prismarine Shard, Prismarine Crystals · Tinted Glass · End Stone, Soul Sand, Obsidian, Magma Block, Quartz Block, Water Bucket, Lava Bucket, Packed Ice · Nether Wart, Glow Berries · Tropical Fish, Pufferfish · Honeycomb, Honey Bottle |
| **Rare** | Diamond, Emerald · Ender Pearl, Blaze Rod, Ghast Tear, Phantom Membrane, Rabbit Foot, Armadillo Scute, Breeze Rod, Nautilus Shell, Turtle Scute · Blue Ice · Chorus Fruit, Torchflower Seeds, Pitcher Pod · Wither Rose, Torchflower, Pitcher Plant, Spore Blossom · Honey Block, Honeycomb Block |
| **Epic** | Shulker Shell, Wither Skeleton Skull, Totem of Undying |
| **Legendary** | Netherite Scrap, Nether Star |

Items that don't exist on your server version (e.g. Pale Oak on 1.20) are skipped automatically.

</details>

<details>
<summary><b>Slimefun fish</b> (click to expand)</summary>

| Rarity | Fish |
|---|---|
| **Common** | Iron, Gold, Copper, Tin, Silver, Aluminum, Lead, Zinc and Magnesium Dust · Sifted Ore |
| **Uncommon** | Aluminum, Copper, Tin, Silver, Lead, Zinc and Magnesium Ingot · 4K to 10K Gold · Magical Lump I, Ender Lump I |
| **Rare** | Nickel, Cobalt · 12K to 18K Gold · Bronze, Brass, Billon, Solder, Steel, Aluminum Brass, Aluminum Bronze · Heating Coil, Electromagnet, Electric Motor · Magical Lump II, Ender Lump II |
| **Epic** | 20K to 24K Gold · Duralumin, Corinthian Bronze, Damascus Steel, Redstone Alloy, Gilded Iron, Hardened Metal · Plastic Sheet, Uranium, Blistering Ingot (33%), Blistering Ingot (66%) · Magical Lump III, Ender Lump III |
| **Legendary** | Reinforced Alloy Ingot, Blistering Ingot |

</details>

Magic fish **cannot be eaten, cooked** (furnace, smoker, campfire) **or fed** to animals.

---

## 🪱 Rod bait

- A Magic Fishing Rod can only be **cast when it has bait**. The rod's lore shows `Bait: Fish Pellet` or `Bait: None`.
- To bait it, open your inventory, pick up a **Fish Pellet** with the cursor and **click the rod** (left or right click). This uses 1 pellet.
- Catching a fish uses the bait. Reeling in without a fish keeps it.
- Auto-fish mods and AFK setups can't re-bait the rod, so they stop after one fish.

---

## 🎮 Catch minigame (optional)

Disabled by default. Set `minigame.enabled: true` in the config to turn it on.

When a magic fish bites and you reel in, a **boss bar** appears:

```
| | | | | | | | | | | | | | | | | | | | | | | | | | | | | |
            ^^^^^^^^ green zone      ┃ marker
```

- The **green zone** is placed randomly. The **white marker** moves back and forth.
- **Sneak** while the marker is inside the green zone to catch the fish.
- The bar drains over **5 seconds**. If you miss or run out of time, the fish gets away (the pellet is still used).
- Rarer fish have a **smaller zone** and a **faster marker**.

| Rarity | Zone size | Marker speed |
|---|---|---|
| Common | 8 | 0.6 |
| Uncommon | 7 | 0.7 |
| Rare | 6 | 0.9 |
| Epic | 4 | 1.1 |
| Legendary | 3 | 1.3 |

**Bedrock friendly:** any sneak *press or release* counts, so the toggle sneak button on mobile works.
There is a short grace period at the start, so players already holding Shift don't fail instantly.

---

## 🐠 Magic Aquarium

- Put **2 fish of the same type** and **Fish Pellet** in the aquarium (pellets can also come from cargo).
- It consumes **16 Fish Pellet per new fish**, gradually: 1 pellet every 6.25% of progress.
- If pellets run out, the aquarium **pauses** and keeps its progress until you add more.
- The parent fish are never consumed.

---

## 📚 Research

Items are unlocked with Slimefun researches (XP levels). Costs can be changed in
`plugins/Slimefun/Researches.yml`.

| Research | Cost | Unlocks |
|---|---|---|
| Fish Pellet | 8 | Fish Pellet |
| Aqua Core | 15 | Aqua Core |
| Magic Fishing Rod | 20 | Magic Fishing Rod |
| Magic Fish Refinery I | 25 | Magic Fish Refinery I |
| Magic Aquarium | 30 | Magic Aquarium |
| Magic Fish Refinery II | 35 | Magic Fish Refinery II |
| Magic Fish Refinery III | 45 | Magic Fish Refinery III |

Fish don't need a research: they are caught.

---

## 💬 Commands and permissions

Alias: `/mf` = `/magicfishing`.

| Command | Permission | Description |
|---|---|---|
| `/mf help` | (everyone) | Show the commands |
| `/mf github` | (everyone) | Get the link to this page |
| `/mf give <player> <item> [amount]` | `magicfishing.admin` | Give any Magic Fishing item (tab completion for ids) |
| `/mf list [page]` | `magicfishing.admin` | List all Magic Fishing item ids |
| `/mf reload` | `magicfishing.admin` | Reload fishing chances and minigame settings |

`magicfishing.admin` is given to operators by default.

---

## ⚙️ Configuration

`plugins/MagicFishing/config.yml` (defaults shown):

```yaml
fishing:
  slimefun-chance: 50.0          # % of catches that are Slimefun fish
  rarity-chance:                 # relative weights, don't need to add up to 100
    COMMON: 50.0
    UNCOMMON: 28.0
    RARE: 15.0
    EPIC: 6.0
    LEGENDARY: 1.0

minigame:
  enabled: false                 # true = timing minigame on every catch
  seconds: 5.0                   # time limit
  grace-ticks: 5                 # ignore input for the first ticks
  tolerance: 1                   # extra segments allowed on each side of the zone
  difficulty:
    COMMON:    { zone-size: 8, speed: 0.6 }
    UNCOMMON:  { zone-size: 7, speed: 0.7 }
    RARE:      { zone-size: 6, speed: 0.9 }
    EPIC:      { zone-size: 4, speed: 1.1 }
    LEGENDARY: { zone-size: 3, speed: 1.3 }

machines:
  MF_FISH_REFINERY_1: { energy-per-tick: 100,  seconds-per-output: 15 }
  MF_FISH_REFINERY_2: { energy-per-tick: 250,  seconds-per-output: 8 }
  MF_FISH_REFINERY_3: { energy-per-tick: 1000, seconds-per-output: 2 }
  MF_MAGIC_AQUARIUM:  { energy-per-tick: 150,  seconds-per-output: 180, pellets-per-fish: 16 }
```

`/mf reload` applies the `fishing` and `minigame` settings right away. Machine energy and speed, the
aquarium pellet cost and the chances shown in item lore are applied after a **restart**.

Slimefun's own per-item settings (enabling/disabling items) are in `plugins/Slimefun/Items.yml`, and
research costs are in `plugins/Slimefun/Researches.yml`.

---

## 📱 Bedrock (Geyser) notes

- Rod bait works on Bedrock: select the Fish Pellet, then tap the rod.
- The minigame and boss bar work on Bedrock.
- Fish are plain vanilla items, so they look and stack the same on Bedrock.

---

## 🛠️ Building from source

```bash
git clone https://github.com/mybezo/Magic-Fishing.git
cd Magic-Fishing
./gradlew shadowJar
```

The jar is written to `build/libs/MagicFishing-<version>.jar`. Every push is also built by GitHub Actions.

---

## 🐞 Bugs and suggestions

Open an [issue](../../issues) and include:
- Server software and version (`/version`)
- Slimefun version (`/sf versions`)
- Steps to reproduce, and any console errors

You can also open this page in game: click the **GitHub** button at the top of the Magic Fishing guide
category, or run `/mf github`.

---

## 📄 License

Magic Fishing is licensed under the [GNU General Public License v3.0](LICENSE), the same license as Slimefun4.
