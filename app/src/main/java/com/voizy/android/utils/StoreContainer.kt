package com.voizy.android.utils

import android.content.Context
import android.content.SharedPreferences

class BooleanStoreItem(
    cacheKey: String,
    valueKey: String,
    context: Context,
    private val defaultValue: Boolean = false
) :
    StoreItem<Boolean>(cacheKey, valueKey, context) {

    var value: Boolean
        get() = container.getValue(defaultValue)
        set(value) {
            container.setValue(value)
        }
}

class IntStoreItem(
    cacheKey: String,
    valueKey: String,
    context: Context,
    private val defaultValue: Int = -1
) :
    StoreItem<Int>(cacheKey, valueKey, context) {

    var value: Int
        get() = container.getValue(defaultValue)
        set(value) {
            container.setValue(value)
        }
}

class StringStoreItem(
    cacheKey: String,
    valueKey: String,
    context: Context,
    private val defaultValue: String = ""
) :
    StoreItem<String>(cacheKey, valueKey, context) {

    var value: String
        get() = container.getValue(defaultValue)
        set(value) {
            container.setValue(value)
        }
}

class StringSetStoreItem(
    cacheKey: String,
    valueKey: String,
    context: Context,
    private val defaultValue: Set<String> = emptySet()
) :
    StoreItem<Set<String>>(cacheKey, valueKey, context) {

    var value: Set<String>
        get() = container.getValue(defaultValue)
        set(value) {
            container.setValue(value)
        }

    fun remove() {
        container.remove()
    }
}

class LongStoreItem(
    cacheKey: String,
    valueKey: String,
    context: Context,
    private val defaultValue: Long = -1
) :
    StoreItem<Long>(cacheKey, valueKey, context) {
    var value: Long
        get() = container.getValue(defaultValue)
        set(value) {
            container.setValue(value)
        }
}

abstract class StoreItem<T>(cacheKey: String, valueKey: String, private val context: Context) {
    val container = StoreContainer<T>(cacheKey, valueKey)

    fun StoreContainer<Boolean>.getValue(defaultValue: Boolean = false): Boolean {
        return getSharedPreferences(this.cacheKey).getBoolean(this.valueKey, defaultValue)
    }

    fun StoreContainer<Boolean>.setValue(value: Boolean) {
        val editor = getEditor(this.cacheKey)
        editor.putBoolean(this.valueKey, value)
        editor.apply()
    }

    fun StoreContainer<Long>.getValue(defaultValue: Long = -1L): Long {
        return getSharedPreferences(this.cacheKey).getLong(this.valueKey, defaultValue)
    }

    fun StoreContainer<Long>.setValue(value: Long) {
        val editor = getEditor(this.cacheKey)
        editor.putLong(this.valueKey, value)
        editor.apply()
    }

    fun StoreContainer<Int>.getValue(defaultValue: Int): Int {
        return getSharedPreferences(this.cacheKey).getInt(this.valueKey, defaultValue)
    }

    fun StoreContainer<Int>.setValue(value: Int) {
        val editor = getEditor(this.cacheKey)
        editor.putInt(this.valueKey, value)
        editor.apply()
    }

    fun StoreContainer<String>.getValue(defaultValue: String): String {
        return getSharedPreferences(this.cacheKey).getString(this.valueKey, defaultValue)
            ?: defaultValue
    }

    fun StoreContainer<Set<String>>.setValue(value: Set<String>) {
        val editor = getEditor(this.cacheKey)
        editor.putStringSet(this.valueKey, value)
        editor.apply()
    }

    fun StoreContainer<Set<String>>.getValue(defaultValue: Set<String>): Set<String> {
        return getSharedPreferences(this.cacheKey).getStringSet(this.valueKey, defaultValue)
            ?: defaultValue
    }

    fun StoreContainer<String>.setValue(value: String) {
        val editor = getEditor(this.cacheKey)
        editor.putString(this.valueKey, value)
        editor.apply()
    }

    fun StoreContainer<Set<String>>.remove() {
        val editor = getEditor(this.cacheKey)
        editor.remove(this.valueKey)
        editor.commit()
    }

    private fun getEditor(cacheName: String): SharedPreferences.Editor {
        return getSharedPreferences(cacheName).edit()
    }

    private fun getSharedPreferences(cacheName: String): SharedPreferences {
        return context.getSharedPreferences(cacheName, android.content.Context.MODE_PRIVATE)
    }
}

class StoreContainer<T>(val cacheKey: String, val valueKey: String)