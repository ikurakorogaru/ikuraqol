package me.ikurakorogaru.ikuraqol.client.feature

import me.ikurakorogaru.ikuraqol.access.NoPitchLimitAccess
import me.ikurakorogaru.ikuraqol.client.mainCommand
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.minecraft.client.Minecraft

object NoPitchLimit {
    init {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                mainCommand.then(
                    ClientCommands.literal("nopitchlimit").then(
                        ClientCommands.literal("on")
                            .executes { context ->
                                (Minecraft.getInstance().player as NoPitchLimitAccess).`ikuraqol$setNoPitchLimit`(true)
                                1
                            }
                    ).then(
                        ClientCommands.literal("off")
                            .executes { context ->
                                (Minecraft.getInstance().player as NoPitchLimitAccess).`ikuraqol$setNoPitchLimit`(false)
                                1
                            }
                    )
                )
            )
        }
    }
}