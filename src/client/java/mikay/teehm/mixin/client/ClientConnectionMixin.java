package mikay.teehm.mixin.client;

import mikay.teehm.TeehmClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

	@Inject(at = @At("HEAD"), method = "handlePacket")
	private static void receivePacket(Packet<?> packet, PacketListener listener, CallbackInfo info) {
		MinecraftClient client = MinecraftClient.getInstance();

		if (client.player == null) return;
		if(!(packet instanceof SetTradeOffersS2CPacket tradePacket)) return;

		TeehmClient.getInstance().latestOffers = tradePacket.getOffers();
	}
}