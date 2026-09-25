FROM ubuntu:20.04

ENV DEBIAN_FRONTEND=noninteractive

# 1. ติดตั้ง Dependencies พื้นฐาน, Java 8, Java 11, Perl, Python และเครื่องมือ Build สำหรับ Defects4J
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-8-jdk \
    openjdk-11-jdk \
    git \
    subversion \
    perl \
    cpanminus \
    build-essential \
    python3 \
    python3-pip \
    curl \
    wget \
    unzip \
    zip \
    ca-certificates \
    libdbi-perl \
    libdbd-csv-perl \
    libtemplate-perl \
    libxml-xpath-perl \
    && rm -rf /var/lib/apt/lists/*

# ตั้งค่า Default Java เป็น OpenJDK 11 (Defects4J CLI ต้องการ Java 11)
RUN update-java-alternatives -s java-1.11.0-openjdk-amd64 || true
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:/opt/defects4j/framework/bin:$PATH

# 2. ติดตั้ง Perl Dependencies สำหรับ Defects4J
RUN cpanm -n String::Interpolate DBI DBD::CSV

# 3. ติดตั้ง Python Dependencies สำหรับ AI & Universal Runner
RUN pip3 install --no-cache-dir requests tabulate

# 4. ดาวน์โหลดและตั้งค่า Defects4J Framework
WORKDIR /opt
RUN git clone https://github.com/rjust/defects4j.git /opt/defects4j
WORKDIR /opt/defects4j
RUN cpanm --installdeps . && ./init.sh

# ตรวจสอบและสร้าง symlink junit-4.12.jar สำหรับ Ant dependencies
RUN [ -f /opt/defects4j/framework/projects/lib/junit-4.12.jar ] || \
    ln -sf /opt/defects4j/framework/projects/lib/junit-4.12-hamcrest-1.3.jar /opt/defects4j/framework/projects/lib/junit-4.12.jar || true

# 5. Build และติดตั้ง Microsoft PICT Tool (สำหรับ Member 1: IPO Algorithm)
RUN git clone https://github.com/microsoft/pict.git /tmp/pict \
    && cd /tmp/pict \
    && make CXX="g++ -pthread" \
    && cp pict /usr/local/bin/ \
    && rm -rf /tmp/pict

# 6. ดาวน์โหลด EvoSuite 1.0.6 และ Standalone Runtime (สำหรับ Member 2: MIO Algorithm)
RUN mkdir -p /opt/evosuite \
    && wget -q https://github.com/EvoSuite/evosuite/releases/download/v1.0.6/evosuite-1.0.6.jar -O /opt/evosuite/evosuite-1.0.6.jar \
    && wget -q https://github.com/EvoSuite/evosuite/releases/download/v1.0.6/evosuite-standalone-runtime-1.0.6.jar -O /opt/evosuite/evosuite-standalone-runtime-1.0.6.jar

WORKDIR /workspace

CMD ["/bin/bash"]
