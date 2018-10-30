(ns wasm-from-cljs.core
  (:require [aintegrant.core :as ag]
            [kitchen-async.promise :as p]
            [integrant.core :as ig]))

(enable-console-print!)

(defmethod ag/init-key :wasm [_ {:keys [path]} callback]
  (p/let [resp (js/fetch path)
          bytes (.arrayBuffer resp)
          result (js/WebAssembly.instantiate bytes #js {})]
    (callback (.. result -instance -exports))))

(defmethod ig/init-key :facts [_ {:keys [wasm]}]
  (mapv #(.fact wasm %) (range 10)))

(def config
  {:wasm {:path "/factorial.wasm"}
   :facts {:wasm (ig/ref :wasm)}})

(def system (atom nil))

(defn -main []
  (p/try
    (p/let [x ((p/promisify ag/init) config)]
      (js/alert (str "facts: " (:facts x)))
      (reset! system x))
    (p/catch :default e
      (js/console.log e))))

(.addEventListener js/window "load" -main)
