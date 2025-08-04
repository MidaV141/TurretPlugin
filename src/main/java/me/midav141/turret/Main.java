package me.midav141.turret;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;

import java.util.*;

public class Main extends JavaPlugin implements Listener, org.bukkit.command.CommandExecutor {

    private final Set<ArmorStand> turrets = new HashSet<>();
    private static final NamespacedKey TURRET_CORE_KEY = NamespacedKey.minecraft("turret_core");

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        startTurretTask();
        registerTurretCoreRecipe();
        getCommand("giveturretcore").setExecutor(this);
        getLogger().info("TurretPlugin enabled!");
    }

    // ==== /giveturretcore: только для оператора ====
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("giveturretcore")) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage("Command only for players!");
                return true;
            }
            if (!p.isOp()) {
                p.sendMessage("§cOperator only!");
                return true;
            }
            p.getInventory().addItem(getTurretCoreItem());
            p.sendMessage("§aGiven: §eTurret Core");
            return true;
        }
        return false;
    }

    // === Крафт и предмет для установки турели ===

    private void registerTurretCoreRecipe() {
        ItemStack turretCore = getTurretCoreItem();
        ShapedRecipe recipe = new ShapedRecipe(TURRET_CORE_KEY, turretCore);
        recipe.shape("IRI", "RDR", "IRI");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('D', Material.DISPENSER);
        Bukkit.addRecipe(recipe);
    }

    private ItemStack getTurretCoreItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR); // Можно заменить на любой предмет
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Turret Core");
        meta.setLore(Arrays.asList("§7Place on the ground to create a turret"));
        item.setItemMeta(meta);
        return item;
    }

    // === Установка турели правым кликом Turret Core ===
    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName() || !meta.getDisplayName().contains("Turret Core")) return;

        Block clicked = event.getClickedBlock();
        if (clicked == null) return;
        Location loc = clicked.getLocation().add(0, 1, 0);
        World world = loc.getWorld();
        if (!world.getBlockAt(loc).isEmpty()) {
            event.getPlayer().sendMessage("§cNo space for turret!");
            return;
        }
        // Спавним турель
        ArmorStand as = world.spawn(loc.add(0.5, 0, 0.5), ArmorStand.class, st -> {
            st.setInvisible(false);
            st.setInvulnerable(true);
            st.setGravity(false);
            st.setCustomName("Turret");
            st.setCustomNameVisible(true);
            st.setMarker(false);
            st.setArms(true);
            st.setBasePlate(false);
            st.setItem(EquipmentSlot.HEAD, new ItemStack(Material.DISPENSER));
            st.setItem(EquipmentSlot.CHEST, new ItemStack(Material.IRON_CHESTPLATE));
            st.setItem(EquipmentSlot.HAND, new ItemStack(Material.BOW));
        });
        turrets.add(as);
        event.getPlayer().sendMessage(ChatColor.GREEN + "Turret placed!");

        // Минус 1 предмет, если не креатив
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
        }
        event.setCancelled(true);
    }

    // === Удаление турели ударом ===
    @EventHandler
    public void onTurretBreak(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ArmorStand stand)) return;
        if (!(event.getDamager() instanceof Player p)) return;
        if (!"Turret".equals(stand.getCustomName())) return;
        stand.remove();
        turrets.remove(stand);
        p.sendMessage(ChatColor.RED + "Turret destroyed!");
        event.setCancelled(true);
    }

    // === Логика турели: стреляет по hostile-мобам с прямой видимостью ===
    private void startTurretTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                turrets.removeIf(as -> !as.isValid());
                for (ArmorStand turret : turrets) {
                    LivingEntity target = findNearestMob(turret.getLocation(), 20, turret);
                    if (target != null) {
                        Location tl = turret.getLocation();
                        Location tlk = target.getLocation().clone().add(0, target.getHeight() / 2.0, 0);
                        Vector dir = tlk.subtract(tl).toVector().normalize();
                        tl.setDirection(dir);
                        turret.teleport(tl);
                        if (turret.getTicksLived() % 8 == 0) {
                            Arrow a = turret.getWorld().spawnArrow(
                                tl.clone().add(0, 0.8, 0).add(dir.multiply(1.1)),
                                dir, 2.5f, 0
                            );
                            a.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
                            a.setShooter(turret);
                        }
                    }
                }
            }
        }.runTaskTimer(this, 1L, 1L);
    }

    // Только hostile-мобы и только если видно!
    private LivingEntity findNearestMob(Location from, double radius, ArmorStand turret) {
        World world = from.getWorld();
        return world.getNearbyEntities(from, radius, radius, radius).stream()
            .filter(e -> e instanceof Monster)
            .filter(e -> e instanceof LivingEntity le
                && !le.isDead()
                && turret.hasLineOfSight(le))
            .map(e -> (LivingEntity) e)
            .min(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(from)))
            .orElse(null);
    }
}
