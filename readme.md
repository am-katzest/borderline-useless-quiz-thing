# borderline useless quiz thing

Fast, editable at runtime online quiz thing, made for a quiz that friend is
organizing sometimes.

It uses a custom optimistic updates & rollbacks based system which may or may
not work in practice. 

hosted nowhere because it's only ~halfway done in total.
* networking: finished
* server: finished
* questions: only abcd so far
* ui: started
* building: not started

## running tests

```bash
(cd model; clj -X:test)
(cd server; clj -X:test)
```

## building 

todo

## developement

```bash
cd server
clj -X:run-server # :port 8091
```

```bash
./start_nginx_dev
```

```bash
cd spa
npx shadow-cljs watch app
```

app should be avialable at port `8642`.

