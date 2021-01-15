package com.github.cesar1287.revisaofirebase

import android.text.Editable

fun String.getEditable(): Editable {
    return Editable.Factory.getInstance().newEditable(this)
}