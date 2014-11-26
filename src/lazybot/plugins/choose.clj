(ns lazybot.plugins.choose
  (:require [lazybot.registry :as registry]
            [somnium.congomongo :refer [fetch fetch-one insert! destroy! update!]]))

(registry/defplugin
  (:cmd
   "Picks a random choice in a given category."
   #{"choose"} 
   (fn [{:keys [args] :as com-m}]
     (let [[category] args
           db (fetch-one :choose :where {:category category})]
       (if db
         (registry/send-message com-m (rand-nth (:items db)))
         (registry/send-message com-m "I have no choices to make. Please feed me some with addchoice!")))))
  (:cmd
   "Lists choices in a given category."
   #{"choices"} 
   (fn [{:keys [args] :as com-m}]
     (let [[category] args
           db (fetch-one :choose :where {:category category})]
       (if db
         (registry/send-message com-m (->> db :items (interpose " ") (apply str)))
         (registry/send-message com-m "No choices to make. Please feed me some with addchoice!")))))
  (:cmd
   "Adds a choice to the choice database."
   #{"addchoice"} 
   (fn [{:keys [args] :as com-m}]
     (if (seq args)
       (let [[category item] args
             existing (fetch-one :choose :where {:category category})]
         (update! :choose {:category category}
                  (update-in (or existing {:category category :items {}})
                             [:items]
                             #(conj (set %1) %2) item))
         (registry/send-message com-m (format "Added %s to %s" item category)))
       (registry/send-message com-m "Need to specify a category and item to add!"))))

  (:cmd
   "Removes a choice from the choice database."
   #{"rmchoice"} 
   (fn [{:keys [args] :as com-m}]
     (if (seq args)
       (let [[category item] args
             existing (fetch-one :choose :where {:category category})]
         (if existing
           (do (update! :choose {:category category}
                        (update-in existing
                                   [:items]
                                   #(disj (set %1) %2) item))
               (registry/send-message com-m (format "%s no longer in %s" item category)))
           (registry/send-message com-m (format "Category %s doesn't exist." category))))
       (registry/send-message com-m "Need to specify a category and item to remove!")))))

