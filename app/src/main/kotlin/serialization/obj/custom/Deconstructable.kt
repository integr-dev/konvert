package net.integr.serialization.obj.custom

import net.integr.serialization.data.DataStructure


interface Deconstructable<T> {
    fun deconstruct(data: T): DataStructure
}