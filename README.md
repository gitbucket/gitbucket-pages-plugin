
# Gitbucket-Pages-Plugin

This plugin provides *Project Pages* for
[GitBucket](https://github.com/gitbucket/gitbucket).

## Notes

- view static web page in `gh-pages` branch at
  `<gitbucket base url>/<user>/<project>/pages/`
- no site generator will run (you must build & commit all html with
  assets if you use jekyll and the like)
- you kinda have to add links to pages in README or repository
  description, there's no way to provide handy link automatically for
  now
- only tested in standalone mode
- might be incompatible with absolute urls (eg: you use github project
  pages and assume your pages will aways live under `/<project/`)

## Installation

- download from [releases](https://github.com/yaroot/gitbucket-pages-plugin/releases)
- move the jar file to `<gitbucket_home>/plugins/` (`gitbucket_home` defaults to `~/.gitbucket`)
- restart gitbucket and enjoy

## Version compatibility

- 0.1: tested with gitbucket 3.9

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

    location ~ ^/([^/]+)/([^/]+)/(.*)$ {
        proxy_pass 127.0.0.1:8080;
    }

    location / {
        return 403;
    }
}
```

