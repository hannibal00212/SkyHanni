package at.hannibal2.skyhanni.events.garden

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI

class PlotChangeEvent(val plot: GardenPlotAPI.Plot?) : SkyHanniEvent()
