package gitbucket.plugin.pages

import org.scalatest.funspec.AnyFunSpec

class PagesControllerBaseSpec extends AnyFunSpec {

  val controller = new PagesController

  describe("isRoot") {
    it("should be true only for the empty path") {
      assert(controller.isRoot(""))
      assert(!controller.isRoot("/"))
      assert(!controller.isRoot("index.html"))
    }
  }

  describe("joinPath") {
    it("should not prefix with a separator when the base is root") {
      assert(controller.joinPath("", "index.html") == "index.html")
    }
    it("should join a non-root base and suffix with a single slash") {
      assert(controller.joinPath("docs", "index.html") == "docs/index.html")
    }
    it("should not double up slashes when base ends with / or suffix starts with /") {
      assert(controller.joinPath("docs/", "/index.html") == "docs/index.html")
    }
  }

  describe("listProbablePages") {
    it("should try the path itself, then index.html, then index.htm, in that order") {
      assert(controller.listProbablePages("docs") == List("docs", "docs/index.html", "docs/index.htm"))
    }
    it("should resolve index files at the root without a leading slash") {
      assert(controller.listProbablePages("") == List("", "index.html", "index.htm"))
    }
  }

  describe("shouldRedirect") {
    it("should never redirect the root path") {
      assert(!controller.shouldRedirect("", "index.html", endsWithSlash = false))
    }
    it("should not redirect when the resolved path equals the requested path") {
      assert(!controller.shouldRedirect("index.html", "index.html", endsWithSlash = false))
    }
    it("should redirect when the resolved path is a deeper match and the request has no trailing slash") {
      assert(controller.shouldRedirect("docs", "docs/index.html", endsWithSlash = false))
    }
    it("should not redirect when the request already ends with a trailing slash") {
      assert(!controller.shouldRedirect("docs", "docs/index.html", endsWithSlash = true))
    }
    it("should not redirect when the resolved path is not a prefix match") {
      assert(!controller.shouldRedirect("docs", "other/index.html", endsWithSlash = false))
    }
  }
}
