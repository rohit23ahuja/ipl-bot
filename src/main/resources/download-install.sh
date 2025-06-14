#!/bin/bash
YOUR_DOMAIN="rohitahuja.dev"
YOUR_EMAIL="rohit23ahuja@gmail.com"

update_and_install(){
  sudo dnf update -y
  sudo dnf install git -y
  sudo dnf install java-21-amazon-corretto-devel -y
  echo "Successfully retrieved API key"
  echo 'export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto' >> ~/.bash_profile
  echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bash_profile
  source ~/.bash_profile
  sudo dnf install maven -y
  sudo dnf install -y awscli
  sudo dnf install nginx -y
  sudo dnf install -y certbot python3-certbot-nginx
  java -version
  mvn -version
  git --version
  aws --version
}
# Create the Nginx configuration file for your application
# This configures Nginx to listen on port 80 for the initial Certbot challenge
# and then act as a reverse proxy for your application.
configure_nginx() {
  cat <<EOF > /etc/nginx/conf.d/${YOUR_DOMAIN}.conf
  server {
      listen 80;
      server_name ${YOUR_DOMAIN};

      # This location is used by Certbot for domain validation
      location /.well-known/acme-challenge/ {
          root /usr/share/nginx/html;
      }

      # After validation, Certbot will update this block to redirect all HTTP to HTTPS
      location / {
          return 301 https://\$host\$request_uri;
      }
  }
EOF
# Enable and start Nginx service temporarily for Certbot
  systemctl enable nginx
  systemctl start nginx
# Make sure your app is running on localhost:${APP_PORT} before proceeding.
# Request SSL Certificate from Let's Encrypt using Certbot
# The --nginx flag automatically modifies the Nginx config for SSL.
# The --non-interactive flag makes it run without manual prompts.
  certbot --nginx --non-interactive --agree-tos --email ${YOUR_EMAIL} --domain ${YOUR_DOMAIN}
# Certbot automatically modifies the Nginx config to add the SSL server block.
# Now, we will add the proxy_pass configuration to the new HTTPS server block.
# This is a robust way to inject the proxy configuration into the SSL server block created by Certbot.
  awk -v app_port="$APP_PORT" '
/listen 443 ssl;/ {
    print;
    print "    location / {";
    print "        proxy_pass http://127.0.0.1:" app_port ";";
    print "        proxy_set_header Host \$host;";
    print "        proxy_set_header X-Real-IP \$remote_addr;";
    print "        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;";
    print "        proxy_set_header X-Forwarded-Proto \$scheme;";
    print "    }";
    next;
}
/location \/ \{/ {
    in_location_block = 1;
}
/}/ {
    if (in_location_block) {
        in_location_block = 0;
        next;
    }
}
!in_location_block {
    print;
}
' /etc/nginx/conf.d/${YOUR_DOMAIN}.conf > /etc/nginx/conf.d/${YOUR_DOMAIN}.conf.tmp && mv /etc/nginx/conf.d/${YOUR_DOMAIN}.conf.tmp /etc/nginx/conf.d/${YOUR_DOMAIN}.conf

# Test Nginx configuration and restart the service to apply all changes
nginx -t
systemctl restart nginx

# Enable the Certbot renewal timer to run automatically
systemctl enable certbot-renew.timer
systemctl start certbot-renew.timer

}

set_env_variable() {
  export AWS_DEFAULT_REGION=us-east-1
  echo "Retrieving OpenAI API key from Parameter Store..."
  export OPENAI_API_KEY=$(aws ssm get-parameter \
      --name "/ipl-bot/openai-api-key" \
      --with-decryption \
      --query 'Parameter.Value' \
      --output text)

  if [ -z "$OPENAI_API_KEY" ]; then
      echo "Failed to retrieve OpenAI API key from Parameter Store"
      exit 1
  fi
}

app_checkout_build_run() {
  git clone https://github.com/rohit23ahuja/ipl-bot.git
  sudo chown -R ec2-user:ec2-user ipl-bot
  chmod 777 -R ipl-bot
  chmod 777 -R ipl-bot/
  cd ipl-bot
  mvn clean package
  java -jar target/ipl-bot-0.0.1-SNAPSHOT.jar &
}

update_and_install
echo "packages updated"
configure_ngix
set_env_variable
app_checkout_build_run
configure_nginx