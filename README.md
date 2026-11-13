
# Gitbucket-Pages-Plugin [![Gitter](https://img.shields.io/gitter/room/gitbucket/gitbucket.js.svg)](https://gitter.im/gitbucket/gitbucket) [![build](https://github.com/gitbucket/gitbucket-pages-plugin/actions/workflows/build.yml/badge.svg)](https://github.com/gitbucket/gitbucket-pages-plugin/actions/workflows/build.yml)

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

**Note**: This plugin won't render markdown content. To render markdown content, use the GitBucket Wiki functionality, or use one of the many static site generators (e.g. [jekyll](http://jekyllrb.com/), [hugo](https://gohugo.io/))

### Deploy from GitBucket CI

If you build your site with [gitbucket-ci-plugin](https://github.com/takezoe/gitbucket-ci-plugin),
the build can publish it to the `gb-pages` branch:

1. Pick a user to push with, ideally a dedicated one that is a collaborator (write access) only on
   the repositories it deploys, and generate a personal access token for it (Account settings → Applications).
2. Save the token in a file on the GitBucket server, readable only by the OS user GitBucket runs as.
3. In the repository, go to Settings → Build, choose the "Script" build type and adapt this script:

```sh
set -eu
SITE_DIR=public                    # where your build writes the site
SOURCE_BRANCH=main                 # deploy only builds of this branch
GB_URL=https://gitbucket.example.com
GB_USER=deployer                   # must be the owner of the token
TOKEN_FILE=/etc/gitbucket/pages-deploy-token

./build-site.sh                    # your site build

if [ "$CI_PULL_REQUEST" != false ] || [ "$CI_BUILD_BRANCH" != "$SOURCE_BRANCH" ]; then
  echo "Not deploying (branch $CI_BUILD_BRANCH, pull request $CI_PULL_REQUEST)"
  exit 0
fi
[ -f "$SITE_DIR/index.html" ] || { echo "No $SITE_DIR/index.html, not deploying"; exit 1; }

cd "$SITE_DIR"
rm -rf .git
git init -q
git checkout -q -b gb-pages
git add -A
git -c user.name=CI -c user.email=ci@localhost commit -q -m "Deploy $CI_COMMIT_ID [skip ci]"
export GB_USER TOKEN_FILE
git -c credential.helper= \
    -c credential.helper='!f() { echo "username=$GB_USER"; echo "password=$(cat "$TOKEN_FILE")"; }; f' \
    push -q --force "$GB_URL/git/$CI_REPO_SLUG.git" gb-pages
```

Every deploy replaces `gb-pages` with a single commit. The repository's Pages setting must be
"gh-pages branch" (the default), which serves `gb-pages`.

- `[skip ci]` in the commit message keeps the push from starting a build of `gb-pages`. It works as long as
  it's in the repository's CI skip words (it is by default).
- `Authentication failed` means a wrong token, a `GB_USER` that doesn't own it, or a user without write access.
- Branch protection on `gb-pages` rejects the force push.
- If GitBucket uses a certificate from a private CA, `git` in the build must trust it
  (system trust store, or `export GIT_SSL_CAINFO=/path/to/ca.crt` in the script).
- POSIX shell only: it doesn't work with the Docker build types or on Windows.

**Security**: builds run as the GitBucket OS user, so any build on the server can read the token file. That
includes other repositories' builds and pull request builds, which run code from the pull request (from forks
too, if you enabled fork pull request builds). Give the token's user no more access than it needs.

## Installation

**This plugin is bundled with newer version of GitBucket, for older
version please follow the instruction below**

### Install manually

- download from [releases](https://github.com/gitbucket/gitbucket-pages-plugin/releases)
- copy the jar file to `<GITBUCKET_HOME>/plugins/` (`GITBUCKET_HOME` defaults to `~/.gitbucket`)
- enable it in plugin settings (you may need to restart gitbucket)

## Versions

| pages version | gitbucket version |
|     :---:     |       :---:       |
| 1.11.0        | 4.38.0            |
| 1.10.0        | 4.36.0            |
| 1.9.0         | 4.35.0            |
| 1.8.0         | 4.32.0            |
| 1.7.0         | 4.23.0            |
| 1.6.0         | 4.19.0            |
| 1.5.0         | 4.15.0            |
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

- build by [GitHub Actions](https://github.com/gitbucket/gitbucket-pages-plugin/actions)
