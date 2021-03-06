# Halty

[![Build Status](https://secure.travis-ci.org/krrrr38/halty.png)](http://travis-ci.org/krrrr38/halty)
[![codecov.io](http://codecov.io/github/krrrr38/halty/coverage.svg?branch=master)](http://codecov.io/github/krrrr38/halty?branch=master)
[![Codacy Badge](https://www.codacy.com/project/badge/8379734cad1a4b069a7bd76e3620de57)](https://www.codacy.com/app/k-kaizu38/halty)
[![Maven Central 2.11.x](https://maven-badges.herokuapp.com/maven-central/com.krrrr38/halty_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.krrrr38/halty_2.11)
[![Maven Central 2.10.x](https://maven-badges.herokuapp.com/maven-central/com.krrrr38/halty_2.10/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.krrrr38/halty_2.10)
[![License: MIT](http://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

Text-to-HTML converter with Halty syntax.

Halty syntax is based on [Hatena syntax](http://hatenadiary.g.hatena.ne.jp/keyword/%E3%81%AF%E3%81%A6%E3%81%AA%E8%A8%98%E6%B3%95%E4%B8%80%E8%A6%A7).

## Usage

```scala
libraryDependencies += "com.krrrr38" %% "halty" % "0.1.3"
```

or

```scala
resolvers += Opts.resolver.sonatypeSnapshots

libraryDependencies += "com.krrrr38" %% "halty" % "0.1.4-SNAPSHOT"
```

## Synopsys

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
val ast = Halty(input)
XHTMLGenerator.generate(ast)    # generate xhtml
MarkdownGenerator.generate(ast) # generate markdown
```

## Contribution

1. Fork ([https://github.com/krrrr38/halty/fork](https://github.com/krrrr38/halty/fork))
1. Create a feature branch
1. Commit your changes
1. Rebase your local changes against the target version branch
1. Run test suite with the `sbt test` command and confirm that it passes
1. Create new Pull Request

## Author

[krrrr38](https://github.com/krrrr38)
