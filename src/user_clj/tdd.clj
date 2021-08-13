(ns user-clj.tdd
  (:require
    [nrepl.cmdline]
    [cognitect.rebl]
    [portal.api :as p]
    [clojure.java.shell :refer [sh]]
    [clojure.edn :as edn]
    [kaocha.stacktrace]
    [kaocha.repl]
    [kaocha.watch]
    [io.aviso.repl]))

(defn pre-run [config]
  config)

(defn test-config [config]
  (io.aviso.repl/install-pretty-exceptions)
  config)

(defn inspect [name & args]
  (apply cognitect.rebl/submit name args))

(defn pre-load [config]
  (alter-var-root
    #'kaocha.stacktrace/print-cause-trace
    (constantly io.aviso.repl/pretty-print-stack-trace))

  config)

(def readers
  {'object pr-str
   'function pr-str})

(defn post-run [config]
  (tap> {:test-config config})
  (let [tests (-> config :kaocha.result/tests first :kaocha.result/tests)]
    (doseq [t tests]
      (when-let [failed (seq (filter #(when-let [failct (:kaocha.result/fail %)]
                                        (> failct 0))
                                     (:kaocha.result/tests t)))]
        (tap> {:failed-test t})
        (doseq [f failed]
          (let [strs (-> (:kaocha.plugin.capture-output/output f)
                         (clojure.string/split #"\n"))]
            (doseq [s strs]
              (try (let [conv (edn/read-string {:readers readers} s)]
                     (if (map? conv)
                       (tap> {(first (keys conv)) (first (vals conv))})
                       (tap> conv)))
                   (catch Exception ex
                     (tap> {:error {:err ex :string s}})))))))))
  config)

(defn testing
  []
  (kaocha.watch/run
    (kaocha.repl/config {:config-file "tdd.edn"})))

(defn start-portal
  [_ _ _]
  (io.aviso.repl/install-pretty-exceptions)
  (add-tap #'p/submit)
  (p/open)
  (testing))

(defn -main [& _args]
  (nrepl.cmdline/-main
    "--interactive"
    "--repl-fn" "user-clj.tdd/start-portal"
    "--middleware" "[cider.nrepl/cider-middleware,nrebl.middleware/wrap-nrebl]"))
