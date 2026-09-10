FROM docker-defects4j-env:latest

ENV DEBIAN_FRONTEND=noninteractive

# ติดตั้ง OpenJDK 8 (คู่ขนานกับ Java 11), Perl dependencies และเครื่องมือ Build
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-8-jdk \
    subversion \
    build-essential \
    libdbi-perl \
    libdbd-csv-perl \
    libtemplate-perl \
    libxml-xpath-perl \
    && rm -rf /var/lib/apt/lists/*

# ตั้งค่า Default Java เป็น OpenJDK 11 (Defects4J CLI ต้องการ Java 11)
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:/opt/defects4j/framework/bin:$PATH

# ติดตั้ง Python Dependencies สำหรับ AI & Runner
RUN pip3 install --no-cache-dir requests tabulate

# Build และติดตั้ง Microsoft PICT Tool (สำหรับ Member 1: IPO Algorithm)
RUN git clone https://github.com/microsoft/pict.git /tmp/pict \
    && cd /tmp/pict \
    && make CXX="g++ -pthread" \
    && cp pict /usr/local/bin/ \
    && rm -rf /tmp/pict

# ดาวน์โหลด EvoSuite 1.0.6 และ Standalone Runtime (สำหรับ Member 2: MIO Algorithm)
RUN mkdir -p /opt/evosuite \
    && wget -q https://github.com/EvoSuite/evosuite/releases/download/v1.0.6/evosuite-1.0.6.jar -O /opt/evosuite/evosuite-1.0.6.jar \
    && wget -q https://github.com/EvoSuite/evosuite/releases/download/v1.0.6/evosuite-standalone-runtime-1.0.6.jar -O /opt/evosuite/evosuite-standalone-runtime-1.0.6.jar

WORKDIR /workspace

CMD ["/bin/bash"]
