(ns clj-libssh2.logging
  "Functions for weaving some detailed debug logging into every function in
   clj-libssh2."
  (:require [clojure.tools.logging :as log]
            [clojure.pprint :refer [pprint]]
            [robert.hooke :as hook]))

(defn- spy-fn
  "Create a function that can be used as a hook to log the call and result from
   a given function. This is somewhat similar to the log/spy macro in
   clojure.tools.logging but that doesn't work too neatly with robert.hooke.

   Arguments:

   fn-var A var pointing to the function we'll be tracing.
   level  The level at which the messages should be logged.

   Return:

   A function of the form `(fn [f & args])` which can be given to
   robert.hooke/add-hook to hook a function."
  [fn-var level]
  (let [fn-meta (meta fn-var)
        fn-name (symbol (str (ns-name (:ns fn-meta))) (str (:name fn-meta)))
        make-message (fn [args result]
                       (format "%s => %s"
                               (with-out-str (pprint (cons fn-name args)))
                               (with-out-str (pprint result))))]
    (fn [f & args]
      (try
        (let [result (apply f args)]
          (log/log level (make-message args result))
          result)
        (catch Throwable t
          (log/log level t (make-message args t))
          (throw t))))))

(defn trace-fn
  "Hook a function so that all calls to it (and the results of those calls) are
   logged at a given level.

   Arguments:

   fn-var A var pointing to the function to trace.
   level  The log level at which the function call and result should be logged.

   Return:

   See robert.hooke/add-hook for the description of the return value."
  [fn-var level]
  (hook/add-hook fn-var (spy-fn fn-var level)))

(defn trace-ns
  "Apply (trace-fn % level) to all functions in a given namespace.

   Arguments:

   ns-sym The symbolic name of a namespace.
   level  The level to log all calls at.

   Return:

   nil"
  [ns-sym level]
  (doseq [fn-var (filter #(fn? (var-get %)) (vals (ns-interns ns-sym)))]
    (trace-fn fn-var level)))

(defn- init*
  "Initialise the logging of clj-libssh2 by applying trace-ns to all of the
   namespaces in the library if debug logging is enabled."
  []
  (doseq [nmspc (all-ns)]
    (when (and (not (= 'clj-libssh2.logging (ns-name nmspc)))
               (re-find #"^clj-libssh2\." (str (ns-name nmspc))))
      (trace-ns nmspc :trace))))

(def ^{:arglists '([])} init
  "Initialise the logging of clj-libssh2 by applying trace-ns to all of the
   namespaces in the library if debug logging is enabled."
  (memoize init*))
