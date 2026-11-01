FROM node:22-bookworm-slim
WORKDIR /app
COPY backend-node/package.json backend-node/package-lock.json ./
RUN npm ci --omit=dev
COPY backend-node/src ./src
COPY backend/openapi.json ./contract/openapi.json
ENV CONTRACT_PATH=/app/contract/openapi.json
ENV NODE_ENV=production
ENV PORT=8088
USER node
EXPOSE 8088
CMD ["node", "src/server.js"]
