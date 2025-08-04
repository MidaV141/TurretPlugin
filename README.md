# TurretPlugin

**TurretPlugin** is a plugin for [PaperMC](https://papermc.io/) 1.20.x / 1.21.x that adds **automatic turrets** to your Minecraft server — with no mods required! Turrets protect your area by automatically targeting and shooting hostile mobs that are in direct line of sight. They’re crafted and placed like regular Minecraft items, and can be removed individually by players.

---

## 🚀 Features

- **Craftable "Turret Core"** item (see recipe below)
- **Place turrets** by right-clicking a block with the "Turret Core"
- Turrets **automatically aim and shoot** only at hostile mobs, if they are in direct line of sight (won’t shoot through walls)
- Turrets are immovable and immune to explosions or fire
- **Break a turret by hitting it** (only the targeted turret is removed)
- `/giveturretcore` command for giving yourself the core (admin/OP only)
- No client mods required — works out-of-the-box!

---

## 📦 Turret Core Crafting Recipe

<details>
  <summary>Click to view recipe</summary>
  I R I
  
  R D R
  I R I
  
- **I** — Iron Ingot (`IRON_INGOT`)
- **R** — Redstone (`REDSTONE`)
- **D** — Dispenser (`DISPENSER`)
- Center: Dispenser, surrounded by iron and redstone

</details>

---

## 🛠️ Installation

1. **Build the project:**  
   - Requires JDK 21+, Maven  
   - In the project root, run:
     ```
     mvn clean package -DskipTests
     ```
2. Copy the generated file `target/TurretPlugin-1.1.jar` to your server’s `plugins` folder.
3. Restart your PaperMC server.

---

## ⚙️ Usage

- **Get a Turret Core:**  
  Craft it (see above), or use the command (OPs only):
/giveturretcore
- **Place a turret:**  
Hold the core in your hand and right-click any block.
- **Break a turret:**  
Simply hit the turret (with hand or tool) to remove it.
- **Turrets automatically defend the area** by shooting at hostile mobs in line of sight (not through walls).

---

## 🔒 Permissions & Restrictions

- `/giveturretcore` — OPs only (`isOp()` check)
- Turrets cannot be destroyed by explosions; only a player hit will remove them

---

## ✍️ Author & License

- Author: [@MidaV141](https://github.com/MidaV141)
- Open source, MIT License

---

## 💡 Ideas for improvements

- Upgradable turrets (damage, rate of fire, projectile type)
- Region/player-based turret ownership and protection
- Different turret types (melee, fire, etc.)
- Permissions via Permission API
- Configurable parameters (range, damage, cooldown, etc.)

---

**Pull requests and forks are welcome!**  
For questions or bug reports, use [Issues](https://github.com/yourname/TurretPlugin/issues).

---
