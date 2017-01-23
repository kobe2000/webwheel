#Template Engine
WebWheel provides a clean and powerful templage engine. It aims to separate view from model whithout losing productivity.

##Basic
Rendering is accomplished by composing string template and java object. The template must be a full/partial well-formed html/xml. The object is pojo.
```html
<!DOCTYPE html>
<html lang="en">
<head>
<title>${title}</title>
</head>
<body>
<h1>${message}</h1>
</body>
</html>
```
```html
This is an html fragment/component.
<div>
    reusable text
</div>
component footer
```
```java
public class PageModel {
    public String title;
    public String getMessage() {
        return "hello";
    }
}
```
##Expression Element
The engine supports expression in templates. The syntax is:
```
${expression} or #{expression}
```
The former is wrapping mode, the latter is raw mode.
[MVEL](https://github.com/mvel/mvel) is used evaluating the expressions with model object as context.
The expression elements can appear in tag attribute's value or text content.
###Wrapping Mode Expression
In raw mode, the expression evaluated result will be shown as it be.
While in wrapping mode, the evaluated result will be processed in different situations.
* In text content, character `<` will be replaced by `&lt;`
```html
<div>${'<div>'}</div> -> <div>&lt;div></div>
```
* In attribute value,  character `"` will be replaced by `&quot;`
```html
<h2 title="${'"'}">"</h2> -> <h2 title="&quot;">"</h2>
```
* In `<script>` element or onxxx attribute value, [Jackson](https://github.com/FasterXML/jackson) is used to translate the result to string
```html
<a onclick="alert(${'hi'})">alert</a> -> <a onclick="alert('hi')">alert</a>
```
* In attribute likes `href/src/...`, the result will be encoded as URL
```html
<a href="${'u r l'}">link</a> -> <a href="u+r+l">link</a>
```
##Control-Flow
In this engine, all control-flow statements are expressed by `w:` tags.
There are several usage.
* introduce variables
```html
<div w:v1="1+2" w:v2="3" w:result="v1+v2">
${result}
</div>
```
* decision making
```html
<!-- must be bool type -->
<div w:="1==2'>never show</div>
<div w:="1!=2'>always show</div>
<!-- attribute control -->
<input type="checkbox" checked="${1==1}"> -> <input type="checkbox" checked="checked">
<input type="checkbox" checked="${1!=1}"> -> <input type="checkbox">
```
* loop
```html
<!-- must be iterable or array -->
<div w:v="[1,2,3]">
<!-- _idx variable is 0-based index -->
${v} at ${v_idx}
</div>
<!-- treat v as an ordinal array variable -->
<div w:v="#{[1,2,3}">
${v}
</div>
```
##Slot/Reference
You can reuse template fragment in same/other pages.
```html
<!-- page1.html -->
<html>
<body>
<div t:slot="header">Header</div>
<div t:ref="#header"></div>
</body>
</html>
```
```html
<!-- page2.html -->
<html>
<body>
<div t:ref="page1.html#header"></div>
</body>
</html>
```
You can also reimplement the fragments. The `slot` can also appear nested.
```html
<!-- shell.html -->
<html>
<head>
<title t:slot="title"></title>
</head>
<body t:slot="body">
<div t:slot="header">header</div>
<div t:slot="content"></div>
<div t:slot="footer">footer</div>
</body>
</html>
```
```html
<!-- index.html -->
<html t:ref="shell.html">
<title t:slot="title">index page</title>
<body t:slot="body" t:ref=""><!-- same as t:ref="shell.html#body" -->
<div t:slot="content">
This is content.
</div>
</body>
</html>
```
By this means, template pages can be organized cleanly, and we can reuse html fragment easily.
##Miscellaneous
### `NT/NoTag`
Sometimes you don't want tag displayed, then you can use `nt/notag` as tag's name.
```html
<!-- shell.html -->
<html>
<head>
<script t:slot="js_lib"></script>
</head>
<body>
</body>
</html>
```
```html
<!-- page.html -->
<html t:ref="shell.html">
<nt t:slot="js_lib">
<script src="..."></script>
<script src="..."></script>
</nt>
</html>
```
###Template Recursion
This function needs model to return template result.
```html
<!-- tree.html -->
<ul t:slot="tree">
<li w:b="branches">
${b.name}
${sub(b)}
</li>
</ul>
```
```java
public class Branch {
    public String name;
    public List<Branch> children;
}

public class Model {
    public List<Branch> branches;
    public cn.webwheel.results.TemplateResult getSub(Branch b) {
        Model sub = new Model();
        sub.branches = b.children;
        return new cn.webwheel.results.TemplateResult(sub, "tree.html#tree");
    }
}
```