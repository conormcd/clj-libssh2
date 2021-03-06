# The main clj-libssh2 API

The primary public API for this library is the following set of functions:

- `clj-libssh2.ssh/exec` - Execute a command on the remote host.
- `clj-libssh2.ssh/scp-from` - SCP a file from the remote host.
- `clj-libssh2.ssh/scp-to` - SCP a file to the remote host.
- `clj-libssh2.ssh/with-session` - A convenience macro for managing sessions.

If there are any breaking changes to the above-named functions after version
0.2.x of this library the major version number will be incremented.

## Examples

### Run a command and get the output.

```clojure
user=> (require '[clj-libssh2.ssh :as ssh])
nil
user=> (ssh/exec {:hostname "127.0.0.1"} "uptime")
{:out "18:40  up 155 days, 19:04, 4 users, load averages: 2.45 1.76 1.66\n",
:err "", :exit 0, :signal {:exit-signal nil, :err-msg nil, :lang-tag nil}}
```

### Run multiple commands on the same session

```clojure
user=> (require '[clj-libssh2.ssh :as ssh])
nil
user=> (ssh/with-session session {:hostname "127.0.0.1"}
  #_=>   (print (:out (ssh/exec session "uptime")))
  #_=>   (print (:out (ssh/exec session "date"))))
18:44  up 155 days, 19:07, 4 users, load averages: 2.17 1.93 1.75
Sun 17 Jan 2016 18:44:03 GMT
nil
```

### Pipe input to a process on the remote machine

```clojure
user=> (require '[clj-libssh2.ssh :as ssh])
nil
user=> (ssh/exec {:hostname "127.0.0.1"}
  #_=>           "while read -t 1 s; do echo s; done"
  #_=>           :in "foo\nbar\n")
{:out "foo\nbar\n", :err "", :exit 0, :signal {:exit-signal nil, :err-msg nil,
:lang-tag nil}}
```

### Copy a file from a remote machine.

```clojure
user=> (require '[clj-libssh2.ssh :as ssh])
nil
user=> (ssh/scp-from {:hostname "127.0.0.1"} "/home/conor/.vimrc" ".vimrc")
{:local-path ".vimrc", :remote-path "/home/conor/.vimrc", :size 4525,
:remote-stat {:atime #object[java.time.Instant 0x3fc52110
"2016-01-30T23:16:16Z"], :ctime #object[java.time.Instant 0x6de7c794
"1970-01-01T00:00:00Z"], :gid 0, :mode 420, :mtime #object[java.time.Instant
0x3e81f5e4 "2016-01-30T17:58:11Z"], :size 4525, :uid 0}}
```

### Send a file to a remote machine.

```clojure
user=> (require '[clj-libssh2.ssh :as ssh])
nil
user=> (ssh/scp-to {:hostname "127.0.0.1"} "/home/conor/.vimrc" "/tmp/.vimrc")
nil
```
