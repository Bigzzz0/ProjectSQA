FROM ubuntu:20.04@sha256:8feb4d8ca5354def3d8fce243717141ce31e2c428701f6682bd2fafe15388214

ARG DEBIAN_FRONTEND=noninteractive
ARG DEFECTS4J_REF=8c16da8230843cdc918eaf4ddb449637f02b83c6
ARG PICT_REF=ab76c2548f551fcb46314e58653a1bd1172f3a72

ENV TZ=Asia/Bangkok \
    JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 \
    PATH=/usr/lib/jvm/java-11-openjdk-amd64/bin:/opt/defects4j/framework/bin:/usr/lib/jvm/java-8-openjdk-amd64/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

# Defects4J 3.x runs on Java 11. Java 8 remains installed for MIO generation.
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential ca-certificates cpanminus curl git libdbi-perl \
    libdbd-csv-perl libtemplate-perl libxml-xpath-perl openjdk-11-jdk \
    openjdk-8-jdk perl python3 python3-pip subversion unzip wget zip \
    && rm -rf /var/lib/apt/lists/*

# Pin the Defects4J checkout used to build the benchmark catalog and image.
RUN git clone https://github.com/rjust/defects4j.git /opt/defects4j \
    && git -C /opt/defects4j checkout "${DEFECTS4J_REF}" \
    && cpanm --installdeps -n /opt/defects4j \
    && cd /opt/defects4j \
    && ./init.sh

RUN pip3 install --no-cache-dir requests tabulate

# PICT is retained as an optional reference backend, separate from Native IPO.
RUN git clone https://github.com/microsoft/pict.git /tmp/pict \
    && git -C /tmp/pict checkout "${PICT_REF}" \
    && make -C /tmp/pict CXX="g++ -pthread" \
    && install /tmp/pict/pict /usr/local/bin/pict \
    && rm -rf /tmp/pict

# Keep the generation toolchain version and its downloaded JARs verifiable.
RUN mkdir -p /opt/evosuite \
    && wget -q https://github.com/EvoSuite/evosuite/releases/download/v1.0.6/evosuite-1.0.6.jar -O /opt/evosuite/evosuite-1.0.6.jar \
    && wget -q https://github.com/EvoSuite/evosuite/releases/download/v1.0.6/evosuite-standalone-runtime-1.0.6.jar -O /opt/evosuite/evosuite-standalone-runtime-1.0.6.jar \
    && echo '430304e3265b29edec710e36c5aaff0b7762ecc7afd8967a9335e89412e1b9dd  /opt/evosuite/evosuite-1.0.6.jar' | sha256sum -c - \
    && echo '0071b194ac5a5bc156dd2a7313624e9a0ff2ab7e618c9ca7fa841b98115ec48b  /opt/evosuite/evosuite-standalone-runtime-1.0.6.jar' | sha256sum -c -

WORKDIR /workspace
CMD ["/bin/bash"]
