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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlainElement implements PlainNode {

    private String tag;
    private LinkedHashMap<String, String> attributes = new LinkedHashMap<String, String>();
    private List<PlainNode> content = new ArrayList<PlainNode>();
    private boolean closed;

    private int location;

    public PlainElement(String tag, int location) {
        this.tag = tag;
        this.location = location;
    }

    public String getTag() {
        return tag;
    }

    public LinkedHashMap<String, String> getAttributes() {
        return attributes;
    }

    public List<PlainNode> getContent() {
        return content;
    }

    public void nullContent(boolean closed) {
        content = null;
        this.closed = closed;
    }

    public boolean isClosed() {
        return closed;
    }

    public int getLocation() {
        return location;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (tag != null) {
            sb.append('<').append(tag);
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                sb.append(' ').append(entry.getKey());
                if (entry.getValue() != null) {
                    sb.append('=').append('"').append(entry.getValue()).append('"');
                }
            }
            if (content == null && closed) {
                sb.append("/>");
            } else {
                sb.append('>');
            }
        }
        if (content != null) {
            for (Object o : content) {
                sb.append(o);
            }
        }
        if (tag != null && content != null) {
            sb.append("</").append(tag).append('>');
        }
        return sb.toString();
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
