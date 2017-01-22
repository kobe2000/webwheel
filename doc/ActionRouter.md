#Action Router
In WebWheel each URL is mapped to an action. An action is in fact an instance or static method in java language.
##Basic
Mapping is done by API `cn.webwheel.Main.map`, for example:
```java
public class MyActionClass {
    
    public static String action1() {
        return "action1 is executed";
    }
    
    public String action2() {
        return "action2 is executed";
    }
}
```
```java
public class MyMain extends cn.webwheel.Main {
    
    public void init() {
        map("/hello1").with(MyActionClass.class, "action1");
        map("/hello2").with(MyActionClass.class, MyActionClass.class.getMethod("action2"));
    }
    
    public <T> T createAction(WebContext ctx, Class<T> type) {
        super.createAction(ctx, type);
    }
}
```
Here we map two URLs to two java methods.
When web application container receives url "/hello1" or "/hello2" (relative to servlet context path),
the two methods will be invoked. And before the instance method is called,
the instance of class will be created by `createAction` method,
and the default behavior is using default constructor to create one.
If the class is instance of `cn.webwheel.WebContextAware`,
the `WebContext` instance will be also injected. If you use a IOC framework, such as Spring,
you may override this method to control the action instance initialization.

##URL Rewrite
Meanwhile, there's a simple url rewrite mechanism based on Regex in WebWheel.
```java
public class MyMain extends cn.webwheel.Main {
    
    public void init() {
        map("/book.html?cat=$1&id=$2").rest("/book/(.*?)/(\\d+)").with(MyBooks.class, "show_book");
    }
}

```
This time, URL `/book/java/81` will be treated as `/book.html?cat=java&id=81`.
##Auto Map
Using java code to map all urls to their actions is annoying and ineffective.
WebWheel uses reflection and annotation to simplify this process.
The API is hosted in `cn.webwheel.DefaultMain`, which inherits from `cn.webwheel.Main`.
```java
public class MyMain extends cn.webwheel.DefaultMain {
    
    public void init() {
        super.init();
        autoMap("com.my.web");
    }
}
```
```java
package com.my.web;
public class MyAction {
    @cn.webwheel.Action("/my_action")
    public void execute() {
    }
}
```
This code is equivalent with following:
```java
public class MyMain extends cn.webwheel.DefaultMain {
    
    public void init() {
        super.init();
        map("/my_action").with(MyAction.class, "execute");
    }
}
```
```java
package com.my.web;
public class MyAction {
    public void execute() {
    }
}
```
The `autoMap` method will scan all classes in the package recursively.
It checks every method annotated by `cn.webwheel.Action`, then maps it to url corresponded.
When a lot of actions need to be managed, the convention is far more better than code.

There are 4 manners using url in `cn.webwheel.Action`:
* If the url is omitted, the class name and method name will be joined by `.` to make an url.
* If the url is leading by `.`, the postfix will be replaced.
* If the url is not leading by `/`, the relative path will be remained.
* Otherwise, full url annotated will be used.

For example:
```java
public class MyMain extends cn.webwheel.DefaultMain {
    
    public void init() {
        super.init();
        autoMap("com.my.web");
    }
}
```
```java
package com.my.web.sub;
public class MyAction {
    //url will be '/sub/MyAction.execute'
    @cn.webwheel.Action
    public void execute() {
    }
    //url will be '/sub/MyAction.do'
    @cn.webwheel.Action(".do")
    public void execute() {
    }
    //url will be '/sub/my_action.do'
    @cn.webwheel.Action("my_action.do")
    public void execute() {
    }
    //url will be '/my_action.do'
    @cn.webwheel.Action("/my_action.do")
    public void execute() {
    }
}
```
The rewrite functions are supported by `cn.webwheel.Action` too.