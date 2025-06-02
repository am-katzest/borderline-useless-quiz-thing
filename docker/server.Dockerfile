FROM clojure:tools-deps-1.12.0.1530-bookworm-slim
RUN mkdir model server
COPY model/deps.edn model/
RUN cd model; clj -P
COPY server/deps.edn server/
RUN cd server; clj -P
COPY model model
COPY server server
WORKDIR server
CMD clj -X:run-server
