package com.example.budgetm

import java.io.Serializable

class Item : Serializable {
    var id: String? = null
    var name: String? = null
    var description: String? = null
    var cost: Double? = null
    var category: Category? = null

    //For read data from FireBase
    constructor() {}

    constructor(id: String, name: String, description: String, cost: Double, category: Category) {
        this.id = id
        this.name = name
        this.description = description
        this.cost = cost
        this.category = category
    }
}