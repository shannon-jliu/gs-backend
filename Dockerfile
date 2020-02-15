FROM gradle:jdk13

WORKDIR /home/gs-backend

# install watchman to watch the src directory for changes
RUN su - \
  && git clone https://github.com/facebook/watchman.git \
  && cd watchman/ \
  && git checkout v4.9.0 \
  && apt-get update \
  && apt-get install -y autoconf automake build-essential libtool libssl-dev pkg-config python-dev \
  && ./autogen.sh && ./configure \
  && make && make install

# set up a trigger named "recompile" to rebuild every time a file changes
CMD watchman -- trigger /home/gs-backend/src recompile -- /home/gs-backend/recompile \
    && ./run
