package commands

import world.cepi.kstom.command.kommand.Kommand

object ToggleDitherCommand : Kommand({

    onlyPlayers

    default {
        Main.dither = !Main.dither
        sender.sendMessage("${if (Main.dither) "Enabled" else "Disabled"} dithering")
    }

}, "dither")