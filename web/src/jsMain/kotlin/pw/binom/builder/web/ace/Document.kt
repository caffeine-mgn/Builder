package pw.binom.builder.web.ace

external class Document{
    constructor(text:String)
    constructor(lines:Array<String>)

    var value: String
        @JsName("getValue")
        get
        @JsName("setValue")
        set
}