public void testIssue368() {
    testTypes(
        "/** @constructor */ function Foo() {}"
        + "/** @param {number} x @param {number} y */"
        + "Foo.prototype.add = function(x, y) {};"
        + "/** @constructor @extends {Foo} */ function Bar() {}"
        + "/** @override @param {number} x */"
        + "Bar.prototype.add = function(x) {};"
        + "var bar = new Bar();"
        + "bar.add(1, 2);",
        "actual parameter 2 of Bar.prototype.add does not match formal parameter");
}