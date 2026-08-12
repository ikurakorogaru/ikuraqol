package me.ikurakorogaru.ikuraqol.client.feature

import me.ikurakorogaru.ikuraqol.client.GlobalData
import com.mojang.brigadier.arguments.StringArgumentType
import me.ikurakorogaru.ikuraqol.client.mainCommand
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import kotlin.collections.forEach


object HoverKey {
    private val data = GlobalData.HoverKey

    init {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                mainCommand.then(
                    ClientCommands.literal("hoverkey")
                        .then(
                            ClientCommands.literal("keys")
                                .then(
                                    ClientCommands.literal("add")
                                        .then(
                                            ClientCommands.argument("keyId", StringArgumentType.word())
                                                .suggests { _, builder ->
                                                    SharedSuggestionProvider.suggest(
                                                        Minecraft.getInstance()
                                                            .options
                                                            .keyMappings
                                                            .map { it.name },
                                                        builder
                                                    )
                                                }
                                                .executes { context ->
                                                    val keyId = StringArgumentType.getString(context, "keyId")

                                                    val keyMapping = Minecraft.getInstance().options.keyMappings.find {
                                                        it.name == keyId
                                                    }
                                                    if (keyMapping == null) {
                                                        print("not found $keyId")
                                                    }
                                                    if (keyMapping != null) {
                                                        data.pushKeys.add(keyMapping)
                                                    }
                                                    1
                                                }
                                        )
                                ).then(
                                    ClientCommands.literal("del")
                                        .then(
                                            ClientCommands.argument("keyId", StringArgumentType.word())
                                                .suggests { _, builder ->
                                                    SharedSuggestionProvider.suggest(
                                                        data.pushKeys.map { it.name },
                                                        builder
                                                    )
                                                }.executes { context ->
                                                    val keyId = StringArgumentType.getString(context, "keyId")

                                                    val keyMapping = data.pushKeys.find {
                                                        it.name == keyId
                                                    }
                                                    if (keyMapping == null) {
                                                        print("not found $keyId")
                                                    }
                                                    if (keyMapping != null) {
                                                        data.pushKeys.remove(keyMapping)
                                                        keyMapping.setDown(true)
                                                    }
                                                    1
                                                }

                                        )
                                ).then(
                                    ClientCommands.literal("list")
                                        .executes { context ->
                                            context.source.sendFeedback(
                                                Component.literal(
                                                    data.pushKeys.joinToString(
                                                        separator = ", ",
                                                        transform = { it.name })
                                                )
                                            )
                                            1
                                        }
                                )
                        ).then(
                            ClientCommands.literal("on")
                                .executes { context ->
                                    data.toggle = true
                                    1
                                }
                        ).then(
                            ClientCommands.literal("off")
                                .executes { context ->
                                    data.toggle = false
                                    data.pushKeys.forEach { keyMapping ->
                                        keyMapping.setDown(false)
                                    }
                                    1
                                }
                        )
                )
            )
        }

        ClientTickEvents.START_CLIENT_TICK.register {
            if (data.toggle) {
                data.pushKeys.forEach { keyMapping ->
                    keyMapping.setDown(true)
                }
            }
        }
    }
}