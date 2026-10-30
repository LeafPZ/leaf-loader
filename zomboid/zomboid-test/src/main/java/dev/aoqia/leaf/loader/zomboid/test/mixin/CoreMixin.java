/*
 * Copyright (C) 2026 aoqia
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <https://www.gnu.org/licenses/>.
 *
 * This license and file are additionally subjected to the exceptions described in LICENSE_EXCEPTION.md.
 *
 * The exceptions permit independent software to use this project as a dependency under terms of its own choosing, but
 * does not permit the copying or incorporation of covered source code into another work outside the terms of the AGPL.
 */

package dev.aoqia.leaf.loader.zomboid.test.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import zombie.core.Core;
import zombie.debug.DebugLog;
import zombie.iso.IsoPuddles;

@Mixin(Core.class)
public class CoreMixin {
    @Inject(method = "getVersion", at = @At("HEAD"), cancellable = true)
    private void getVersion(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue("43.0.0");
        cir.cancel();
    }

    @ModifyExpressionValue(
        method = "initShaders",
        at = @At(
            value = "INVOKE",
            target = "Lzombie/iso/IsoPuddles;getInstance()Lzombie/iso/IsoPuddles;"
        )
    )
    private IsoPuddles initShaders(IsoPuddles instance) {
        DebugLog.log("IsoPuddles::initShaders instance effect name: " + instance.effect.getName());
        return instance;
    }
}
