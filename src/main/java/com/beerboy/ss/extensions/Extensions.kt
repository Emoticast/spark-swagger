package com.emoticast.extensions

import com.google.gson.Gson
import org.eclipse.jetty.util.UrlEncoded
import kotlin.reflect.KClass

fun <T : Any?> T.print(): T = this.apply {
    val stackFrame = Thread.currentThread().stackTrace[2]
    val className = stackFrame.className
    val methodName = stackFrame.methodName
    val fileName = stackFrame.fileName
    val lineNumber = stackFrame.lineNumber
    println("$this at $className.$methodName($fileName:$lineNumber)")
}

val Any.json: String get() = Gson().toJson(this)
val String.urlEncoded: String get() = UrlEncoded.encodeString(this)
inline fun <reified T : Any> String.parseJson() :T  = Gson().fromJson(this, T::class.java)
fun <T : Any> String.parseJson(klass: KClass<T>) :T  = Gson().fromJson(this, klass.java)
