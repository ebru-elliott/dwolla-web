import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "dwolla-web"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      javaCore,
      javaJdbc,
      javaEbean,
      "com.squareup.retrofit" % "retrofit" %   "1.2.2" ,
      "org.mindrot" % "jbcrypt" % "0.3m",
      "org.mongodb" % "mongo-java-driver" % "2.11.3",
      "org.mongojack" % "mongojack" % "2.0.0-RC5"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here      
    )

}
