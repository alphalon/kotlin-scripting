/*
 * Project: Kotlin Scripting
 * Created: Nov 29, 2018
 *
 * Copyright (c) 2018 Alphalon, LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused")

package io.alphalon.kotlin.scripting

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.stream.Stream

/**
 * Preforms an [action] for each element of the collection.
 *
 * This operation blocks until all of the elements have been processed.
 *
 * @param action The closure to call once per file, order is undefined
 */
fun <T> Iterable<T>.forEachAsync(action: (T) -> Unit) {
    runBlocking(Dispatchers.IO) {
        coroutineScope {
            val channel = Channel<T>()

            // Producer
            launch {
                this@forEachAsync.forEach { channel.send(it) }

                channel.close()
            }

            // Consumers
            repeat(Runtime.getRuntime().availableProcessors()) {
                launch {
                    for (item in channel)
                        action(item)
                }
            }
        }
    }
}

/**
 * Performs an [action] for each element in the sequence.
 *
 * This operation blocks until all of the elements have been processed.
 *
 * @param action The closure to call once per file, order is undefined
 */
fun <T> Sequence<T>.forEachAsync(action: (T) -> Unit) {
    runBlocking(Dispatchers.IO) {
        coroutineScope {
            val channel = Channel<T>()

            // Producer
            launch {
                this@forEachAsync.forEach { channel.send(it) }

                channel.close()
            }

            // Consumers
            repeat(Runtime.getRuntime().availableProcessors()) {
                launch {
                    for (item in channel)
                        action(item)
                }
            }
        }
    }
}

/**
 * Performs an [action] for each element in the stream.
 *
 * This operation blocks until all of the elements have been processed.
 *
 * @param action The closure to call once per file, order is undefined
 */
fun <T> Stream<T>.forEachAsync(action: (T) -> Unit) {
    parallel().forEach(action)
}
