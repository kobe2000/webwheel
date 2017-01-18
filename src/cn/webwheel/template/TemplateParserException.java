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

package cn.webwheel.template;

public class TemplateParserException extends RuntimeException {

    public int location;
    public String msg;
    public String line;

    public TemplateParserException(String string, int location, String msg) {
        this.location = location;
        this.msg = msg;

        if (string.isEmpty()) {
            line = string;
            return;
        }

        if (location < 0) location = 0;
        else if (location >= string.length()) location = string.length() - 1;

        int begin = location;
        int end = location + 1;
        for (; begin > 0; begin--) {
            char c = string.charAt(begin);
            if (c == '\r' || c == '\n') {
                begin++;
                break;
            }
        }
        for (; end < string.length(); end++) {
            if (end >= string.length()) break;
            char c = string.charAt(end);
            if (c == '\r' || c == '\n') {
                break;
            }
        }
        line = string.substring(begin, end);
    }

    /**
     * 获得错误描述。
     * @return 错误描述
     */
    public String getMessage() {
        return (msg == null ? "unknown" : msg) + " at line \"" + line + "\"";
    }
}
