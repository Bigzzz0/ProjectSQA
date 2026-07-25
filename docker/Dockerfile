FROM ubuntu:20.04

ENV DEBIAN_FRONTEND=noninteractive

# ติดตั้ง Dependencies สำหรับ Java 8, Perl, Git, Python และการสั่งรัน Defects4J
RUN apt-get update && apt-get install -y \
    openjdk-8-jdk \
    git \
    perl \
    curl \
    wget \
    build-essential \
    python3 \
    python3-pip \
    zip \
    unzip \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

# Clone และ Setup Defects4J Framework
WORKDIR /opt
RUN git clone https://github.com/rjust/defects4j.git
WORKDIR /opt/defects4j
RUN ./init.sh
RUN ./framework/bin/defects4j sanity-check

ENV PATH=/opt/defects4j/framework/bin:$PATH

WORKDIR /workspace

CMD ["/bin/bash"]
