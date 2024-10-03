package at.hannibal2.skyhanni.utils

import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.FMLCommonHandler
import java.awt.Desktop
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.net.URI
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.UIManager

object NEUVersionCheck {

    @JvmStatic
    fun checkIfNeuIsLoaded() {
        try {
            Class.forName("io.github.moulberry.notenoughupdates.NotEnoughUpdates")
        } catch (e: Throwable) {
            neuWarning("missing")
            return
        }

        try {
            val clazz = Class.forName("io.github.moulberry.notenoughupdates.util.ItemResolutionQuery")

            for (field in clazz.methods) {
                if (field.name == "findInternalNameByDisplayName") return
            }
        } catch (_: Throwable) {
        }
        neuWarning("outdated")
    }

    private fun neuWarning(what: String) {
        openPopupWindow(
            "NotEnoughUpdates is $what!\n" +
                "SkyHanni requires the latest version of NotEnoughUpdates to work.\n" +
                "Use these links to download the latest version:",
            Pair("Join SkyHanni Discord", "https://discord.com/invite/skyhanni-997079228510117908"),
            Pair("Open SkyHanni GitHub", "https://github.com/hannibal002/SkyHanni"),
            Pair("Join NEU Discord", "https://discord.gg/moulberry"),
            Pair("Download NEU from GitHub", "https://github.com/NotEnoughUpdates/NotEnoughUpdates/releases/latest"),
            Pair("Download NEU from Modrinth", "https://modrinth.com/mod/notenoughupdates/version/latest"),
            Pair("Open Mods Folder", File(Minecraft.getMinecraft().mcDataDir, "mods").toURI().toString()),
        )
        closeMinecraft()
    }

    /**
     * Taken and modified from Skytils
     */
    @Suppress("PrintStackTrace")
    private fun openPopupWindow(errorMessage: String, vararg options: Pair<String, String>) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        val frame = JFrame()
        frame.isUndecorated = true
        frame.isAlwaysOnTop = true
        frame.setLocationRelativeTo(null)
        frame.isVisible = true

        val buttons = mutableListOf<JButton>()
        for ((name, link) in options) {
            val button = JButton(name)
            button.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(event: MouseEvent) {
                    try {
                        Desktop.getDesktop().browse(URI(link))
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
            })
            buttons.add(button)
        }
        val close = JButton("Close")
        close.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(event: MouseEvent) {
                try {
                    closeMinecraft()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        })
        buttons.add(close)

        val allOptions = buttons.toTypedArray()
        JOptionPane.showOptionDialog(
            frame,
            errorMessage,
            "SkyHanni Error",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.ERROR_MESSAGE,
            null,
            allOptions,
            allOptions[0]
        )
    }

    fun closeMinecraft() {
        FMLCommonHandler.instance().handleExit(-1)
        FMLCommonHandler.instance().expectServerStopped()
    }
}
