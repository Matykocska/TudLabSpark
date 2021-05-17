FROM gcr.io/datamechanics/spark-universal:3.1.1-dev0
ARG GOOGLE_CREDENTIALS_FILE

COPY target/scala-2.12/tlspark.jar .
COPY input_files/ input_files/
COPY credentials/$GOOGLE_CREDENTIALS_FILE credentials/

ENV GOOGLE_APPLICATION_CREDENTIALS=credentials/$GOOGLE_CREDENTIALS_FILE

CMD java -jar tlspark.jar