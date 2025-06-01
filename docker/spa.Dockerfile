FROM strategicblue/cljs-builder as build
RUN mkdir -p /opt/build/
WORKDIR /opt/build
RUN mkdir spa
RUN mkdir model
WORKDIR /opt/build/spa
RUN \
    --mount=type=cache,target=/opt/build/node_modules \
    npm install react
COPY spa/package.json ./
COPY spa/package-lock.json ./
RUN \
    --mount=type=cache,target=/opt/build/node_modules \
    npm install
COPY spa/ ./
COPY model/ /opt/build/model/
RUN \
    --mount=type=cache,target=/opt/build/node_modules \
    --mount=type=cache,target=/root/.m2 \
    --mount=type=cache,target=/opt/build/.shadow-cljs \
    npm run release
# thank you shadow-cljs for returning 0 even if compilation failed
RUN test -e resources/public/js/compiled/app.js
FROM "nginx:1.25.5-alpine" as ngnix
COPY --from=build  /opt/build/spa/resources/public /var/www/mnt
