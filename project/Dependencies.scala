import sbt._

object Dependencies {

  case object ch {
    case object qos {
      lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
    }
  }

  // NOTE: io is reserved.
  case object circe {
    private[this] lazy val version = "0.13.0"

    lazy val `circe-literal` = "io.circe" %% "circe-literal" % version
    lazy val `circe-generic` = "io.circe" %% "circe-generic" % version
  }

  case object chrisdavenport {
    lazy val `log4cats-slf4j` = "io.chrisdavenport" %% "log4cats-slf4j" % "1.1.1"
  }

  case object com {
    case object codecommit {
      lazy val `cats-effect-testing-utest` =
        "com.codecommit" %% "cats-effect-testing-utest" % "0.5.0"
    }

    case object github {
      case object liancheng {
        lazy val `organize-imports` = "com.github.liancheng" %% "organize-imports" % "0.4.4"
      }

      case object pureconfig {
        lazy val pureconfig = "com.github.pureconfig" %% "pureconfig" % "0.14.0"
      }
    }

    case object lihaoyi {
      lazy val utest = "com.lihaoyi" %% "utest" % "0.7.2"
    }
  }

  case object dev {
    case object profunktor {
      private[this] lazy val version = "0.11.1"

      lazy val `redis4cats-effects` = "dev.profunktor" %% "redis4cats-effects" % version
      lazy val `redis4cats-log4cats` = "dev.profunktor" %% "redis4cats-log4cats" % version
    }
  }

  case object org {
    case object http4s {
      private[this] lazy val version = "0.21.14"

      lazy val `http4s-dsl` = "org.http4s" %% "http4s-dsl" % version
      lazy val `http4s-blaze-server` = "org.http4s" %% "http4s-blaze-server" % version
      lazy val `http4s-circe` = "org.http4s" %% "http4s-circe" % version
    }

    case object typelevel {
      lazy val `cats-core` = "org.typelevel" %% "cats-core" % "2.3.0"
      lazy val `cats-effect` = "org.typelevel" %% "cats-effect" % "2.1.4"
    }
  }
}
