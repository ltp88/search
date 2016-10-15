package rever.search

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.twitter.finagle.http.Status
import com.twitter.finatra.http.EmbeddedHttpServer
import com.twitter.finatra.thrift.ThriftClient
import com.twitter.inject.server.FeatureTest
import rever.search.domain.{SearchRequest, RegisterTemplateRequest}

/**
 * Created by phuonglam on 10/15/16.
 **/
class SingleFilterFeatureTest extends FeatureTest {
  val mapper: ObjectMapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  val writer = mapper.writerWithDefaultPrettyPrinter()

  override protected def server = new EmbeddedHttpServer(twitterServer = new Server) with ThriftClient

  "[HTTP] Put & Search Template " should {
    val templateName = "search-test-1"

    "put successful " in {
      val tplSource =
        """
          {
            "template": "{\"query\": {\"bool\": {\"filter\": {\"term\": {\"{{field}}\": \"{{value}}\"}}}}}"
          }
        """
      val registerTemplateRequest = RegisterTemplateRequest(templateName, tplSource)
      server.httpPut("/template",
        putBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(registerTemplateRequest),
        headers = Map("token" -> "b07c7deb733fd52df20fc26cda23e1a0"),
        andExpect = Status.Created,
        withJsonBody =
          """
            {
              "code":"succeed"
            }
          """)
    }

    "search successful" in {
      val searchRequest = SearchRequest(templateName, Array("tweet"),
        Map(
          "field" -> "message",
          "value" -> "elon"
        )
      )
      server.httpPost("/search",
        postBody = writer.writeValueAsString(searchRequest),
        andExpect = Status.Ok)
    }
  }
}
