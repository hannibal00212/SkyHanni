package at.hannibal2.skyhanni.utils.renderables

import at.hannibal2.skyhanni.data.model.TextInput
import at.hannibal2.skyhanni.utils.StringUtils.removeColor

class Searchable(val renderable: Renderable, val string: String?)

fun Renderable.toSearchable(searchText: String? = null) = Searchable(this, searchText?.removeColor())
fun Searchable.toRenderable() = renderable
fun List<Searchable>.toRenderable() = map { it.toRenderable() }
fun List<Searchable>.toMap() = associate { it.renderable to it.string }
val searchPrefix = "§eSearch: §7"
fun List<Searchable>.buildSearchBox(): Renderable {
    val textInput = TextInput()
    val key = 0
    return Renderable.searchBox(
        Renderable.verticalSearchableContainer(toMap(), textInput = textInput, key = key + 1),
        searchPrefix,
        onUpdateSize = { println("onUpdateSize") },
        textInput = textInput,
        key = key,
    )
}

fun List<Searchable>.buildSearchableScrollable(
    height: Int,
    textInput: TextInput,
    scrollValue: ScrollValue = ScrollValue(),
    velocity: Double = 2.0,
): Renderable {
    val key = 0
    return Renderable.searchBox(
        Renderable.searchableScrollList(
            toMap(),
            textInput = textInput,
            key = key + 1,
            height = height,
            scrollValue = scrollValue,
            velocity = velocity,
        ),
        searchPrefix,
        onUpdateSize = { println("onUpdateSize") },
        textInput = textInput,
        key = key,
    )
}

fun Map<List<Renderable>, String?>.buildSearchableTable(): Renderable {
    val textInput = TextInput()
    val key = 0
    return Renderable.searchBox(
        Renderable.searchableTable(toMap(), textInput = textInput, key = key + 1),
        searchPrefix,
        onUpdateSize = { println("onUpdateSize") },
        textInput = textInput,
        key = key,
    )
}
