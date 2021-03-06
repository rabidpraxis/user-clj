Super basic user script for bootstrapping clojure project TDD sessions.

Add this to `$HOME/.clojure/deps.edn`

```clojure
{:aliases
 :tdd
 {:extra-deps {io.github.rabidpraxis/user-clj {:git/sha "2d895b45ddac1cc9e5e48f683cd2ba61e232b827"}}
  :main-opts ["-m" "user-clj.tdd"]}
```

Then in the project

```bash
clj -M:tdd
```

And a nrepl, automated test run, and portal session will begin.

One thing to note. The `:tdd` alias main assumes that you have a kaocha config
file, with the name `tdd.edn` at the root of your project. This is named
separately so you can tailor your TDD sessions.
