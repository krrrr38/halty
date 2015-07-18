Halty
=========

Text-to-HTML converter with Halty syntax.

Halty syntax is based on [Hatena syntax](http://hatenadiary.g.hatena.ne.jp/keyword/%E3%81%AF%E3%81%A6%E3%81%AA%E8%A8%98%E6%B3%95%E4%B8%80%E8%A6%A7).

SYNOPSYS
---------

```scala
val input =
    """
    |* h3 header
    |text
    |
    |>https://github.com/krrrr38/halty>
    |cite from halty - github
    |<<
    |
    |- LIST
    |-- https://github.com/krrrr38/halty
    |-- [https://github.com/krrrr38/halty]
    |-- [https://github.com/krrrr38/halty:title]
    |-- [https://github.com/krrrr38/halty:title=foo]
    |
    |:dt:dd
    |
    ||table|table|
    ||100|200|
    |
    |>|scala|
    |case class Person(name: String)
    |||<
    """.stripMargin
val res = XHTMLGenerator.generate(Halty(input))
```
