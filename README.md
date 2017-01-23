#A Simple Java Web Application Framework
WebWheel helps Java developers build a web site rapidly.
It mainly includes a template engine,
an url router and a parameter injection framework.
WebWheel do things straightly and naturally, maybe straight-arrow.
If you like old Java, I think you will like WebWheel too.

#How to use
First of all, you need specifying [WebWheelFilter](src/cn/webwheel/WebWheelFilter.java) as servlet filter in web.xml.
Then assign the Application Entrance Class to `main` parameter. 
```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
         version="3.1">
    <filter>
        <filter-name>WebWheel</filter-name>
        <filter-class>cn.webwheel.WebWheelFilter</filter-class>
        <init-param>
            <param-name>main</param-name>
            <param-value>com.my.Main</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>WebWheel</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
</web-app>
```
The Application Entrance Class looks like:
```java
package com.my;

import cn.webwheel.*;

public class Main extends DefaultMain {
    
    @Override
    protected void init() {
        
        super.init();//prepare template engine and action setter engine
        
        autoMap("com.my.web");//look up all action classes under a package in recurrence manner
    }
}
```
Now we use an html page to demonstrate the usages: `index.html` in web root directory.
```html
<!-- /index.html -->
<html>
<head>
<!-- expression evaluation & render it -->
<title>${title}</title>
</head>
<body>
<h3 t:slot="msg">${message}</h3>
<!-- action uri is /index.say -->
<form action="/index.say">
Say: <input name="what"><input type="submit">
</form>
<h3 t:ref="#msg"></h3>
</body>
</html>
```
In WebWheel framework, one page is usually bound to a class(not necessary):
```java
package com.my.web;

import cn.webwheel.*;

public class index extends DefaultPage {

    //can be used in template & changed by url parameter
    public String title = "Hello WebWheel";
    
    //can be used in template by getter
    private String message;
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public Object html() {
        return super.html();//returning a template result
    }
    
    @Action
    public Object say(String what/*action parameter*/) {
        message = "Oh " + what;//echo message
        return super.html();//forward to self page
    }
}

```
Well done! This is a tiny but complete WebWheel application.
There are more wonderful features to be continued:
* [Action Router](doc/ActionRouter.md)
* [Action Setter](doc/ActionSetter.md)
* [Result Interpreter](doc/ResultInterpreter.md)
* [Template Engine](doc/TemplateEngine.md)
* [DefaultAction & DefaultPage](doc/DefaultActionPage.md)

#Depends
WebWheel depends on and thanks:
* [MVEL](https://github.com/mvel/mvel)
* [Apache Commons FileUpload](http://commons.apache.org/fileupload/)
* [Jackson Java JSON-processor](http://jackson.codehaus.org/)