package com.github.cesar1287.revisaofirebase

import androidx.lifecycle.ViewModel

class TestViewModel : ViewModel() {

    fun helloWorld(name: String = "Novo Mundo"): String {
        return "Olá $name!"
    }

    fun changeClassList(classList: MutableList<String>): MutableList<String> {
        val nome = "Cesar"
        classList.add(nome)
        return classList
    }

    fun changeString(value: String): String {
        return value.take(2)
    }

    fun verifyIfNull(value: String? = null): String? {
        return value?.let {
            it
        }
    }
}