(ns fluxo.config)

(def debug?
  ^boolean goog.DEBUG)

(def sablier-time-buffer
  "Sets the time buffer used to safely create a Sablier stream ahead of the
  blocktime."
  (* 10 60))

(def sablier-thegraph-ws-url
  "Sablier (Ropsten) websocket URL on TheGraph blockchain indexer GraphQL API.

  See https://thegraph.com/explorer/subgraph/sablierhq/sablier-ropsten"
  "wss://api.thegraph.com/subgraphs/name/sablierhq/sablier-ropsten")
