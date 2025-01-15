package net.integr

import net.integr.processor.annotation.Serialize
import net.integr.serialization.Konvert
import net.integr.serialization.builder.DataBuilder
import net.integr.serialization.data.DataStructure

fun main() {
    val builtDataStructure = DataBuilder.make {
        value("name", "John")
        value("age", 30)
        obj("extra") {
            value("new", true)
            arr("pets") {
                value {
                    value("name", "dog")
                    value("age", 5)
                }
                value {
                    value("name", "cat")
                    value("age", 3)
                }
            }
        }
    }

    val builtDummy: Dummy = Konvert.deserializeInto<Dummy>(builtDataStructure)
    val serializedDataStructure: DataStructure = Konvert.serializeFrom(builtDummy)

    println(serializedDataStructure.getPath("extra.pets.1.age").asInt())
}

@Serialize data class Dummy(val name: String, val age: Int, val extra: Extra)
@Serialize data class Extra(val new: Boolean, val pets: List<Pet>)
@Serialize data class Pet(val name: String, val age: Int)
