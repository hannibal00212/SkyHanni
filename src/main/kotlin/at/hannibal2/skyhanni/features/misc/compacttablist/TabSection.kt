package at.hannibal2.skyhanni.features.misc.compacttablist

class TabSection(val columnValue: TabColumn) {

    val lines = mutableListOf<String>()

    fun addLine(line: String) {
        lines.add(line)
    }

    fun size() = lines.size
}
