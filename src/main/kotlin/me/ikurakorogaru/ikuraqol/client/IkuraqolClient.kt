package me.ikurakorogaru.ikuraqol.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands

val mainCommand = ClientCommands.literal("ikuraqol")

val featurePaths: MutableList<String> = mutableListOf(
    "me.ikurakorogaru.ikuraqol.client.feature.HoverKey",
    "me.ikurakorogaru.ikuraqol.client.feature.RunKotlin",
    "me.ikurakorogaru.ikuraqol.client.feature.FogClear",
    "me.ikurakorogaru.ikuraqol.client.feature.AutoKey",
    "me.ikurakorogaru.ikuraqol.client.feature.LegacyIME",
    "me.ikurakorogaru.ikuraqol.client.feature.NoPitchLimit"
)

class IkuraQolClient : ClientModInitializer {
    override fun onInitializeClient() {
        featurePaths.forEach {
            try {
                Class.forName(it)
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
        }

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(mainCommand)

        }

    }
}