(ns frontend.core
  (:require [datascript.core :as d]
            [clojure.edn :as edn]
            [clojure.pprint :as pprint]
            [logseq.db :as ldb]
            [logseq.db.rules :as rules]
            [logseq.graph-parser :as gp]
            [logseq.graph-parser.config :as gp-config]))

(defn init []
  (.log js/console "hello!"))

(defn ^:export conn []
  (ldb/start-conn))

(defn ^:export read-config [config-text]
  (edn/read-string config-text))

(defn ^:export parse [conn path content {:keys [config] :as options}]
  (let [extract-options (merge {:date-formatter (gp-config/get-date-formatter config)
                                :user-config config}
                               (select-keys options [:verbose]))]
    (gp/parse-file conn path content {:extract-options extract-options})))

(defn ^:export query [conn q]
  (let [query (edn/read-string q)
        add-rules? (not (contains? (set query) :in))
        query' (if add-rules? (into query [:in '$ '%]) query)
        ;; _ (println (pr-str query'))
        res (map first
                 (apply d/q query' @conn
                        (if add-rules? [(vals rules/query-dsl-rules)] [])))]
    (pprint/pprint res)
    (clj->js res)))
