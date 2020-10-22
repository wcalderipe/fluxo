(ns fluxo.models.add-token-test
  (:require [cljs.test :refer-macros [testing is use-fixtures]]
            [cljsjs.react]
            [day8.re-frame.test :refer [run-test-sync]]
            [devcards.core :refer-macros [deftest]]
            [fluxo.models.add-token :as model]
            [fluxo.test-helper :refer [fixture-re-frame]]
            [re-frame.core :as rf]))

(use-fixtures :each (fixture-re-frame))

(deftest on-submit-test
  (run-test-sync
   (let [assets       (rf/subscribe [:wallet/assets])
         active-route (rf/subscribe [:routes/active])]

     (rf/dispatch [::model/on-submit {:symbol  "VIN"
                                      :address "0xViN"
                                      :name    "Vinho"}])

     (testing "adds the asset in the wallet"
       (is (= [{:symbol  "VIN"
                :address "0xViN"
                :name    "Vinho"}] @assets)))

     (testing "redirects to the amount step"
       (is (= :create-stream/amount @active-route))))))
