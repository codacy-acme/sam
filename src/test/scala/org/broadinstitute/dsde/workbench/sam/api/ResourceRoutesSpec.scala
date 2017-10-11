package org.broadinstitute.dsde.workbench.sam.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.broadinstitute.dsde.workbench.sam.model._
import org.scalatest.{FlatSpec, Matchers}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import org.broadinstitute.dsde.workbench.model.ErrorReport
import spray.json.{JsBoolean, JsValue}
import spray.json.DefaultJsonProtocol._
import org.broadinstitute.dsde.workbench.model.ErrorReportJsonSupport._

/**
  * Created by dvoet on 6/7/17.
  */
class ResourceRoutesSpec extends FlatSpec with Matchers with ScalatestRouteTest {

  "ResourceRoutes" should "404 for unknown resource type" in {
    val samRoutes = TestSamRoutes(Map.empty)

    Get("/api/resource/foo/bar/action") ~> samRoutes.route ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[ErrorReport].message shouldEqual "resource type foo not found"
    }
  }

  it should "list all resource types" in {
    val samRoutes = TestSamRoutes(Map.empty)

    Get("/api/resourceTypes") ~> samRoutes.route ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  it should "204 create resource" in {
    val resourceType = ResourceType(ResourceTypeName("rt"), Set(ResourceAction("run")), Set(ResourceRole(ResourceRoleName("owner"), Set(ResourceAction("run")))), ResourceRoleName("owner"))
    val samRoutes = TestSamRoutes(Map(resourceType.name -> resourceType))

    Post(s"/api/resource/${resourceType.name}/foo") ~> samRoutes.route ~> check {
      status shouldEqual StatusCodes.NoContent
    }

    Get(s"/api/resource/${resourceType.name}/foo/action/run") ~> samRoutes.route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[JsValue] shouldEqual JsBoolean(true)
    }

  }

  it should "200 on list resource roles" in {
    val resourceType = ResourceType(ResourceTypeName("rt"), Set(ResourceAction("run")), Set(ResourceRole(ResourceRoleName("owner"), Set(ResourceAction("run")))), ResourceRoleName("owner"))
    val samRoutes = TestSamRoutes(Map(resourceType.name -> resourceType))

    Post(s"/api/resource/${resourceType.name}/foo") ~> samRoutes.route ~> check {
      status shouldEqual StatusCodes.NoContent
    }

    Get(s"/api/resource/${resourceType.name}/foo/roles") ~> samRoutes.route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Set[String]]
    }
  }

  it should "404 on list resource roles when resource type doesnt exist" in {
    val resourceType = ResourceType(ResourceTypeName("rt"), Set(ResourceAction("run")), Set(ResourceRole(ResourceRoleName("owner"), Set(ResourceAction("run")))), ResourceRoleName("owner"))
    val samRoutes = TestSamRoutes(Map(resourceType.name -> resourceType))

    Get(s"/api/resource/doesntexist/foo/roles") ~> samRoutes.route ~> check {
      status shouldEqual StatusCodes.NotFound
    }
  }

}

