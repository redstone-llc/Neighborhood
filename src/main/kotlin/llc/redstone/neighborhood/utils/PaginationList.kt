package llc.redstone.neighborhood.utils

import java.util.ArrayList
import kotlin.math.ceil
import kotlin.math.min

class PaginationList<T> : ArrayList<T> {
    var elementsPerPage: Int

    constructor(elements: Iterable<T>, elementsPerPage: Int) : super() {
        this.elementsPerPage = elementsPerPage
        addAll(elements)
    }

    constructor(elementsPerPage: Int) : super() {
        this.elementsPerPage = elementsPerPage
    }

    constructor(elementsPerPage: Int, vararg elements: T) : super() {
        this.elementsPerPage = elementsPerPage
        addAll(elements.asList())
    }

    fun getPageCount(): Int =
        ceil(size.toDouble() / elementsPerPage.toDouble()).toInt()

    fun getPage(page: Int): List<T>? {
        if (page < 1 || page > getPageCount()) return null
        val startIndex = (page - 1) * elementsPerPage
        val endIndex = min(startIndex + elementsPerPage, size)
        return subList(startIndex, endIndex)
    }

    fun getPages(): List<List<T>?> =
        (1..getPageCount()).map { getPage(it) }

    fun addAllArray(items: Array<T>) {
        for (item in items) add(item)
    }

    override fun toString(): String {
        val res = StringBuilder()
        for (i in 1..getPageCount()) {
            res.append("Page ").append(i).append(": ").append("\n")
            for (element in getPage(i)!!) {
                res.append(" - ").append(element).append("\n")
            }
        }
        return res.toString()
    }
}