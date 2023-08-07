FROM azul/prime:17
# FROM azul/zulu-openjdk-alpine:17-latest

COPY ./server/target/pack/ /PSF-LoginServer

EXPOSE 51000/udp
EXPOSE 51001/udp
EXPOSE 51002/tcp

CMD ["/PSF-LoginServer/bin/psforever-server"]

