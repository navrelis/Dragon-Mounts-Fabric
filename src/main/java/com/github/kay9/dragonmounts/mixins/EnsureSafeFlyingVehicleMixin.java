package com.github.kay9.dragonmounts.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.animal.FlyingAnimal;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public class EnsureSafeFlyingVehicleMixin
{
    /**
     * Purpose: to ensure players who ride flight-capable vehicles don't get kicked for flying
     * without needing to compromise server security by disabling "Kick for flying"
     * <br>
     * Fabric port: wraps the field assignment (MixinExtras) instead of redirecting it, so other mods can coexist.
     */
    @WrapOperation(method = "handleMoveVehicle(Lnet/minecraft/network/protocol/game/ServerboundMoveVehiclePacket;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;clientVehicleIsFloating:Z", opcode = Opcodes.PUTFIELD))
    private void dragonmounts_ensureSafeFlyingVehicle(ServerGamePacketListenerImpl instance, boolean flag, Operation<Void> original)
    {
        original.call(instance, flag && !(instance.getPlayer().getRootVehicle() instanceof FlyingAnimal a && a.isFlying()));
    }
}
