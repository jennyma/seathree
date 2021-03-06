(ns seathree.twitter
  (use twitter.oauth
       twitter.api.restful))

(defn creds-from-cfg
  "Given a config map as generated by seathree.config/get-cfg, produce
   an oauth creds map to pass to the twitter api"
  [cfg]
  (let [twitter-creds (:oauth (:twitter cfg))]
             (make-oauth-creds (:consumer-key        twitter-creds)
                               (:consumer-secret      twitter-creds)
                               (:access-token        twitter-creds)
                               (:access-token-secret twitter-creds))))
  
(defn match [re s] (not (nil? (re-seq re s))))

(defn extract-tweets-from-body
  "Given the body of a response from the twitter api, extract just a
   list of the raw tweets"
  [body]
  (map :text body))

(defn get-statuses
  "Given a twitter oauth creds map, a username, the id after which to
   fetch tweets, and a number of retires, hit the twitter API and return
   the fetched tweets"
  [creds username since-id retries]
  (if (= 0 retries)
    nil
    (let [base-params   {:screen-name username :include-rts false}
          params        (if since-id (assoc base-params :since-id since-id) base-params)
          response      (statuses-user-timeline :oauth-creds creds :params params)
          status-string (format "%d" (:code (:status response)))] 
      (condp match status-string
        #"^[45]" (do
                    (println "Twitter threw a" status-string "for" username ":" (:msg (:status response)))
                    (get-statuses username since-id (- retries 1)))
        #"^2"    (extract-tweets-from-body (:body response))))))
