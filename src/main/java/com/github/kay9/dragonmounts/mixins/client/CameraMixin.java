package com.github.kay9.dragonmounts.mixins.client;

import com.github.kay9.dragonmounts.client.MountCameraManager;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric port: replaces NeoForge's {@code ViewportEvent.ComputeCameraAngles}.
 * A plain TAIL injection is used (no redirects/overwrites) so it stacks safely with other mods hooking {@link Camera#setup}.
 */
@Mixin(Camera.class)
public abstract class CameraMixin
{
    @Inject(method = "setup", at = @At("TAIL"))
    private void dragonmounts_setMountCameraAngles(CallbackInfo ci)
    {
        MountCameraManager.setMountCameraAngles((Camera) (Object) this);
    }
}
