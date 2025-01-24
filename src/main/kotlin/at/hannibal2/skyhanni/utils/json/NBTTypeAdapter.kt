package at.hannibal2.skyhanni.utils.json

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTTagCompound
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64

object NBTTypeAdapter : TypeAdapter<NBTTagCompound>() {

    override fun write(out: JsonWriter, value: NBTTagCompound) {
        val baos = ByteArrayOutputStream()
        CompressedStreamTools.writeCompressed(value, baos)
        out.value(Base64.getEncoder().encode(baos.toByteArray()).decodeToString())
    }

    override fun read(reader: JsonReader): NBTTagCompound {
        val bais = ByteArrayInputStream(Base64.getDecoder().decode(reader.nextString()))
        return CompressedStreamTools.readCompressed(bais)
    }
}
