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

package cn.webwheel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Action method configuration when using {@link Main#autoMap(String)}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Action {

    /**
     * http url
     * @see Main#map(String)
     */
    String value() default "";

    /**
     * Disable action method. Used when need to disable parent class action method.
     */
    boolean disabled() default false;

    /**
     * url rewrite pattern
     * @see Main.ActionBinder#rest(String)
     */
    String rest() default "";

    /**
     * http parameter binding setter string encoding
     * @see SetterConfig#getCharset()
     */
    String charset() default "";

    /**
     * http parameter binding setter file upload size max
     * @see SetterConfig#getFileUploadSizeMax()
     */
    int fileUploadSizeMax() default 0;

    /**
     * http parameter binding setter file upload file size max
     * @see SetterConfig#getFileUploadFileSizeMax()
     */
    int fileUploadFileSizeMax() default 0;

    /**
     * http parameter injection point
     */
    SetterPolicy setterPolicy() default SetterPolicy.Auto;
}
