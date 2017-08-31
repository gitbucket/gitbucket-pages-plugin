
# Gitbucket-Pages-Plugin [![Gitter](https://img.shields.io/gitter/room/gitbucket/gitbucket.js.svg?style=flat-square)](https://gitter.im/gitbucket/gitbucket) [![Travis](https://img.shields.io/travis/gitbucket/gitbucket-pages-plugin.svg?style=flat-square)](https://travis-ci.org/gitbucket/gitbucket-pages-plugin)

This plugin provides *Project Pages* functionality for
[GitBucket](https://github.com/gitbucket/gitbucket) based repositories.

## User guide

This plugin serves static files directly from one of the following
places:

- `gb-pages` branch (with fallback to `gh-pages` to be compatible with
  github, this is the default)
- `master` branch
- `docs` folder under `master` branch

### Quick start

- create a directory or branch if necessary (eg. create an orphan branch called `gb-pages`: `git checkout --orphan gb-pages && git rm -f $(git ls-files)`)
- create a static site under this branch. E.g. `echo '<h1>hello, world</h1>' > index.html` to create a simple file.
- commit && push to gitbucket this orphan branch
- open the browser and point to `<your repo url>/pages`

**Note**: This plugin won't render markdown content. To render markdown content, use the GitBucket Wiki functionality, or just one of the many static
site generators ([jekyll](http://jekyllrb.com/), [hugo](https://gohugo.io/), etc)

## Installation

- download from [releases](https://github.com/gitbucket/gitbucket-pages-plugin/releases)
- copy the jar file to `<GITBUCKET_HOME>/plugins/` (Note that `GITBUCKET_HOME` defaults to `~/.gitbucket` if not changed otherwise)
- restart gitbucket and enjoy

## Versions

| pages version | gitbucket version |
|     :---:     |       :---:       |
| 1.3           | 4.14.1            |
| 1.2           | 4.13              |
| 1.1           | 4.11              |
| 1.0           | 4.10              |
| 0.9           | 4.9               |
| 0.8           | 4.6               |
| 0.7           | 4.3 ~ 4.6         |
| 0.6           | 4.2.x             |
| 0.5           | 4.0, 4.1          |
| 0.4           | 3.13              |
| 0.3           | 3.12              |
| 0.2           | 3.11              |
| 0.1           | 3.9, 3.10         |


## Security (panic mode)

To prevent XSS, one must use two different domains to host the pages and
Gitbucket itself. Below is a working example of nginx configuration to achieve that.

```
server {
    listen 80;
    server_name git.local;

    location ~ ^/([^/]+)/([^/]+)/pages/(.*)$ {
        rewrite  ^/([^/]+)/([^/]+)/pages/(.*)$  http://doc.local/$1/$2/pages/$3  redirect;
    }

    location / {
        proxy_pass http://127.0.0.1:8080;
    }
}

server {
    listen 80;
    server_name doc.local;

    location ~ ^/([^/]+)/([^/]+)/pages/(.*)$ {
        proxy_pass http://127.0.0.1:8080;
    }

    location / {
        return 403;
    }
}
```

## CI

- build by [travis-ci](https://travis-ci.org/gitbucket/gitbucket-pages-plugin)

