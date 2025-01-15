package net.integr.serialization.obj.custom

import net.integr.serialization.data.DataStructure

interface Constructable<T> {
    fun construct(data: DataStructure): T
}