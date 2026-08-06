package me.ikurakorogaru.ikuraqol.mixin;

import com.mojang.blaze3d.platform.TextInputManager;
import me.ikurakorogaru.ikuraqol.access.TextInputManagerAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextInputManager.class)
public abstract class TextInputManagerMixin implements TextInputManagerAccessor {
    @Unique
    private boolean ikuraqol$keepTextInputEnabled;

    @Unique
    @Override
    public void ikuraqol$setKeepTextInputEnabled(boolean enabled) {
        this.ikuraqol$keepTextInputEnabled = enabled;
    }

    @Unique
    @Override
    public boolean ikuraqol$isKeepTextInputEnabled() {
        return this.ikuraqol$keepTextInputEnabled;
    }

    @Inject(
            method = "stopTextInput",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ikuraqol$keepTextInputEnabled(CallbackInfo ci) {
        if (this.ikuraqol$keepTextInputEnabled) {
            ci.cancel();
        }
    }
}