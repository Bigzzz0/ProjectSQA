FROM ubuntu:20.04

ENV DEBIAN_FRONTEND=noninteractive

# ติดตั้ง Dependencies สำหรับ Java 8, Perl, Git, Python และการสั่งรัน Defects4J
RUN apt-get update && apt-get install -y \
    openjdk-11-jdk \
    git \
    perl \
    cpanminus \
    libdbi-perl \
    libdbd-csv-perl \
    libtemplate-perl \
    libxml-xpath-perl \
    curl \
    wget \
    build-essential \
    python3 \
    python3-pip \
    zip \
    unzip \
    ca-certificates \
    && cpanm -n String::Interpolate DBI DBD::CSV \
    && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

# Clone และ Setup Defects4J Framework (Bypass init.sh 600MB ด้วยการ clone commons-lang จาก GitHub โดยตรง)
WORKDIR /opt
RUN git clone https://github.com/rjust/defects4j.git
WORKDIR /opt/defects4j
RUN mkdir -p project_repos && \
    git clone --bare https://github.com/apache/commons-lang.git project_repos/commons-lang.git && \
    mkdir -p framework/lib/test_generation/generation framework/lib/test_generation/runtime framework/lib/build_systems/gradle
ENV PATH=/opt/defects4j/framework/bin:$PATH

WORKDIR /workspace

CMD ["/bin/bash"]
