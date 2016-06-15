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

import cn.webwheel.FileEx;

import java.io.File;

public class FileArraySetter extends AbstractSetter<File[]> {

    @Override
    protected File[] get(Object param) {
        if (param instanceof FileEx[]) {
            FileEx[] fileExs = (FileEx[]) param;
            File[] files = new File[fileExs.length];
            for (int i = 0; i < files.length; i++) {
                files[i] = fileExs[i].getFile();
            }
            return files;
        }
        return null;
    }
}
