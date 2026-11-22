package com.example.disasters;

import com.example.disasters.event.AntiRepostHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class NaturalDisastersMod implements ModInitializer {
    public static final String MODID = "naturaldisasters";
    private static final List<Item> ALL = new ArrayList<>();

    public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(MODID, "naturaldisasters_tab")
    );

    private static Item reg(String id, String t) {
        Item item = Registry.register(
                BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(MODID, id),
                new DisasterItem(new Item.Properties().stacksTo(1), t)
        );
        ALL.add(item);
        return item;
    }

    private static Vec3 tPos = null, bPos = null;
    private static int tTick = 0, bTick = 0;

    @Override
    public void onInitialize() {
        reg("tornado_spawner", "tornado");
        reg("meteor_spawner", "meteor");
        reg("earthquake_spawner", "earthquake");
        reg("volcano_spawner", "volcano");
        reg("lightning_spawner", "lightning");
        reg("tsunami_spawner", "tsunami");
        reg("blizzard_spawner", "blizzard");
        reg("blackhole_spawner", "blackhole");
        reg("acidrain_spawner", "acidrain");
        reg("nuke_spawner", "nuke");
        reg("sandstorm_spawner", "sandstorm");
        reg("wildfire_spawner", "wildfire");

        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                TAB_KEY,
                FabricItemGroup.builder()
                        .title(Component.literal("Natural Disasters"))
                        .icon(() -> new ItemStack(Items.FIRE_CHARGE))
                        .displayItems((params, output) -> ALL.forEach(output::accept))
                        .build()
        );

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            ALL.forEach(entries::accept);
        });

        ServerTickEvents.END_WORLD_TICK.register(NaturalDisastersMod::onTick);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                AntiRepostHandler.checkAndWarn(handler.getPlayer())
        );
    }
    public static class DisasterItem extends Item {
        private final String type;
        public DisasterItem(Properties p, String t) { super(p); this.type = t; }

        @Override
        public InteractionResultHolder<ItemStack> use(Level w, Player p, InteractionHand h) {
            if (!w.isClientSide && w instanceof ServerLevel serverLevel) {
                run(serverLevel, p, p.blockPosition(), type);
            }
            return InteractionResultHolder.sidedSuccess(p.getItemInHand(h), w.isClientSide());
        }
    }

    private static void run(ServerLevel w, Player p, BlockPos pos, String type) {
        RandomSource r = w.random;
        switch (type) {
            case "tornado":
                tPos = new Vec3(pos.getX() + 8, pos.getY(), pos.getZ() + 8);
                tTick = 500;
                break;
            case "meteor":
                for (int i = 0; i < 10; i++) {
                    Vec3 motion = new Vec3(0, -1.5, 0);
                    LargeFireball m = new LargeFireball(w, p, motion, 7);
                    m.setPos(pos.getX() + (r.nextDouble() - 0.5) * 35, pos.getY() + 60 + i * 5, pos.getZ() + (r.nextDouble() - 0.5) * 35);
                    w.addFreshEntity(m);
                }
                break;
            case "earthquake":
                for (int x = -18; x <= 18; x++) {
                    for (int z = -18; z <= 18; z++) {
                        if (r.nextInt(3) == 0) {
                            for (int y = -6; y <= 3; y++) {
                                BlockPos t = pos.offset(x, y, z);
                                if (!w.isEmptyBlock(t) && !w.getBlockState(t).is(Blocks.BEDROCK)) {
                                    w.destroyBlock(t, false);
                                }
                            }
                        }
                    }
                }
                w.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4.0F, 0.5F);
                break;
            case "volcano":
                int rad = 14;
                for (int y = 0; y <= rad; y++) {
                    int lr = rad - y;
                    for (int x = -lr; x <= lr; x++) {
                        for (int z = -lr; z <= lr; z++) {
                            int d = x * x + z * z, m = lr * lr, in = (lr - 2) * (lr - 2);
                            if (d <= m) {
                                w.setBlockAndUpdate(pos.offset(x + 16, y, z),
                                        (lr > 2 && y > 2 && d < in ? Blocks.LAVA : Blocks.OBSIDIAN).defaultBlockState());
                            }
                        }
                    }
                }
                for (int i = 0; i < 15; i++) {
                    PrimedTnt t = new PrimedTnt(w, pos.getX() + 16, pos.getY() + rad + 3, pos.getZ(), p);
                    t.setDeltaMovement((r.nextDouble() - 0.5) * 1.8, 1.1 + r.nextDouble(), (r.nextDouble() - 0.5) * 1.8);
                    t.setFuse(60);
                    w.addFreshEntity(t);
                }
                break;
            case "lightning":
                for (int i = 0; i < 20; i++) {
                    BlockPos top = w.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,
                            new BlockPos(pos.getX() + (int) ((r.nextDouble() - 0.5) * 45), 0, pos.getZ() + (int) ((r.nextDouble() - 0.5) * 45)));
                    LightningBolt b = EntityType.LIGHTNING_BOLT.create(w);
                    if (b != null) {
                        b.moveTo(Vec3.atBottomCenterOf(top));
                        w.addFreshEntity(b);
                    }
                }
                break;
            case "tsunami":
                for (int x = -20; x <= 20; x++) {
                    for (int z = -20; z <= 20; z++) {
                        for (int y = 0; y < 10; y++) {
                            BlockPos t = pos.offset(x, y, z);
                            if (w.isEmptyBlock(t)) w.setBlockAndUpdate(t, Blocks.WATER.defaultBlockState());
                        }
                    }
                }
                break;
            case "blizzard":
                for (int x = -20; x <= 20; x++) {
                    for (int z = -20; z <= 20; z++) {
                        BlockPos top = w.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos.offset(x, 0, z));
                        if (w.isEmptyBlock(top)) w.setBlockAndUpdate(top, Blocks.SNOW.defaultBlockState());
                        if (w.getBlockState(top.below()).is(Blocks.WATER)) w.setBlockAndUpdate(top.below(), Blocks.ICE.defaultBlockState());
                    }
                }
                for (LivingEntity e : w.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(20))) {
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 3));
                }
                break;
            case "blackhole":
                bPos = new Vec3(pos.getX() + 10, pos.getY() + 8, pos.getZ() + 10);
                bTick = 300;
                break;
            case "acidrain":
                for (int x = -18; x <= 18; x++) {
                    for (int z = -18; z <= 18; z++) {
                        if (r.nextInt(3) == 0) {
                            BlockPos top = w.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos.offset(x, 0, z)).below();
                            if (!w.getBlockState(top).is(Blocks.BEDROCK)) w.removeBlock(top, false);
                        }
                    }
                }
                for (LivingEntity e : w.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(20))) {
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 2));
                }
                break;
            case "nuke":
                w.explode(p, pos.getX() + 12, pos.getY(), pos.getZ() + 12, 22.0F, true, Level.ExplosionInteraction.TNT);
                break;
            case "sandstorm":
                for (int i = 0; i < 80; i++) {
                    FallingBlockEntity.fall(w,
                            BlockPos.containing(pos.getX() + (r.nextDouble() - 0.5) * 30, pos.getY() + 15 + r.nextInt(10), pos.getZ() + (r.nextDouble() - 0.5) * 30),
                            Blocks.SAND.defaultBlockState());
                }
                for (LivingEntity e : w.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(20))) {
                    e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 1));
                }
                break;
            case "wildfire":
                for (int x = -18; x <= 18; x++) {
                    for (int z = -18; z <= 18; z++) {
                        if (r.nextInt(3) == 0) {
                            BlockPos top = w.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos.offset(x, 0, z));
                            if (w.isEmptyBlock(top)) w.setBlockAndUpdate(top, Blocks.FIRE.defaultBlockState());
                        }
                    }
                }
                break;
        }
    }

    public static void onTick(ServerLevel w) {
        RandomSource r = w.random;

        if (tTick > 0 && tPos != null) {
            tTick--;
            Vec3 d = new Vec3(r.nextDouble() - 0.5, 0, r.nextDouble() - 0.5).normalize();
            tPos = tPos.add(d.x * 0.35, 0, d.z * 0.35);
            BlockPos c = BlockPos.containing(tPos);
            for (int y = 0; y < 22; y += 2) {
                double rad = 1.5 + y * 0.35, ang = tTick * 0.6 + y * 0.5;
                w.sendParticles(ParticleTypes.CLOUD, tPos.x + Math.cos(ang) * rad, tPos.y + y, tPos.z + Math.sin(ang) * rad, 4, 0.4, 0.2, 0.4, 0.02);
            }
            if (tTick % 15 == 0) {
                w.playSound(null, c, SoundEvents.ENDER_DRAGON_FLAP, SoundSource.WEATHER, 3.0F, 0.5F);
            }
            for (int y = 0; y < 20; y++) {
                int rad = 2 + y / 4;
                for (int x = -rad; x <= rad; x++) {
                    for (int z = -rad; z <= rad; z++) {
                        if (r.nextInt(10) == 0) {
                            BlockPos bp = c.offset(x, y, z);
                            if (!w.isEmptyBlock(bp) && !w.getBlockState(bp).is(Blocks.BEDROCK)) w.removeBlock(bp, false);
                        }
                    }
                }
            }
            for (Entity en : w.getEntities(null, new AABB(c).inflate(10, 24, 10))) {
                en.setDeltaMovement(en.getDeltaMovement().x + (r.nextDouble() - 0.5) * 1.1, 0.75, en.getDeltaMovement().z + (r.nextDouble() - 0.5) * 1.1);
                en.hurtMarked = true;
            }
        }

        if (bTick > 0 && bPos != null) {
            bTick--;
            BlockPos c = BlockPos.containing(bPos);
            w.sendParticles(ParticleTypes.PORTAL, bPos.x, bPos.y, bPos.z, 40, 2.5, 2.5, 2.5, 0.5);
            for (int i = 0; i < 12; i++) {
                BlockPos bp = c.offset(r.nextInt(17) - 8, r.nextInt(17) - 8, r.nextInt(17) - 8);
                if (!w.isEmptyBlock(bp) && !w.getBlockState(bp).is(Blocks.BEDROCK)) w.removeBlock(bp, false);
            }
            for (Entity en : w.getEntities(null, new AABB(c).inflate(18))) {
                en.setDeltaMovement(bPos.subtract(en.position()).normalize().scale(0.5));
                en.hurtMarked = true;
            }
        }
    }
}
