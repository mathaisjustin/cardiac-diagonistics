FROM node:20-alpine AS build

WORKDIR /build

# Optional corporate/proxy root CA. If you're building behind a
# TLS-intercepting network (common on managed/corporate machines) you'll
# hit "UNABLE_TO_GET_ISSUER_CERT_LOCALLY" talking to registry.npmjs.org -
# export your org's root CA (Base-64 X.509 .pem/.crt) and save it as
# corporate-ca.pem right here in cardiac-frontend-service/ (gitignored,
# never commit it - it's specific to your machine/network). The wildcard
# COPY below is a no-op if that file doesn't exist, so this is harmless
# for anyone not behind such a proxy.
COPY corporate-ca.pem* ./
RUN if [ -f corporate-ca.pem ]; then \
      npm config set cafile "$(pwd)/corporate-ca.pem" --global; \
    fi

# node:20-alpine's bundled npm has a known internal crash bug ("Exit handler
# never called!" - see npm/cli#4028 and related issues) that can kill an
# install partway through with no useful error. Upgrading npm first avoids
# it entirely - pinned to the 10.x line (not "latest"/npm@12, which requires
# Node >=22 and fails with EBADENGINE against this image's Node 20).
RUN npm install -g npm@10

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
