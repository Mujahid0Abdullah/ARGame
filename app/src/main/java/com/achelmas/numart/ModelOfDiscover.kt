package com.achelmas.numart

class ModelOfDiscover  (
    var question: Map<String, String> = mapOf(),
    var targetNumber: String = "",
    var target: String = "",
    var shape1: String = "",
    var shape2: String = "",
    var shape3: String = "",
    var shape4: String = "",
    var isUnlocked: Boolean = false // Hedef açık mı kapalı mı
)