package dev.emortal.blockscreen

import net.minestom.server.command.builder.arguments.ArgumentWord
import world.cepi.kstom.command.arguments.suggest
import world.cepi.kstom.command.kommand.Kommand

object ScaleTypeCommand : Kommand({

    onlyPlayers

    val scaleTypeArgument = ArgumentWord("Scale Type")
        .suggest { listOf("DEFAULT", "FAST", "SMOOTH", "REPLICATE", "AREA_AVERAGING") }

    syntax(scaleTypeArgument) {
        val new = when (!scaleTypeArgument) {
            "DEFAULT" -> 1
            "FAST" -> 2
            "SMOOTH" -> 4
            "REPLICATE" -> 8
            "AREA_AVERAGING" -> 16
            else -> return@syntax
        }

        Main.scaleType = new
        sender.sendMessage("Set scale type to ${!scaleTypeArgument}!")
    }

}, "scaletype")