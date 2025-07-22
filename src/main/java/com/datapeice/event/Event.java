package com.datapeice.event;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;


public class Event implements ModInitializer {
	public static final String MOD_ID = "eventik";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("SL:\nEventik loaded!\n by datapeice & LendSpele :3");
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof HostileEntity mob) {
				enhanceMobArmor(mob);
				enhanceMobEffect(mob);
			}
			if (entity instanceof CreeperEntity creeper) {
				Random random = new Random();
				if (!creeper.isCharged() && (random.nextInt(0, 99) < 5)) { // 5% шанс
					chargeCreeper(creeper); // Заряжаем крипера
					creeper.setFireTicks(0); // Снимаем огонь с крипера
				}
			}
		});
		ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> handler.getPlayer().setSilent(true));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> handler.getPlayer().setSilent(true));

	}

	private void enhanceMobEffect(HostileEntity mob) {
		Random rand = new Random();
		if (rand.nextInt(0, 9) < 2){
			mob.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 600, 1, false, true));
		}
		if (rand.nextInt(0, 9) < 2){
			mob.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 600, 2, false, true));
		}
		if (rand.nextInt(0, 9) < 2){
			mob.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 600, 2, false, true));
		}
		if (rand.nextInt(0, 9) < 2){
			mob.addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 600, 2, false, true));
		}
		if (rand.nextInt(0, 9) < 2){
			mob.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 600, 1, false, true));
		}
	}

	private void onServerTick(MinecraftServer server) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			boolean hasCompassOrMap =
					player.getMainHandStack().isOf(Items.COMPASS) ||
							player.getOffHandStack().isOf(Items.COMPASS) ||
							player.getMainHandStack().isOf(Items.FILLED_MAP) ||
							player.getOffHandStack().isOf(Items.FILLED_MAP);

			updateDebugInfo(player, hasCompassOrMap);
			updatePlayerNames(server, player);
		}

		clearTabList(server);
	}

	private void updateDebugInfo(ServerPlayerEntity player, boolean hasCompassOrMap) {
		// boolean currentState = player.getServerWorld().getGameRules().getBoolean(GameRules.REDUCED_DEBUG_INFO);

		if (hasCompassOrMap) {
			BlockPos playerPOS = player.getBlockPos();

			int[] pos = RandomNumber.getRandomPos();


            Text compass = Text.empty()
					.append(Text.literal("X: ").setStyle(Style.EMPTY.withColor(Formatting.RED)))
					.append(Text.literal(String.valueOf(pos[1])).setStyle(Style.EMPTY.withColor(Formatting.RED)))

 					.append(Text.literal(" Y: ").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
				.append(Text.literal(String.valueOf(playerPOS.getY())).setStyle(Style.EMPTY.withColor(Formatting.GREEN)))

 					.append(Text.literal(" Z: ").setStyle(Style.EMPTY.withColor(Formatting.AQUA)))
					.append(Text.literal(String.valueOf(pos[2])).setStyle(Style.EMPTY.withColor(Formatting.AQUA)));

			player.sendMessage(compass, true);

		}
//		else {                         ТЕКСТА НЕТ КОГДА В РУКЕ НЕТ КОМАСА/КАРТЫ
//				Text noCompass = Text.literal("X: ")
//						.setStyle(Style.EMPTY.withColor(Formatting.RED))
//						.append(Text.literal("-").setStyle(Style.EMPTY.withColor(Formatting.RED)))
//						.append(Text.literal(" Y: ")
//								.setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
//						.append(Text.literal("-").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
//						.append(Text.literal(" Z: ")
//								.setStyle(Style.EMPTY.withColor(Formatting.AQUA)))
//						.append(Text.literal("-").setStyle(Style.EMPTY.withColor(Formatting.AQUA)));
//
//				player.sendMessage(noCompass, true);
//			}
		}

	private void updatePlayerNames(MinecraftServer server, ServerPlayerEntity player) {
		boolean shouldShowNames = false;

		for (ServerPlayerEntity other : server.getPlayerManager().getPlayerList()) {
			if (player != other && player.squaredDistanceTo(other) <= 2) { // 2 метра = 4 блока
				shouldShowNames = true;
				break;
			}
		}

		ScoreboardObjective objective = shouldShowNames ? null : player.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.LIST);
		player.getScoreboard().setObjectiveSlot(ScoreboardDisplaySlot.LIST, objective);
	}

	private void clearTabList(MinecraftServer server) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			player.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player));
		}
	}

	private void enhanceMobArmor(HostileEntity mob) {
		if (mob.getRandom().nextFloat() < 0.7f) {
			net.minecraft.util.math.random.Random random = mob.getRandom();  // Use Minecraft's Random

			// Список возможных материалов брони
			Item[] armorMaterials = {
					Items.IRON_HELMET, Items.DIAMOND_HELMET, Items.GOLDEN_HELMET,
					Items.IRON_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.GOLDEN_CHESTPLATE,
					Items.IRON_LEGGINGS, Items.DIAMOND_LEGGINGS, Items.GOLDEN_LEGGINGS,
					Items.IRON_BOOTS, Items.DIAMOND_BOOTS, Items.GOLDEN_BOOTS,
			};

			// Устанавливаем броню для каждого слота с случайным материалом


			if (random.nextBoolean()) mob.equipStack(EquipmentSlot.HEAD, new ItemStack(armorMaterials[random.nextInt(3)]));  // 0-2 индексы для шлема
			if (random.nextBoolean()) mob.equipStack(EquipmentSlot.CHEST, new ItemStack(armorMaterials[random.nextInt(3) + 3]));  // 3-5 индексы для нагрудников
			if (random.nextBoolean()) mob.equipStack(EquipmentSlot.LEGS, new ItemStack(armorMaterials[random.nextInt(3) + 6]));  // 6-8 индексы для поножей
			if (random.nextBoolean()) mob.equipStack(EquipmentSlot.FEET, new ItemStack(armorMaterials[random.nextInt(3) + 9]));  // 9-11 индексы для ботинок

			// Устанавливаем шанс выпадения брони
			mob.setEquipmentDropChance(EquipmentSlot.HEAD, 0.03f);
			mob.setEquipmentDropChance(EquipmentSlot.CHEST, 0.01f);
			mob.setEquipmentDropChance(EquipmentSlot.LEGS, 0.01f);
			mob.setEquipmentDropChance(EquipmentSlot.FEET, 0.05f);
		}
	}
	private void chargeCreeper(CreeperEntity creeper) {
		if (creeper != null) {
			if (!creeper.getWorld().isClient) {
				ServerWorld world = (ServerWorld) creeper.getWorld();

				BlockPos pos = creeper.getBlockPos();

				LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(creeper.getWorld(), SpawnReason.TRIGGERED);

				lightning.refreshPositionAfterTeleport(pos.getX(), pos.getY(), pos.getZ());

				world.spawnEntity(lightning);
				creeper.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 200, 3, false, false));
				for (int i = 0; i < 100; i++) {  // Количество частиц
					double offsetX = (Math.random() - 0.5) * 2;  // Случайное смещение по оси X
					double offsetY = Math.random() * 1.5;  // Случайное смещение по оси Y
					double offsetZ = (Math.random() - 0.5) * 2;  // Случайное смещение по оси Z

					world.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
							creeper.getX() + offsetX,
							creeper.getY() + offsetY,
							creeper.getZ() + offsetZ,
							0.0, 0.0, 0.0
					);
				}
			}
		}

	}


}


