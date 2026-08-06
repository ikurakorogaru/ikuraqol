package me.ikurakorogaru.ikuraqol.client.feature

import me.ikurakorogaru.ikuraqol.client.mainCommand
import me.ikurakorogaru.ikuraqol.access.TextInputManagerAccessor
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.minecraft.client.Minecraft

object LegacyIME {
    init {
        var toggled = false
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                mainCommand.then(
                    ClientCommands.literal("legacyime")
                        .then(
                            ClientCommands.literal("on")
                                .executes { context ->
                                    (Minecraft.getInstance().textInputManager() as TextInputManagerAccessor).`ikuraqol$setKeepTextInputEnabled`(true)
                                    1
                                }
                        ).then(
                            ClientCommands.literal("off")
                                .executes { context ->
                                    (Minecraft.getInstance().textInputManager() as TextInputManagerAccessor).`ikuraqol$setKeepTextInputEnabled`(true)
                                    1
                                }
                        )
                )
            )
        }
    }
}