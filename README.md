# Upshot

The goal of upshot is to push mutability further to the edges of your progam. This library is extremely small as it's more about enforcing a pattern of development.


## Why?

Force you into patterns that make it easy to write testable code. Utilizing upshot you will be responsible for writing commands, which should be pure functions, and handlers which will deal with mutating state. In general you will have very few handlers and many commands. If you can verify that the output of your commands is correct and that your handlers function with valid input then you can be assured that this part of your program will be solid.

## How?

Upshot consists of 3 core parts which are:

- Commands
- Handlers
- Processor

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

Of course you may want to do other things inside of this command like validation or apply other business rules as needed. The end result however will be a vector that includes the command status along with list of handlers followed by a vector of the parameters required. This particular example includes only one handler `:tx-data`. This makes it simple to write fast and simple tests that can verify that your commands work the way you intend.

As mentioned above, it is possible for a command to return any number of handlers that should run. Taking the example above, suppose we want to send an email upon creation of the user. We might then have the result of the command look like the following:

``` clojure
(defmethod myapp.user/create
  [_ {:keys [email first-name last-name]}]
  (let [tx-data (zipmap [:user/email :user/first-name :user/last-name]
                        [email first-name last-name])]
    [::command/ok :tx-data [tx-data]
                  :send-email [{:to email
                                :from "support@myapp.com"
                                :subject "Thanks for registering!"
                                :message "Welcome to myapp!"}]]))
```

Handlers are simply functions that take 2 parameters and return a status and result map. An example handler for the tx-data function might look like the following:

```clojure
(defn tx-data-handler
  [env data]
  (let [tx-result (d/transact! (:conn env) {:tx-data [data]})]
    [::handler/ok tx-result]))
```

That last piece of the puzzle is the processor that takes the result of a command operation and actually processes the side effects. The creation of the processor requires a map of key -> handler functions.

``` clojure
(def handlers
  {:tx-data #'tx-data-handler})

(def command-processor (upshot.core/processor handlers))

(->> (command :myapp.user/create {:email "email@domain.com"
                                  :first-name "firstname"
                                  :last-name "lastname"})
     (command-processor {:conn conn}))
```

It is important to know that the processor will process the results of the command syncronously in order that they appear in the vector returned by the command. If any handler fails the operation will be short-circuited and return with an error status and a map describing the error. In the example above of creating the user then sending the email, if the email handler were to fail we still would have created the user record as there is no type of wrapping transaction support at this point.
