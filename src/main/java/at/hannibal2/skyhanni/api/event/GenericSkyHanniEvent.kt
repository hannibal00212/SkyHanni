package at.hannibal2.skyhanni.api.event

abstract class GenericSkyHanniEvent<T>(val type: Class<T>) : SkyHanniEvent(), SkyHanniEvent.Cancellable
