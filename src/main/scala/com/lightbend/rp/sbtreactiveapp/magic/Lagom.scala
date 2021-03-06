/*
 * Copyright 2017 Lightbend, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lightbend.rp.sbtreactiveapp.magic

import com.lightbend.rp.sbtreactiveapp._
import play.api.libs.json.{JsObject, Json}
import sbt._
import scala.collection.immutable.Seq
import scala.language.reflectiveCalls

object Lagom {
  def component(id: String): Option[ModuleID] = {
    // The method signature equals the signature of `com.lightbend.lagom.sbt.LagomImport`
    type LagomImport = {
      def component(id: String): ModuleID
    }

    withContextClassloader(this.getClass.getClassLoader) { loader =>
      getSingletonObject[LagomImport](loader, "com.lightbend.lagom.sbt.LagomImport$")
        .map(_.component(id))
        .toOption
    }
  }

  def endpoints(classPath: Seq[Attributed[File]], scalaLoader: ClassLoader): Option[Map[String, Endpoint]] =
    services(classPath, scalaLoader).map(decodeServices)

  def isJava: Boolean = localObjectExists("com.lightbend.lagom.sbt.LagomJava$")

  def isPlayJava: Boolean = localObjectExists("com.lightbend.lagom.sbt.LagomPlayJava$")

  def isPlayScala: Boolean = localObjectExists("com.lightbend.lagom.sbt.LagomPlayScala$")

  def isScala: Boolean = localObjectExists("com.lightbend.lagom.sbt.LagomScala$")

  def services(classPath: Seq[Attributed[File]], scalaLoader: ClassLoader): Option[String] = {
    // `ServiceDetector` mirror from the Lagom api tools library.
    // The method signature equals the signature from the api tools `ServiceDetector`
    type ServiceDetector = {
      def services(classLoader: ClassLoader): String
    }

    val classLoader = new java.net.URLClassLoader(classPath.files.map(_.toURI.toURL).toArray, scalaLoader)

    withContextClassloader(classLoader) { loader =>
      getSingletonObject[ServiceDetector](loader, "com.lightbend.lagom.internal.api.tools.ServiceDetector$")
        .map(_.services(loader))
        .toOption
    }
  }

  def version: Option[String] = {
    // The method signature equals the signature of `com.lightbend.lagom.core.LagomVersion`
    type LagomVersion = {
      def current: String
    }

    withContextClassloader(this.getClass.getClassLoader) { loader =>
      getSingletonObject[LagomVersion](loader, "com.lightbend.lagom.core.LagomVersion$")
        .map(_.current)
        .toOption
    }
  }

  private def localObjectExists(className: String): Boolean =
    withContextClassloader(this.getClass.getClassLoader) { loader =>
      objectExists(loader, className)
    }

  private def decodeServices(services: String): Map[String, Endpoint] = {
    def toEndpoint(pathBegins: Seq[String]): Endpoint =
      Endpoint(
        protocol = "http",
        port = 0,
        acls = pathBegins.distinct.map {
          case "" => HttpAcl("^/")
          case pt => HttpAcl(s"^$pt")
        }
      )

    def mergeEndpoint(endpoints: Map[String, Endpoint], endpointEntry: (String, Endpoint)): Map[String, Endpoint] =
      endpointEntry match {
        case (serviceName, endpoint) =>
          val mergedEndpoint =
            endpoints
              .get(serviceName)
              .fold(endpoint) { prevEndpoint =>
                prevEndpoint.copy(acls = prevEndpoint.acls ++ endpoint.acls)
              }

          endpoints + (serviceName -> mergedEndpoint)
      }

    Json
      .parse(services)
      .as[Seq[JsObject]].map { o =>
        val serviceName = (o \ "name").as[String]
        val pathlessServiceName = if (serviceName.startsWith("/")) serviceName.drop(1) else serviceName
        val pathBegins = (o \ "acls" \\ "pathPattern")
          .map(_.as[String])
          .toVector
          .collect {
            case pathBeginExtractor(pathBegin) =>
              if (pathBegin.endsWith("/"))
                pathBegin.dropRight(1)
              else
                pathBegin
          }

        pathlessServiceName -> toEndpoint(pathBegins)
      }
      .foldLeft(Map.empty[String, Endpoint])(mergeEndpoint)
  }

  // Matches strings that starts with sequence escaping, e.g. \Q/api/users/:id\E
  // The first sequence escaped substring that starts with a '/' is extracted as a variable
  // Examples:
  // /api/users                         => false
  // \Q/\E                              => true, variable = /
  // \Q/api/users\E                     => true, variable = /api/users
  // \Q/api/users/\E([^/]+)             => true, variable = /api/users/
  // \Q/api/users/\E([^/]+)\Q/friends\E => true, variable = /api/users/
  private val pathBeginExtractor = """^\\Q(\/.*?)\\E.*""".r
}
