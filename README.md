# Upshot

The goal of upshot is to push mutability further to the edges of your progam. This library is extremely small as it's more about enforcing a pattern of development.

Upshot consists of 3 core parts which are:

- Commands
- Processor
- Handlers

Commands involve implementing a multimethod that then describes the actions you want to take place. The multimethod for a command is straightforward:

```clojure
(defmulti command
  (fn [k params]
      {:pre [(s/valid? ::command-key k)
             (s/valid? ::command-params params)]}
      k))
```

The command methods should be immutable and simply return the desired effect. For example, imagine we are creating users in our system so we might implement a method like the following:

```clojure
(defmethod myapp.user/create
  [_ {:keys [email first-name last-name]}]
  (let [tx-data (zipmap [:user/email :user/first-name :user/last-name]
                        [email first-name last-name])]
    [::command/ok :tx-data [tx-data]]))
```



Of course you may want to do other things inside of this command like validation or apply other business rules as needed. The end result however will be a vector that includes the command status along with list of handlers followed by the data required. This particular example includes only one handler.
