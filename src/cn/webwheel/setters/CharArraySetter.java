/*
 * Copyright 2017 XueSong Guo.
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

public class CharArraySetter extends AbstractSetter<char[]> {
    @Override
    protected char[] get(Object param) {
        if (param instanceof String[]) {
            String[] ss = (String[]) param;
            char[] ds = new char[ss.length];
            for (int i = 0; i < ds.length; i++) {
                if (ss[i].length() == 1) {
                    ds[i] = ss[i].charAt(0);
                }
            }
            return ds;
        }
        return null;
    }
}
