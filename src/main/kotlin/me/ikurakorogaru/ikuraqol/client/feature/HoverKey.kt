package me.ikurakorogaru.ikuraqol.client.feature

import com.mojang.brigadier.arguments.StringArgumentType
import me.ikurakorogaru.ikuraqol.client.mainCommand
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import kotlin.collections.forEach


object HoverKey {
    init {
        var pushKeys: MutableList<KeyMapping> = mutableListOf()
        var ifPush: Boolean = false

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
                                                        pushKeys.add(keyMapping)
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
                                                        pushKeys.map { it.name },
                                                        builder
                                                    )
                                                }.executes { context ->
                                                    val keyId = StringArgumentType.getString(context, "keyId")

                                                    val keyMapping = pushKeys.find {
                                                        it.name == keyId
                                                    }
                                                    if (keyMapping == null) {
                                                        print("not found $keyId")
                                                    }
                                                    if (keyMapping != null) {
                                                        pushKeys.remove(keyMapping)
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
                                                    pushKeys.joinToString(
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
                                    ifPush = true
                                    1
                                }
                        ).then(
                            ClientCommands.literal("off")
                                .executes { context ->
                                    ifPush = false
                                    pushKeys.forEach { keyMapping ->
                                        keyMapping.setDown(false)
                                    }
                                    1
                                }
                        )
                )
            )
        }

        ClientTickEvents.START_CLIENT_TICK.register {
            if (ifPush) {
                pushKeys.forEach { keyMapping ->
                    keyMapping.setDown(true)
                }
            }
        }
    }
}