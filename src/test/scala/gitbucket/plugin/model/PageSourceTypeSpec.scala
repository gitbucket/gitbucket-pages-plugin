package gitbucket.plugin.model

import org.scalatest.funspec.AnyFunSpec

class PageSourceTypeSpec extends AnyFunSpec {

  describe("valueOf / valueOpt") {
    it("should round-trip every known code back to its PageSourceType") {
      PageSourceType.values.foreach { value =>
        assert(PageSourceType.valueOf(value.code) == value)
        assert(PageSourceType.valueOpt(value.code).contains(value))
      }
    }
    it("should return None from valueOpt for an unrecognized code") {
      assert(PageSourceType.valueOpt("bogus") == None)
    }
    it("should throw from valueOf for an unrecognized code") {
      assertThrows[NoSuchElementException] {
        PageSourceType.valueOf("bogus")
      }
    }
  }

  describe("fromCode") {
    it("should round-trip every known code back to its PageSourceType") {
      PageSourceType.values.foreach { value =>
        assert(PageSourceType.fromCode(value.code) == value)
      }
    }
    it("should fall back to GH_PAGES instead of throwing for an unrecognized code") {
      assert(PageSourceType.fromCode("bogus") == PageSourceType.GH_PAGES)
    }
  }
}
