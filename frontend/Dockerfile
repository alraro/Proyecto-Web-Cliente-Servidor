FROM nginx:alpine

RUN apk add --no-cache openssl \
	&& mkdir -p /etc/nginx/certs \
	&& openssl req -x509 -nodes -days 3650 -newkey rsa:2048 \
	-keyout /etc/nginx/certs/server.key \
	-out /etc/nginx/certs/server.crt \
	-subj "/C=ES/ST=Malaga/L=Malaga/O=Bancosol/OU=Dev/CN=localhost"

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY . /usr/share/nginx/html/

EXPOSE 80
EXPOSE 443