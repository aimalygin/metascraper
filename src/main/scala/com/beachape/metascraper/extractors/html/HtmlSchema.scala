package com.beachape.metascraper.extractors.html

import java.nio.charset.Charset

import com.beachape.metascraper.extractors.{ SchemaFactory, Schema }
import com.ning.http.client.Response
import com.ning.http.util.AsyncHttpProviderUtils
import dispatch.as.String
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Created by Lloyd on 2/15/15.
 */
trait HtmlSchema extends Schema {

  def doc: Document

  /**
   * Gets the non-empty content of a Document element.
   *
   * Returns None if it is empty
   */
  protected def nonEmptyContent(doc: Document, selector: String): Option[String] = Option {
    doc.select(selector).attr("content")
  }.filter(_.nonEmpty)

}

case class HtmlSchemas(schemas: (Document => HtmlSchema)*) extends SchemaFactory {

  val contentTypes: Seq[String] = Seq("text/html")

  def apply(resp: Response): Seq[HtmlSchema] = {
    val doc = parse(resp)
    schemas.map(_.apply(doc))
  }

  protected def parse(resp: Response): Document = {
    val detectedFromResp = responseCharset(resp)
    Jsoup.parse(detectedFromResp(resp), resp.getUri.toString)
  }

  protected def responseCharset(resp: Response): String.charset =
    tryFromContentType(resp.getContentType).getOrElse(String.utf8)

  protected def tryFromContentType(contentType: String): Option[String.charset] =
    for {
      ct <- Option { contentType }
      charset <- Option { AsyncHttpProviderUtils.parseCharset(ct) }
      if Charset.isSupported(charset)
    } yield String.charset(Charset.forName(charset))
}