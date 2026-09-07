FROM node:20-alpine AS build

WORKDIR /build

# node:20-alpine's bundled npm has a known internal crash bug ("Exit handler
# never called!" - see npm/cli#4028 and related issues) that can kill an
# install partway through with no useful error. Upgrading npm first avoids
# it entirely.
RUN npm install -g npm@latest

COPY package.json package-lock.json ./
# --include=dev guarantees devDependencies (typescript, vite - both required
# just to run "npm run build") install regardless of NODE_ENV=production or
# an "omit=dev" npm config being set anywhere in the build environment -
# npm ci silently skips devDependencies under either of those, which is the
# classic cause of "tsc: not found" / "vite: not found" (exit 127) here.
RUN npm ci --include=dev

# Fails fast with a clear message here instead of a bare "exit 127" three
# steps later if devDependencies still didn't install for some other reason.
RUN test -x node_modules/.bin/tsc && test -x node_modules/.bin/vite \
    || (echo "ERROR: tsc/vite missing from node_modules/.bin after npm ci --include=dev - devDependencies did not install." && exit 1)

# .env's VITE_API_BASE_URL is baked in at build time - it's read by code
# that runs in the user's browser, not inside this container, so it must
# point at the gateway's host-published address (http://localhost:9090),
# not an in-network hostname.
COPY . .
RUN npm run build

FROM nginx:1.27-alpine

COPY --from=build /build/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
