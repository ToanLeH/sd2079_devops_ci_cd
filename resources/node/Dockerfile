ARG BASEIMG
ARG IMG_VERSION
FROM $BASEIMG:$IMG_VERSION as build
WORKDIR /usr/src/app
COPY package.json /usr/src/app
COPY package-lock.json /usr/src/app
RUN npm ci
COPY . /usr/src/app
EXPOSE 3000
CMD ["npm", "start"]