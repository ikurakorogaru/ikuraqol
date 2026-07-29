package me.ikurakorogaru.ikuraqol.client.feature

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import me.ikurakorogaru.ikuraqol.client.mainCommand
import me.ikurakorogaru.ikuraqol.mixin.KeyMappingAccessor
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component

object AutoKey {
    var clickKeys: MutableList<KeyMapping> = mutableListOf()
    var ifClick: Boolean = false
    var delaytick: Int = 0
    var tickCount: Int = 0

    init {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                mainCommand.then(
                    ClientCommands.literal("autokey").then(
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
                                                    clickKeys
                                                        .add(keyMapping)
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
                                                    clickKeys
                                                        .map { it.name },
                                                    builder
                                                )
                                            }.executes { context ->
                                                val keyId = StringArgumentType.getString(context, "keyId")

                                                val keyMapping = clickKeys
                                                    .find {
                                                        it.name == keyId
                                                    }
                                                if (keyMapping == null) {
                                                    print("not found $keyId")
                                                }
                                                if (keyMapping != null) {
                                                    clickKeys
                                                        .remove(keyMapping)
                                                }
                                                1
                                            }

                                    )
                            ).then(
                                ClientCommands.literal("list")
                                    .executes { context ->
                                        context.source.sendFeedback(
                                            Component.literal(
                                                clickKeys
                                                    .joinToString(
                                                        separator = ", ",
                                                        transform = { it.name })
                                            )
                                        )
                                        1
                                    }
                            )
                    ).then(
                        ClientCommands.literal("delaytick")
                            .then(
                                ClientCommands.argument("delaytick", IntegerArgumentType.integer())
                                    .executes { context ->
                                        delaytick = IntegerArgumentType.getInteger(context, "delaytick")
                                        1
                                    }
                            )
                    ).then(
                        ClientCommands.literal("on")
                            .executes { context ->
                                ifClick = true
                                1
                            }
                    ).then(
                        ClientCommands.literal("off")
                            .executes { context ->
                                ifClick = false
                                clickKeys
                                    .forEach { keyMapping ->
                                        keyMapping.setDown(false)
                                    }
                                1
                            }
                    )
                )
            )
        }
        ClientTickEvents.START_CLIENT_TICK.register {
            tickCount++
            if (tickCount >= delaytick) {
                tickCount = 0
                clickKeys.forEach {keyMapping ->
                    val key = (keyMapping as KeyMappingAccessor).`ikuraqol$getKey`()
                    KeyMapping.click(key)
                }
            }
        }
    }
}