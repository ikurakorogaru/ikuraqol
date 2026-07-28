package me.ikurakorogaru.ikuraqol.client.feature

import me.ikurakorogaru.ikuraqol.client.mainCommand
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import me.ikurakorogaru.ikuraqol.mixin.FogRendererAccessor

object FogClear {
    init {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                mainCommand.then(
                    ClientCommands.literal("fogclear").then(
                        ClientCommands.literal("on")
                            .executes { context ->
                                FogRendererAccessor.setFogEnabled(false)
                                1
                            }

                    ).then(
                        ClientCommands.literal("off")
                            .executes { context ->
                                FogRendererAccessor.setFogEnabled(true)
                                1
                            }

                    )
                )
            )
        }
    }
}