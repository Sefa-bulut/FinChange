FROM node:20-alpine

WORKDIR /app

ENV CI=true

# Install dependencies first (use legacy peer deps)
COPY package.json package-lock.json* ./
RUN npm install --legacy-peer-deps

# Copy source
COPY . .

EXPOSE 3000

CMD ["npm", "start"]
