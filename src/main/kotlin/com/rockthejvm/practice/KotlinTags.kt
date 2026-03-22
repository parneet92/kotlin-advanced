package com.rockthejvm.practice

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

object KotlinTags {
    /*
        HTML rendering tags
        Kotlin Tags

        val document = html {
            head {
                title("My Web Page")
            }
            body {
                div(id="header", className = "main-header"){
                    p("Welcome to my Website")
                }
                div {
                    p("this was start of my site")
                    p("this was rendered with kotlin tags")
                }
            }
        }

        1. Define data types for the HTML tags we want to support
            - html, head, title, body, div, p
        2. Define some "builder" that enable the DSL for every tag we want to support
            - HTMLBuilder, HeadBuilder, BodyBuilder, DivBuilder
        3. Define methods that take Lambdas with receivers as arguments => build the DSL
        4. test that it works

     */


    sealed interface HtmlTag
    sealed interface HtmlElement

    data class Html(val head: Head, val body: Body): HtmlElement{
        override fun toString(): String =
            "<html>\n$head\n$body\n</html>"
    }
    data class Head(val title: Title): HtmlElement{
        override fun toString(): String =
            "<head>\n$title\n</head>"
    }
    data class Title(val title: String): HtmlElement{
        override fun toString(): String =
            "<title>${title}</title>"
    }
    data class Body(val children: List<HtmlElement>): HtmlElement{
        override fun toString(): String =
            children.joinToString("\n", "<body>","</body>")
    }
    data class Div(val children: List<HtmlElement>, val id: String?=null, val className: String?=null): HtmlElement{
        val idAttr = id?.let { " id = \"$it\" " } ?: ""
        val classAttr = className?.let { " className = \"$it\" " } ?: ""
        val innerHtml = children.joinToString("\n")
        override fun toString(): String =
            "<div$idAttr$classAttr>$innerHtml</div>"
    }
    data class P(val content: String): HtmlElement{
        override fun toString(): String =
            "<p>$content</p>"
    }

    class DivBuilder(val id: String?, val className: String?) {
        private val children = mutableListOf<HtmlElement>()
        fun p(content: String) {
            children.add(P(content))
        }

        fun build() = Div(children, id, className)
    }

    class BodyBuilder{
        private val children = mutableListOf<HtmlElement>()
        fun div(id: String? = null, className: String? = null, init: DivBuilder.() -> Unit) {
            val builder = DivBuilder(id, className)
            builder.init()
            children.add(builder.build())
        }

        fun p(content: String) {
            children.add(P(content))
        }

        fun build() = Body(children)
    }

    class HeadBuilder {
        private lateinit var title: Title
        fun title(content: String){
            title = Title(content)
        }

        fun build() = Head(title)
    }

    class HtmlBuilder {

        private lateinit var head: Head
        private lateinit var body: Body

        fun head(init: HeadBuilder.() -> Unit) {
            val builder = HeadBuilder()
            builder.init()
            head = builder.build()
        }

        fun body(init: BodyBuilder.() -> Unit) {
            val builder = BodyBuilder()
            builder.init()
            body = builder.build()
        }

        fun build() = Html(head, body)
    }

    fun html(init: HtmlBuilder.() -> Unit): Html {
        val builder = HtmlBuilder()
        builder.init()
        return builder.build()
    }


    val htmlExample = html {
        head {
            title("My first Title")
            body {
                div(id="header", className = "top-class") {
                    p("Very nice content")
                }
                div {
                    p("Some other content")
                }
            }
        }
    }


    @JvmStatic
    fun main(args: Array<String>) {
        val page = PrintWriter(FileWriter(File("src/main/resources/sample.html")))
        page.println(htmlExample)
        page.close()
    }
}