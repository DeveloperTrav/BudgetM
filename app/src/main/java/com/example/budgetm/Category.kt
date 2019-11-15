package com.example.budgetm

import java.io.Serializable

class Category : Serializable {
    var id: String? = null
    var name: String? = null
    var total = 0.0
    var itemIds = arrayListOf<String>()

    //For read data from FireBase
    constructor() {}

    constructor(id: String, name: String) {
        this.id = id
        this.name = name
    }
}