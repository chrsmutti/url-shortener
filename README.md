# url-shortener

A really basic implementation of a url-shortener, my first foray into the
[`cats`](https://typelevel.org/cats/) ecosystem in Scala using [`http4s`](https://http4s.org/).

## Build

This project uses [`sbt`](https://www.scala-sbt.org/) as the build tool. It also has a
plugin dependency on [`sbt-revolver`](https://github.com/spray/sbt-revolver), it's my
recommendation for running the web server, as the `run` command will keep the port allocated
even after a `sigterm` is sent.

```sh
sbt

sbt:url-shortener> compile
sbt:url-shortener> test

# from sbt-resolver
sbt:url-shortener> reStart
sbt:url-shortener> reStop

sbt:url-shortener> scalafixAll
```

## License

This project is licensed under the [MIT License](https://choosealicense.com/licenses/mit).
