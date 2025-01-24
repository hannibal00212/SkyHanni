package at.hannibal2.skyhanni.features.misc.compacttablist

class TabColumn(val columnTitle: String) {

    val lines = mutableListOf<String>()
    val sections = mutableListOf<TabSection>()

    fun addLine(line: String) {
        lines.add(line)
    }

    fun addSection(section: TabSection) {
        sections.add(section)
    }

    fun size() = lines.size + 1
}
