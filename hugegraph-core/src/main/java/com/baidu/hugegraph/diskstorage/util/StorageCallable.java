// Copyright 2017 HugeGraph Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.baidu.hugegraph.diskstorage.util;

import java.util.concurrent.Callable;

import com.baidu.hugegraph.diskstorage.BackendException;

/**
 * Like {@link Callable}, except the exception type is narrowed from
 * {@link Exception} to {@link com.baidu.hugegraph.diskstorage.BackendException}.
 * 
 * @param <T>
 *            call return type
 */
public interface StorageCallable<T> extends Callable<T> {

    @Override
    public T call() throws BackendException;
}
