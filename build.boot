(set-env!
  :source-paths #{"src"}
  :resource-paths #{"resources"}
  :dependencies '[[org.clojure/clojure "1.8.0-RC2"]
                  [org.clojure/clojurescript "1.7.170"]

                  [adzerk/boot-reload "0.4.2" :scope "test"]
                  [adzerk/boot-cljs "1.7.170-3" :scope "test"]
                  [adzerk/boot-cljs-repl   "0.3.0"]
                  [com.cemerick/piggieback "0.2.1"  :scope "test"]
                  [weasel                  "0.7.0"  :scope "test"]
                  [org.clojure/tools.nrepl "0.2.12" :scope "test"]

                  [org.omcljs/om "1.0.0-alpha22"]])

(require '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs :refer :all]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]])

(defn- generate-lein-project-file! [& {:keys [keep-project] :or {:keep-project true}}]
       (require 'clojure.java.io)
       (let [pfile ((resolve 'clojure.java.io/file) "project.clj")
             ; Only works when pom options are set using task-options!
             {:keys [project version]} (:task-options (meta #'boot.task.built-in/pom))
             prop #(when-let [x (get-env %2)] [%1 x])
             head (list* 'defproject (or project 'boot-project) (or version "0.0.0-SNAPSHOT")
                         (concat
                           (prop :url :url)
                           (prop :license :license)
                           (prop :description :description)
                           [:dependencies (get-env :dependencies)
                            :source-paths (vec (concat (get-env :source-paths)
                                                       (get-env :resource-paths)))]))
             proj (pp-str head)]
            (if-not keep-project (.deleteOnExit pfile))
            (spit pfile proj)))

(deftask lein-generate
         "Generate a leiningen `project.clj` file.
          This task generates a leiningen `project.clj` file based on the boot
          environment configuration, including project name and version (generated
          if not present), dependencies, and source paths. Additional keys may be added
          to the generated `project.clj` file by specifying a `:lein` key in the boot
          environment whose value is a map of keys-value pairs to add to `project.clj`."
         []
         (generate-lein-project-file! :keep-project true))

(deftask dev []
         (comp
           (watch)
           (reload)
           (cljs-repl)
           (cljs)))
