package mikay.teehm.mixin.client;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientConnection.class)
public interface ClientConnectionInvoker {

	@Invoker("sendImmediately")
	void sendImmediatelyInvoker(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush);
}
