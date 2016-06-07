
# Gitbucket-Pages-Plugin [![Gitter](https://img.shields.io/gitter/room/gitbucket/gitbucket.js.svg?style=flat-square)](https://gitter.im/gitbucket/gitbucket) [![Travis](https://img.shields.io/travis/yaroot/gitbucket-pages-plugin.svg?style=flat-square)](https://travis-ci.org/yaroot/gitbucket-pages-plugin)

This plugin provides *Project Pages* for
[GitBucket](https://github.com/gitbucket/gitbucket).

## User documentation

This plugin would serve static file under `gh-pages` branch under
`<base url>/<user>/<project>/pages/`.

### Quick start

- checkout orphan branch via `git checkout --orphan && git rm -f $(git ls-files)`
- `echo '<h1>hello, world</h1>' > index.html`
- commit && push to gitbuck
- open browser and point to `<your repo url>/pages`

**note**: this plugin won't render markdown for you, pre-build the
site or just use wiki

## Installation

- download from [releases](https://github.com/yaroot/gitbucket-pages-plugin/releases)
- move the jar file to `<gitbucket_home>/plugins/` (`gitbucket_home` defaults to `~/.gitbucket`)
- restart gitbucket and enjoy

## Versions

| pages version | gitbucket version |
|     :---:     |       :---:       |
| 0.5           | 4.0, 4.1          |
| 0.4           | 3.13              |
| 0.3           | 3.12              |
| 0.2           | 3.11              |
| 0.1           | 3.9, 3.10         |


## Security (panic mode)

To prevent XSS, one must use two different domain to host pages and
gitbucket. Below is a working example of nginx config to achieve that.

```
server {
    listen 80;
    server_name git.local;

    location ~ ^/([^/]+)/([^/]+)/pages/(.*)$ {
        rewrite  ^/([^/]+)/([^/]+)/pages/(.*)$  http://doc.local/$1/$2/pages/$3  redirect;
    }

    location / {
        proxy_pass 127.0.0.1:8080;
    }
}

server {
    listen 80;
    server_name doc.local;

    location ~ ^/([^/]+)/([^/]+)/pages/(.*)$ {
        proxy_pass 127.0.0.1:8080;
    }

    location / {
        return 403;
    }
}
```

