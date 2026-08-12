package me.ikurakorogaru.ikuraqol.client

import net.minecraft.client.KeyMapping

object GlobalData{
    object HoverKey{
        val pushKeys: MutableList<KeyMapping> = mutableListOf()
        var toggle: Boolean = false
    }
    object AutoKey{
        val clickKeys: MutableList<KeyMapping> = mutableListOf()
        var toggle: Boolean = false
        var delayTick: Int = 0
        var tickCount: Int = 0
    }
}