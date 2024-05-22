package mikay.teehm;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerProfession;

public class TeehmClient implements ClientModInitializer {
	static TeehmClient instance;
	public TradeOfferList latestOffers = null;
	VillagerEntity currentVillager = null;
	VillagerEntity lastVillager = null;
	Text oldName;
	boolean oldVisible;
	TeehmState state = TeehmState.IDLE;
	byte counter = 60;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
		instance = this;
	}

	private void onClientTick(MinecraftClient client) {
		if (client.world == null || client.player == null || client.isPaused()) return;

		if (currentVillager == null || client.player.distanceTo(currentVillager) > 3) {
			this.latestOffers = null;
			this.state = TeehmState.IDLE;

			float smallest = 3;
			VillagerEntity closestVillager = null;

			for (Entity entity : client.world.getEntities()) {
				if (!(entity instanceof VillagerEntity)) continue;
				VillagerEntity villager = (VillagerEntity) entity;

				if (villager.isBaby()) continue;

				float distance = client.player.distanceTo(villager);

				if (distance < smallest) {
					closestVillager = villager;

					distance = smallest;
				}
			}

			lastVillager = currentVillager;
			currentVillager = closestVillager;

			updateVillager();
		}

		if (currentVillager == null) return;

		if (this.state == TeehmState.READY) {
			PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.interact(currentVillager, client.player.isSneaking(), Hand.MAIN_HAND);

			client.getNetworkHandler().sendPacket(packet);

			this.state = TeehmState.WAITING;
		} else if (this.state == TeehmState.WAITING) {
			if (--this.counter > 0) {
				if (this.latestOffers == null) return;

				updateVillager();
				this.state = TeehmState.IDLE;
				this.counter = 60;
			} else {
				this.counter = 60;
				this.state = TeehmState.READY;
			}
		} else if (this.state == TeehmState.IDLE) {
			if (currentVillager.getVillagerData().getProfession() == VillagerProfession.NONE) {
				this.latestOffers = null;
			} else if (latestOffers == null) {
				this.state = TeehmState.READY;
			}
		}
	}

	private void updateVillager() {
		if (lastVillager != null) {
			lastVillager.setCustomName(oldName);
			lastVillager.setCustomNameVisible(oldVisible);
		}

		if (currentVillager != null) {
			oldName = currentVillager.getCustomName();
			oldVisible = currentVillager.isCustomNameVisible();

			if (latestOffers != null) {
				currentVillager.setCustomName(getEnchantment().copyContentOnly().formatted(Formatting.GREEN));
				currentVillager.setCustomNameVisible(true);
			}

		}
	}

	private Text getEnchantment() {
		ItemStack item;

		MinecraftClient.getInstance().player.sendMessage(latestOffers.get(0).getSellItem().getName());
		MinecraftClient.getInstance().player.sendMessage(Text.literal("Enchanted = " + latestOffers.get(0).getSellItem()));
		MinecraftClient.getInstance().player.sendMessage(latestOffers.get(1).getSellItem().getName());
		MinecraftClient.getInstance().player.sendMessage(Text.literal("Enchanted = " + latestOffers.get(1).getSellItem().getNbt().asString()));

		if (latestOffers.get(0).getSellItem().hasEnchantments()) {
			item = latestOffers.get(0).getSellItem();
		} else if (latestOffers.get(1).getSellItem().hasEnchantments()) {
			item = latestOffers.get(1).getSellItem();
		} else {
			return Text.literal("Dogshit");
		}

		NbtCompound compound = item.getEnchantments().getCompound(0);

		return Registries.ENCHANTMENT.get(new Identifier(compound.getString("id"))).getName(compound.getInt("lvl"));
	}

	public static TeehmClient getInstance() { return instance; }
}