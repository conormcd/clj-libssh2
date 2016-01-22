# clj-libssh2

A Clojure library for interacting with SSH servers using
[libssh2](http://www.libssh2.org/) under the hood. There are three parts to
this library:

1. A small API for doing most common SSH and SCP operations. You should use
   this for all new code using this library. Examples of usage can be found in
   [doc/Primary-API.md](https://github.com/conormcd/clj-libssh2/blob/master/doc/Primary-API.md).
2. A [JNA](https://github.com/Chouser/clojure-jna) wrap of all of the
   functions and constants in the public API of libssh2. You should use this
   if you need to do something unusual. Please [file an
   issue](https://github.com/conormcd/clj-libssh2/issues) or pull request with
   details of what you end up using this for, so that I can expand the main
   API. Some notes on this API can be found in
   [doc/libssh2.md](https://github.com/conormcd/clj-libssh2/blob/master/doc/libssh2.md).
3. A series of convenience functions designed to make this library a suitable
   replacement for [clj-ssh](https://github.com/hugoduncan/clj-ssh). You
   should use this if you're transitioning code from `clj-ssh` to this
   library.

Function by function documentation for the latest release should always be
available at: http://conormcd.github.io/clj-libssh2/

## Quick Start

```clojure
user=> (require '[clj-libssh2.ssh :as ssh])
nil
user=> (ssh/exec {:hostname "127.0.0.1"} "uptime")
{:out "18:40  up 155 days, 19:04, 4 users, load averages: 2.45 1.76 1.66\n",
:err "", :exit 0, :signal {:exit-signal nil, :err-msg nil, :lang-tag nil}}
```

See
[doc/Primary-API.md](https://github.com/conormcd/clj-libssh2/blob/master/doc/Primary-API.md)
for more examples.

## Release information

Releases are done automatically via
[CircleCI](https://circleci.com/gh/conormcd/clj-libssh2). Release builds can
be found on [Clojars](https://clojars.org/clj-libssh2) and on
[GitHub](https://github.com/conormcd/clj-libssh2/releases). Every release tags
this repository with the version number as well, so commands like `git log
0.1.69..0.1.72` and `git diff 0.1.69..0.1.72` should be usable to find out
what's happened between releases.

The latest release is:

![](https://clojars.org/clj-libssh2/latest-version.svg)

Version numbers are structured as follows: MAJOR.MINOR.BUILD-NUMBER. The
first, major version number will only be incremented for breaking changes to
the primary public API of this library. The second, minor number will be
incremented if either there's a backwards-incompatible change in other APIs
exposed by this library OR if the bundled version of libssh2 changes. The last
portion is the build number from CircleCI. Only green builds on the `master`
branch trigger a release so these numbers will be non-sequential and will not
reset to zero when the major/minor portions are incremented.

N.B. All versions before 0.2.x should be considered early development and
should not be used in production. APIs may be added/changed/removed without
warning.

## License

The majority of this library is released under [a two clause BSD-style
license](https://github.com/conormcd/clj-libssh2/blob/master/LICENSE). The
only exceptions to this are the bundled libraries which have their own
licenses:

- libsimplesocket - This is distributed under its own [two clause BSD-style
  license](https://github.com/conormcd/clj-libssh2/blob/master/resources/linux-x86-64/libsimplesocket.so.LICENSE)
- libssh2 - This is distributed under its own [three clause BSD-style
  license](https://github.com/conormcd/clj-libssh2/blob/master/resources/linux-x86-64/libssh2.so.COPYING)
