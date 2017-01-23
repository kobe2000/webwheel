#Result Interpreter
In WebWheel, action methods can return some results to browser. In java result means an object.
WebWheel use Result Interpreter to translate the result object for browser.
There are two build-in Result Interpreters:
```
SimpleResultInterpreter
TemplateResultInterpreter
```
The `SimpleResultInterpreter` will handle all result type inheriting from `SimpleResult`:
```
DownloadResult
EmptyResult
ErrorResult
ForwardResult
IncludeResult
JavascriptResult
JsonResult
PlainTextResult
RedirectResult
```
Here's an example using SimpleResult:
```java
public class picture {
    @Action
    public Object jpg(int id) {
        //return 404 to browser
        return cn.webwheel.results.ErrorResult.notFound();
    }
}
```
The `TemplateResultInterpreter` handles `TemplateResult`:
```java
public class index {
    @cn.webwheel.Action
    public Object html() {
        Map<String, Object> model = new java.util.HashMap<>();
        return new cn.webwheel.results.TemplateResult(model, "index.html");
    }
}
```