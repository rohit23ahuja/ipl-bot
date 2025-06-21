#!/bin/bash
YOUR_DOMAIN="rohitahuja.dev"
YOUR_EMAIL="rohit23ahuja@gmail.com"

update_and_install(){
  sudo dnf update -y
  sudo dnf install git -y
  sudo dnf install java-21-amazon-corretto-devel -y
  sudo dnf install maven -y
  echo 'export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto' >> ~/.bash_profile
  echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bash_profile
  source ~/.bash_profile
  sudo dnf install -y awscli
  sudo dnf install -y postgresql
  java -version
  mvn -version
  git --version
  aws --version
  psql --version
  echo "packages updated and installed"
}

set_env_variable() {
  echo "Retrieving secrets key from Parameter Store..."
  AWS_DEFAULT_REGION=eu-west-2

  OPENAI_API_KEY=$(aws ssm get-parameter \
      --name "/laie-springai/openai-api-key" \
      --with-decryption \
      --query 'Parameter.Value' \
      --output text)

  DOCUMENTS_PATH=$(aws ssm get-parameter --name "/laie-springai/documents-path" --with-decryption \
        --query 'Parameter.Value' --output text)
  WEATHER_API_KEY=$(aws ssm get-parameter --name "/laie-springai/weather-api-key" --with-decryption \
        --query 'Parameter.Value' --output text)
  EMAIL_USERNAME=$(aws ssm get-parameter --name "/laie-springai/email-username" --with-decryption \
        --query 'Parameter.Value' --output text)
  EMAIL_PASSWORD=$(aws ssm get-parameter --name "/laie-springai/email-password" --with-decryption \
        --query 'Parameter.Value' --output text)
  DB_HOST=$(aws ssm get-parameter --name "/laie-springai/db-host" --with-decryption \
        --query 'Parameter.Value' --output text)
  DB_PASSWORD=$(aws ssm get-parameter --name "/laie-springai/db-password" --with-decryption \
        --query 'Parameter.Value' --output text)
  OPENAI_CHAT_MODEL=$(aws ssm get-parameter --name "/laie-springai/openai-chat-model" --with-decryption \
        --query 'Parameter.Value' --output text)

  # Persist to environment file
  cat<<EOF >> /etc/environment
  OPENAI_API_KEY=$OPENAI_API_KEY
  DOCUMENTS_PATH=$DOCUMENTS_PATH
  WEATHER_API_KEY=$WEATHER_API_KEY
  EMAIL_USERNAME=$EMAIL_USERNAME
  EMAIL_PASSWORD=$EMAIL_PASSWORD
  DB_HOST=$DB_HOST
  DB_PASSWORD=$DB_PASSWORD
  OPENAI_CHAT_MODEL=$OPENAI_CHAT_MODEL
EOF
  echo "Retrieved secrets from parameter store"
}

mcp_server_checkout_build_run() {
  cd /
  git clone https://github.com/rohit23ahuja/springai-mcp-sse-server.git
  sudo chown -R ec2-user:ec2-user springai-mcp-sse-server
  sudo chmod 777 -R springai-mcp-sse-server
  sudo chmod 777 -R springai-mcp-sse-server/
  cd springai-mcp-sse-server
  mvn clean package
  sudo java -jar target/springai-mcp-sse-server-0.0.1-SNAPSHOT.jar &
}

app_checkout_build_run() {
  cd /
  git clone https://github.com/rohit23ahuja/ipl-bot.git
  sudo chown -R ec2-user:ec2-user ipl-bot
  sudo chmod 777 -R ipl-bot
  sudo chmod 777 -R ipl-bot/
  cd ipl-bot
  mvn clean package
  sudo java -jar target/ipl-bot-0.0.1-SNAPSHOT.jar &
}

update_and_install
configure_ngix
set_env_variable
mcp_server_checkout_build_run
app_checkout_build_run
