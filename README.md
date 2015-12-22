This plugin provides *Project Pages* for
[GitBucket](https://github.com/gitbucket/gitbucket).

Notes:

- view static web page in `gh-pages` branch at
  `<gitbucket base url>/<user>/<project>/pages/`
- no site generator will run (you must build & commit all html with
  assets if you use jekyll and the like)
- you kinda have to add links to pages in README or repository
  description, there's no way to provide handy link automatically for
  now
- only tested in standalone mode

Installation:

- download from [releases](https://github.com/yaroot/gitbucket-pages-plugin/releases)
- move the jar file to `<gitbucket_home>/plugins/` (`gitbucket_home` defaults to `~/.gitbucket`)
- restart gitbucket and enjoy

Releases:

- 0.1: initial release, tested with gitbucket 3.9

