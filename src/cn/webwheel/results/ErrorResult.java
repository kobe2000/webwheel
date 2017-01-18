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

package cn.webwheel.results;

import java.io.IOException;

/**
 * http error result
 */
public class ErrorResult extends SimpleResult {

    private int statusCode;
    private String msg;

    /**
     * @param statusCode error code: 404, 500, ...
     */
    public ErrorResult(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * @param statusCode error code: 404, 500, ...
     * @param msg error message
     */
    public ErrorResult(int statusCode, String msg) {
        this.statusCode = statusCode;
        this.msg = msg;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMsg() {
        return msg;
    }

    public void render() throws IOException {
        if (msg == null) {
            ctx.getResponse().sendError(statusCode);
        } else {
            ctx.getResponse().sendError(statusCode, msg);
        }
    }

    public static ErrorResult notFound() {
        return new ErrorResult(404, "Not Found");
    }
}
