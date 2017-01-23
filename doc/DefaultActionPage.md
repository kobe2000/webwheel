#DefaultAction & DefaultPage
##DefaultAction
In this class, many parameter validating method and other convenience functions is provided.
```java
public class user extends cn.webwheel.DefaultAction {
    @cn.webwheel.Action
    public cn.webwheel.results.JsonResult signup(String id, String pwd) {
        notEmpty(notNull(id, "id is null"), "id is empty");
        range(pwd, 6, 12, "password length error");
        return ok();
    }
}
```
If the validation failed, it will throw an `LogicException` which will be caught by `DefaultMain`,
and the latter will return an `JsonResult` with error message to the browser.
##DefaultPage
This class inherits from `DefaultAction`. In addition,
it provide a simple method named `html` which return a template result based on the html template corresponding.
```java
public class book extends cn.webwheel.DefaultPage {
    
    public String id;
    
    @Override
    public Object html() {
        if(id == null) {
            ErrorPage p = new ErrorPage();//ErrorPage inherits from DefaultPage
            p.message = "book not found";
            return p.redirect();//redirect to error page
        }
        return super.html();
    }
}
```