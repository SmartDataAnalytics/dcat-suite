FROM aksw/rpt AS rpt

FROM ubuntu:22.04
MAINTAINER Claus Stadler <cstadler@informatik.uni-leipzig.de>

ENV DEBIAN_FRONTEND noninteractive


# Install packages http://wiki.openstreetmap.org/wiki/Nominatim/Installation#Ubuntu.2FDebian
# Replacements: libgeos-c1 -> libgeos-c1v5

RUN \
    apt-get -y update --fix-missing && \
#    apt-get upgrade -y && \
    apt-get install -y software-properties-common wget git curl sudo inotify-tools xmlstarlet openjdk-17-jdk && \
#    add-apt-repository "deb http://apt.postgresql.org/pub/repos/apt/ trusty-pgdg main" && \
#    wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | apt-key add - && \
    apt-get update


# RUN apt-get install -y build-essential libxml2-dev libpq-dev libbz2-dev libtool automake libproj-dev libboost-dev libboost-system-dev libboost-filesystem-dev libboost-thread-dev libexpat-dev gcc proj-bin libgeos-c1v5 libgeos++-dev libexpat-dev php php-pear php-pgsql php-json php-db libapache2-mod-php postgresql postgis postgresql-contrib postgresql-${POSTGRES_VERSION}-postgis-${POSTGIS_VERSION} postgresql-server-dev-${POSTGRES_VERSION} wget

COPY --from=rpt /app /rpt

# Adapt the classpath paths to the new location (-i = in place)
RUN sed -i -E 's|(^\|:)/app/|\1/rpt/|g' /rpt/jib-classpath-file
RUN echo 'java -cp @/rpt/jib-classpath-file @/rpt/jib-main-class-file "$@"' > /bin/rpt && chmod +x /bin/rpt

WORKDIR /app/mvn-sync

ADD run-dcat-mvn-sync.sh .
ADD sync-mvn.sh .
ADD dcat-mvn-id.sh .

ENTRYPOINT ["./run-dcat-mvn-sync.sh"]
CMD ["/repository"]

# Nominatim install
# RUN git clone --recursive https://github.com/openstreetmap/Nominatim ./src
# RUN cd ./src && git checkout $NOMINATIM_VERSION && git submodule update --recursive --init && \
#  ./autogen.sh && ./configure && make


