# clj-libssh2

A Clojure wrapper around [libssh2](http://www.libssh2.org/). There are three
parts to this library:

1. A small API for doing most common SSH and SCP operations. You should use
   this for all new code using this library.
2. A [JNA](https://github.com/Chouser/clojure-jna) wrap of all of the
   functions and constants in the public API of libssh2. You should use this
   if you need to do something unusual. Please file an issue or PR with
   details of what you end up using this for, so that I can expand the main
   API.
3. A series of convenience functions designed to make this library a suitable
   replacement for [clj-ssh](https://github.com/hugoduncan/clj-ssh). You
   should use this if you're transitioning code from `clj-ssh` to this
   library.

## clj-libssh2 API

TODO: Examples

## libssh2 API

The namespaces are arranged as follows:

- `clj-libssh2.libssh2`
  - All constant definitions. Constants that started with `LIBSSH2_` have had
    that prefix removed.
  - `libssh2_banner_set`
  - `libssh2_base64_decode`
  - `libssh2_exit`
  - `libssh2_free`
  - `libssh2_hostkey_hash`
  - `libssh2_init`
  - `libssh2_poll`
  - `libssh2_poll_channel_read`
  - `libssh2_trace`
  - `libssh2_trace_sethandler`
  - `libssh2_version`
- `clj-libssh2.libssh2.agent` - All functions named `libssh2_agent_*`
- `clj-libssh2.libssh2.channel` - All functions named `libssh2_channel_*`
- `clj-libssh2.libssh2.keepalive` - All functions named `libssh2_keepalive_*`
- `clj-libssh2.libssh2.knownhost` - All functions named `libssh2_knownhost_*`
- `clj-libssh2.libssh2.scp` - All functions named `libssh2_scp_*`
- `clj-libssh2.libssh2.session` - All functions named `libssh2_session_*`
- `clj-libssh2.libssh2.userauth` - All functions named `libssh2_userauth_*`

## clj-ssh API

TODO. :)

## License

Copyright (c) 2015, Conor McDermottroe
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
