package com.example.utility.mixin;

import com.example.utility.feature.Esp;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Makes the renderer treat ESP targets as glowing (setGlowingTag has no effect on the client). */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    private void utility$espGlow(CallbackInfoReturnable<Boolean> cir) {
        if (Esp.shouldGlow((Entity) (Object) this)) {
            cir.setReturnValue(true);
        }
    }
}
