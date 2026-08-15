package me.ikurakorogaru.ikuraqol.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.ikurakorogaru.ikuraqol.access.NoPitchLimitAccess;
import net.minecraft.world.entity.Entity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class NoPitchLimitMixin implements NoPitchLimitAccess {

    @Unique
    private boolean ikuraqol$noPitchLimit = false;

    @Override
    public void ikuraqol$setNoPitchLimit(boolean enabled) {
        this.ikuraqol$noPitchLimit = enabled;
    }

    @Override
    public boolean ikuraqol$getNoPitchLimit() {
        return this.ikuraqol$noPitchLimit;
    }

    @WrapOperation(
            method = {"turn", "absSnapRotationTo"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;clamp(FFF)F"
            )
    )
    private float ikuraqol$removePitchClamp(
            float value,
            float min,
            float max,
            Operation<Float> original
    ) {
        if (this.ikuraqol$noPitchLimit) {
            return value;
        }

        return original.call(value, min, max);
    }

    @WrapOperation(
            method = "setXRot",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/Entity;xRot:F",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void ikuraqol$removeSetXRotClamp(
            Entity instance,
            float clamped,
            Operation<Void> original,
            @Local(argsOnly = true, name = "xRot") float xRot
    ) {
        original.call(
                instance,
                this.ikuraqol$noPitchLimit ? xRot : clamped
        );
    }
}