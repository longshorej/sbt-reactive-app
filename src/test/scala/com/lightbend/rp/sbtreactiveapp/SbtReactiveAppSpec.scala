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

package com.lightbend.rp.sbtreactiveapp

class SbtReactiveAppSpec extends UnitSpec {
  "labels" should {
    "work for defaults" in {
      SbtReactiveApp.labels(
        appName = None,
        diskSpace = None,
        memory = None,
        nrOfCpus = None,
        endpoints = Vector.empty,
        volumes = Map.empty,
        privileged = false,
        healthCheck = None,
        readinessCheck = None,
        environmentVariables = Map.empty,
        version = None,
        secrets = Set.empty) shouldBe Map.empty
    }

    "work for all values (except checks)" in {
      SbtReactiveApp.labels(
        appName = Some("myapp"),
        diskSpace = Some(1234),
        memory = Some(5678),
        nrOfCpus = Some(0.25),
        endpoints = Vector(
          HttpEndpoint("ep1", 1234),
          HttpEndpoint("ep2", 5678, HttpPathIngress("^/my-url.*$"), HttpHostIngress("hello.world.com")),
          TcpEndpoint("ep3", PortIngress(9123)),
          UdpEndpoint("ep4", PortIngress(8123)),
          HttpEndpoint("ep5", PortIngress(1235), httpIngress = Vector.empty, version = None),
          HttpEndpoint("ep6", PortIngress(1236), httpIngress = Vector.empty, version = Some(LiteralVersion("buggy123"))),
          HttpEndpoint("ep7", PortIngress(1237), httpIngress = Vector.empty, version = Some(MajorMinorVersion))
        ),
        volumes = Map(
          "/data/vol1" -> HostPathVolume("/var/lib/vol1"),
          "/data/vol2" -> HostPathVolume("/var/lib/vol2")
        ),
        privileged = true,
        healthCheck = None,
        readinessCheck = None,
        environmentVariables = Map(
          "env1" -> LiteralEnvironmentVariable("my env one"),
          "env2" -> kubernetes.ConfigMapEnvironmentVariable("my-map", "my-key")),
        version = Some((1, 2, 3, Some("SNAPSHOT"))),
        secrets = Set(Secret("myns1", "myname1"), Secret("myns2", "myname2"))) shouldBe Map(

        "com.lightbend.rp.app-name" -> "myapp",
        "com.lightbend.rp.disk-space" -> "1234",
        "com.lightbend.rp.memory" -> "5678",
        "com.lightbend.rp.nr-of-cpus" -> "0.25",
        "com.lightbend.rp.privileged" -> "true",
        "com.lightbend.rp.endpoints.0.name" -> "ep1",
        "com.lightbend.rp.endpoints.0.port" -> "1234",
        "com.lightbend.rp.endpoints.0.protocol" -> "http",
        "com.lightbend.rp.endpoints.0.version" -> "1",
        "com.lightbend.rp.endpoints.1.name" -> "ep2",
        "com.lightbend.rp.endpoints.1.port" -> "5678",
        "com.lightbend.rp.endpoints.1.protocol" -> "http",
        "com.lightbend.rp.endpoints.1.version" -> "1",
        "com.lightbend.rp.endpoints.1.ingress.0.type" -> "http-path",
        "com.lightbend.rp.endpoints.1.ingress.0.path" -> "^/my-url.*$",
        "com.lightbend.rp.endpoints.1.ingress.1.type" -> "http-host",
        "com.lightbend.rp.endpoints.1.ingress.1.host" -> "hello.world.com",
        "com.lightbend.rp.endpoints.2.name" -> "ep3",
        "com.lightbend.rp.endpoints.2.protocol" -> "tcp",
        "com.lightbend.rp.endpoints.2.port" -> "9123",
        "com.lightbend.rp.endpoints.2.version" -> "1",
        "com.lightbend.rp.endpoints.3.name" -> "ep4",
        "com.lightbend.rp.endpoints.3.protocol" -> "udp",
        "com.lightbend.rp.endpoints.3.port" -> "8123",
        "com.lightbend.rp.endpoints.3.version" -> "1",
        "com.lightbend.rp.endpoints.4.name" -> "ep5",
        "com.lightbend.rp.endpoints.4.port" -> "1235",
        "com.lightbend.rp.endpoints.4.protocol" -> "http",
        "com.lightbend.rp.endpoints.5.name" -> "ep6",
        "com.lightbend.rp.endpoints.5.port" -> "1236",
        "com.lightbend.rp.endpoints.5.protocol" -> "http",
        "com.lightbend.rp.endpoints.5.version" -> "buggy123",
        "com.lightbend.rp.endpoints.6.name" -> "ep7",
        "com.lightbend.rp.endpoints.6.port" -> "1237",
        "com.lightbend.rp.endpoints.6.protocol" -> "http",
        "com.lightbend.rp.endpoints.6.version" -> "1.2",
        "com.lightbend.rp.volumes.0.type" -> "host-path",
        "com.lightbend.rp.volumes.0.path" -> "/var/lib/vol1",
        "com.lightbend.rp.volumes.0.guest-path" -> "/data/vol1",
        "com.lightbend.rp.volumes.1.type" -> "host-path",
        "com.lightbend.rp.volumes.1.path" -> "/var/lib/vol2",
        "com.lightbend.rp.volumes.1.guest-path" -> "/data/vol2",
        "com.lightbend.rp.environment-variables.0.name" -> "env1",
        "com.lightbend.rp.environment-variables.0.type" -> "literal",
        "com.lightbend.rp.environment-variables.0.value" -> "my env one",
        "com.lightbend.rp.environment-variables.1.name" -> "env2",
        "com.lightbend.rp.environment-variables.1.type" -> "configMap",
        "com.lightbend.rp.environment-variables.1.map-name" -> "my-map",
        "com.lightbend.rp.environment-variables.1.key" -> "my-key",
        "com.lightbend.rp.version-major" -> "1",
        "com.lightbend.rp.version-minor" -> "2",
        "com.lightbend.rp.version-patch" -> "3",
        "com.lightbend.rp.version-patch-label" -> "SNAPSHOT",
        "com.lightbend.rp.secrets.0.namespace" -> "myns1",
        "com.lightbend.rp.secrets.0.name" -> "myname1",
        "com.lightbend.rp.secrets.1.namespace" -> "myns2",
        "com.lightbend.rp.secrets.1.name" -> "myname2")
    }

    "work for tcp checks" in {
      SbtReactiveApp.labels(
        appName = None,
        diskSpace = None,
        memory = None,
        nrOfCpus = None,
        endpoints = Vector.empty,
        volumes = Map.empty,
        privileged = false,
        healthCheck = Some(TcpCheck(80, 10)),
        readinessCheck = Some(TcpCheck(90, 5)),
        environmentVariables = Map.empty,
        version = None,
        secrets = Set.empty) shouldBe Map(
          "com.lightbend.rp.health-check.type" -> "tcp",
          "com.lightbend.rp.health-check.port" -> "80",
          "com.lightbend.rp.health-check.interval" -> "10",
          "com.lightbend.rp.readiness-check.type" -> "tcp",
          "com.lightbend.rp.readiness-check.port" -> "90",
          "com.lightbend.rp.readiness-check.interval" -> "5")

      SbtReactiveApp.labels(
        appName = None,
        diskSpace = None,
        memory = None,
        nrOfCpus = None,
        endpoints = Vector.empty,
        volumes = Map.empty,
        privileged = false,
        healthCheck = Some(TcpCheck("test", 10)),
        readinessCheck = Some(TcpCheck("test2", 5)),
        environmentVariables = Map.empty,
        version = None,
        secrets = Set.empty) shouldBe Map(
          "com.lightbend.rp.health-check.type" -> "tcp",
          "com.lightbend.rp.health-check.service-name" -> "test",
          "com.lightbend.rp.health-check.interval" -> "10",
          "com.lightbend.rp.readiness-check.type" -> "tcp",
          "com.lightbend.rp.readiness-check.service-name" -> "test2",
          "com.lightbend.rp.readiness-check.interval" -> "5")
    }

    "work for http checks" in {
      SbtReactiveApp.labels(
        appName = None,
        diskSpace = None,
        memory = None,
        nrOfCpus = None,
        endpoints = Vector.empty,
        volumes = Map.empty,
        privileged = false,
        healthCheck = Some(HttpCheck(80, 10, "/health")),
        readinessCheck = Some(HttpCheck(90, 5, "/other-health")),
        environmentVariables = Map.empty,
        version = None,
        secrets = Set.empty) shouldBe Map(
        "com.lightbend.rp.health-check.type" -> "http",
        "com.lightbend.rp.health-check.port" -> "80",
        "com.lightbend.rp.health-check.interval" -> "10",
        "com.lightbend.rp.health-check.path" -> "/health",
        "com.lightbend.rp.readiness-check.type" -> "http",
        "com.lightbend.rp.readiness-check.port" -> "90",
        "com.lightbend.rp.readiness-check.interval" -> "5",
        "com.lightbend.rp.readiness-check.path" -> "/other-health")

      SbtReactiveApp.labels(
        appName = None,
        diskSpace = None,
        memory = None,
        nrOfCpus = None,
        endpoints = Vector.empty,
        volumes = Map.empty,
        privileged = false,
        healthCheck = Some(HttpCheck("test", 10, "/health")),
        readinessCheck = Some(HttpCheck("test2", 5, "/other-health")),
        environmentVariables = Map.empty,
        version = None,
        secrets = Set.empty) shouldBe Map(
        "com.lightbend.rp.health-check.type" -> "http",
        "com.lightbend.rp.health-check.service-name" -> "test",
        "com.lightbend.rp.health-check.interval" -> "10",
        "com.lightbend.rp.health-check.path" -> "/health",
        "com.lightbend.rp.readiness-check.type" -> "http",
        "com.lightbend.rp.readiness-check.service-name" -> "test2",
        "com.lightbend.rp.readiness-check.interval" -> "5",
        "com.lightbend.rp.readiness-check.path" -> "/other-health")
    }

    "work for command checks" in {
      SbtReactiveApp.labels(
        appName = None,
        diskSpace = None,
        memory = None,
        nrOfCpus = None,
        endpoints = Vector.empty,
        volumes = Map.empty,
        privileged = false,
        healthCheck = Some(CommandCheck("/bin/bash", "arg one", "arg two")),
        readinessCheck = Some(CommandCheck("/bin/ash", "arg 1", "arg 2")),
        environmentVariables = Map.empty,
        version = None,
        secrets = Set.empty) shouldBe Map(
        "com.lightbend.rp.health-check.type" -> "command",
        "com.lightbend.rp.health-check.args.0" -> "/bin/bash",
        "com.lightbend.rp.health-check.args.1" -> "arg one",
        "com.lightbend.rp.health-check.args.2" -> "arg two",
        "com.lightbend.rp.readiness-check.type" -> "command",
        "com.lightbend.rp.readiness-check.args.0" -> "/bin/ash",
        "com.lightbend.rp.readiness-check.args.1" -> "arg 1",
        "com.lightbend.rp.readiness-check.args.2" -> "arg 2")
    }
  }
}
