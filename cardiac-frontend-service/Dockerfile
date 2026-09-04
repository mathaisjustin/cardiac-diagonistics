FROM node:20-alpine AS build

WORKDIR /build

COPY package.json package-lock.json ./
RUN npm ci

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
