/*
 * Copyright 2012 XueSong Guo.
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

package cn.webwheel.setters;

public class LongArraySetter extends AbstractSetter<long[]> {
    @Override
    protected long[] get(Object param) {
        if (param instanceof String[]) {
            String[] ss = (String[]) param;
            long[] ds = new long[ss.length];
            for (int i = 0; i < ds.length; i++) {
                try {
                    ds[i] = Long.parseLong(ss[i]);
                } catch (NumberFormatException ignored) {
                }
            }
            return ds;
        }
        return null;
    }
}
